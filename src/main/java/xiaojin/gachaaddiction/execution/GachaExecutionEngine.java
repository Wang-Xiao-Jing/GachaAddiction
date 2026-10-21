package xiaojin.gachaaddiction.execution;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;
import xiaojin.gachaaddiction.reward.GachaLogicalRewardPoolResult;
import xiaojin.gachaaddiction.reward.IGachaRewardPool;
import xiaojin.gachaaddiction.reward.GachaRewardPoolRequest;
import xiaojin.gachaaddiction.reward.model.GachaLogicalReward;
import xiaojin.gachaaddiction.reward.number.IGachaLongProvider;
import xiaojin.gachaaddiction.reward.number.GachaNumberContext;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterEditor;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterSet;
import xiaojin.gachaaddiction.reward.quantity.GachaExecutionLimits;
import xiaojin.gachaaddiction.reward.quantity.GachaQuantityContext;
import xiaojin.gachaaddiction.reward.rule.GachaRuleContext;
import xiaojin.gachaaddiction.reward.rule.GachaRulePhase;
import xiaojin.gachaaddiction.reward.rule.GachaRulePipeline;
import xiaojin.gachaaddiction.reward.rule.GachaRuleScope;
import xiaojin.gachaaddiction.reward.source.GachaCustomLootTableSource;
import xiaojin.gachaaddiction.reward.source.IGachaRewardSource;
import xiaojin.gachaaddiction.reward.source.GachaVanillaLootTableSource;

import java.util.*;

public final class GachaExecutionEngine {
    private final IGachaExecutionComponentResolver components;
    private final GachaStandaloneRewardExecutor standaloneRewards;

    public GachaExecutionEngine(IGachaExecutionComponentResolver components) {
        this.components = Objects.requireNonNull(components, "components");
        this.standaloneRewards = new GachaStandaloneRewardExecutor(components);
    }

    private static void requireRoundsWithinLimit(int rounds, Optional<GachaExecutionLimits> limits,
                                                 ResourceLocation ownerId) {
        if (limits.isPresent() && rounds > limits.get().maxRounds()) {
            throw new IllegalArgumentException("Execution plan round count exceeds execution limit: " + ownerId);
        }
    }

    private static GachaExecutionRequest withoutRoundsOverride(GachaExecutionRequest request) {
        return new GachaExecutionRequest(request.source(), request.mode(), request.parameters(),
                request.inputFingerprint(), request.lootParams(), request.lootTableResolver(), request.random());
    }

    public GachaExecutionResult execute(ResourceLocation planId, GachaExecutionRequest request) {
        Objects.requireNonNull(planId, "planId");
        Objects.requireNonNull(request, "request");
        if (request.mode() == GachaExecutionMode.PREVIEW) {
            throw new IllegalArgumentException("Preview requests must use the execution plan preview service");
        }
        GachaExecutionPlanDefinition plan = components.resolveExecutionPlan(planId).orElseThrow(() ->
                new IllegalArgumentException("Unknown execution plan: " + planId));
        GachaParameterSet inputs = plan.parameterSchema().normalizeInputs(request.parameters());
        return executePlan(plan, request, inputs, new ArrayDeque<>()).result();
    }

    private GachaExecutionState executePlan(GachaExecutionPlanDefinition plan, GachaExecutionRequest request,
                                            GachaParameterSet inputs, Deque<ResourceLocation> path) {
        if (path.contains(plan.id())) {
            throw new IllegalArgumentException("Execution plan reference cycle at runtime: " + plan.id());
        }
        if (path.size() >= GachaExecutionPlanValidator.MAXIMUM_NESTING_DEPTH) {
            throw new IllegalArgumentException("Execution plan nesting depth exceeds limit: " + plan.id());
        }
        if (request.roundsOverride().isPresent() && plan.limits().isPresent()
                && request.roundsOverride().getAsInt() > plan.limits().get().maxRounds()) {
            throw new IllegalArgumentException("Execution plan round count exceeds execution limit: " + plan.id());
        }
        path.addLast(plan.id());
        try {
            GachaParameterEditor parameters = new GachaParameterEditor(request.inputFingerprint(), inputs);
            GachaExecutionState state = new GachaExecutionState(parameters, plan.limits());
            for (IGachaExecutionStep step : plan.steps()) {
                executeStep(plan, step, request, inputs, state, path);
            }
            return state;
        } finally {
            path.removeLast();
        }
    }

    private void executeStep(GachaExecutionPlanDefinition plan, IGachaExecutionStep step,
                             GachaExecutionRequest request, GachaParameterSet initialParameters,
                             GachaExecutionState state, Deque<ResourceLocation> path) {
        GachaNumberContext numbers = new GachaNumberContext(initialParameters,
                state.parameters().currentValues(), request.random());
        if (step instanceof GachaRewardPoolExecutionStep rewardPoolStep) {
            executeRewardPool(plan, rewardPoolStep, request, state, numbers);
            return;
        }
        if (step instanceof GachaCustomTableExecutionStep customTableStep) {
            executeRewardSource(new GachaCustomLootTableSource(customTableStep.target(),
                            customTableStep.target(), components), customTableStep.rounds(),
                    customTableStep.limits(), plan.limits(), request, initialParameters, state, numbers);
            return;
        }
        if (step instanceof GachaVanillaLootTableExecutionStep vanillaLootTableStep) {
            ResourceKey<LootTable> lootTable = vanillaLootTableStep.lootTable();
            executeRewardSource(new GachaVanillaLootTableSource(lootTable.location(), lootTable),
                    vanillaLootTableStep.rounds(), vanillaLootTableStep.limits(), plan.limits(), request,
                    initialParameters, state, numbers);
            return;
        }
        if (step instanceof GachaConsumptionExecutionStep consumptionStep) {
            state.addConsumption(consumptionStep.consumption());
            return;
        }
        if (step instanceof GachaRuleGroupExecutionStep ruleGroupStep) {
            executeRuleGroup(ruleGroupStep, request, initialParameters, state);
            return;
        }
        if (step instanceof GachaQuantityRuleExecutionStep quantityRuleStep) {
            executeQuantityRule(quantityRuleStep, request, initialParameters, state);
            return;
        }
        if (step instanceof GachaComponentReferenceStep reference
                && reference.type().equals(GachaBuiltinExecutionSteps.EXECUTION_PLAN)) {
            executeNestedPlan(plan, reference, request, state, numbers, path);
            return;
        }
        throw new IllegalArgumentException("Execution step is not executable yet: " + step.type());
    }

    private void executeRuleGroup(GachaRuleGroupExecutionStep step, GachaExecutionRequest request,
                                  GachaParameterSet initialParameters, GachaExecutionState state) {
        GachaRewardPoolRequest ruleRequest = new GachaRewardPoolRequest(
                request.roundsOverride().orElse(1), request.lootParams(), initialParameters,
                request.inputFingerprint(), request.lootTableResolver(), request.random());
        GachaRuleContext context = new GachaRuleContext(ruleRequest, OptionalInt.empty(), Optional.empty(),
                OptionalInt.empty());
        GachaRulePipeline pipeline = new GachaRulePipeline(step.rules());
        EnumSet<GachaRuleScope> scopes = EnumSet.allOf(GachaRuleScope.class);
        var budget = state.ruleBudget();
        for (GachaRulePhase phase : GachaRulePhase.values()) {
            if (phase == GachaRulePhase.PRE_SELECT) {
                continue;
            }
            pipeline.run(phase, scopes, context, state.logicalRewards(), state.parameters(), budget);
        }
    }

    private void executeQuantityRule(GachaQuantityRuleExecutionStep step, GachaExecutionRequest request,
                                     GachaParameterSet initialParameters, GachaExecutionState state) {
        GachaRewardPoolRequest ruleRequest = new GachaRewardPoolRequest(
                request.roundsOverride().orElse(1), request.lootParams(), initialParameters,
                request.inputFingerprint(), request.lootTableResolver(), request.random());
        GachaNumberContext numbers = new GachaNumberContext(ruleRequest.parameters(),
                state.parameters().currentValues(), request.random());
        for (int index = state.logicalRewards().size() - 1; index >= 0; index--) {
            GachaLogicalReward reward = state.logicalRewards().get(index);
            long quantity = step.pipeline().apply(reward.quantity(), new GachaQuantityContext(numbers));
            if (quantity == 0) {
                state.logicalRewards().remove(index);
            } else {
                state.logicalRewards().replace(index, reward.withQuantity(quantity));
            }
        }
    }

    private void executeRewardPool(GachaExecutionPlanDefinition plan, GachaRewardPoolExecutionStep step,
                                   GachaExecutionRequest request, GachaExecutionState state,
                                   GachaNumberContext numbers) {
        IGachaRewardPool rewardPool = components.resolveRewardPool(step.target()).orElseThrow(() ->
                new IllegalArgumentException("Unknown reward pool: " + step.target()));
        int rounds = request.roundsOverride().orElseGet(() -> Math.toIntExact(step.rounds().get(numbers)));
        requireRoundsWithinLimit(rounds, plan.limits(), plan.id());
        GachaParameterSet mapped = step.parameters().apply(rewardPool.parameterSchema(), numbers);
        GachaLogicalRewardPoolResult result = rewardPool.drawLogical(new GachaRewardPoolRequest(rounds, request.lootParams(),
                mapped, request.inputFingerprint(), request.lootTableResolver(), request.random()));
        state.appendLogicalRewards(result.rewards(), result.limits(), result.usage(), result.presentationRounds());
        state.addDiagnostics(result.problems());
        step.parameters().applyOutputs(rewardPool.parameterSchema(), result.parameterChanges().finalValues(),
                plan.parameterSchema(), state.parameters());
    }

    private void executeRewardSource(IGachaRewardSource source, IGachaLongProvider roundsProvider,
                                     GachaExecutionLimits limits, Optional<GachaExecutionLimits> planLimits,
                                     GachaExecutionRequest request,
                                     GachaParameterSet initialParameters, GachaExecutionState state,
                                     GachaNumberContext numbers) {
        GachaExecutionLimits effectiveLimits = planLimits.map(plan -> plan.restrictWith(limits)).orElse(limits);
        int rounds = request.roundsOverride().orElseGet(() -> Math.toIntExact(roundsProvider.get(numbers)));
        if (rounds <= 0) {
            throw new IllegalArgumentException("Direct reward source round count must be positive");
        }
        if (rounds > effectiveLimits.maxRounds()) {
            throw new IllegalArgumentException("Direct reward source round count exceeds execution limit");
        }
        if (rounds > effectiveLimits.maxSourceSelections()) {
            throw new IllegalArgumentException("Direct reward source selection count exceeds execution limit");
        }
        GachaRewardPoolRequest sourceRequest = new GachaRewardPoolRequest(rounds, request.lootParams(),
                initialParameters, request.inputFingerprint(), request.lootTableResolver(), request.random());
        GachaLogicalRewardPoolResult result = standaloneRewards.drawLogical(source, sourceRequest,
                state.parameters().currentValues(), effectiveLimits);
        state.appendLogicalRewards(result.rewards(), result.limits(), result.usage(), result.presentationRounds());
        state.addDiagnostics(result.problems());
    }

    private void executeNestedPlan(GachaExecutionPlanDefinition plan, GachaComponentReferenceStep step,
                                   GachaExecutionRequest request, GachaExecutionState state,
                                   GachaNumberContext numbers, Deque<ResourceLocation> path) {
        GachaExecutionPlanDefinition nested = components.resolveExecutionPlan(step.target()).orElseThrow(() ->
                new IllegalArgumentException("Unknown execution plan: " + step.target()));
        GachaParameterSet mapped = step.parameters().apply(nested.parameterSchema(), numbers);
        GachaExecutionState nestedState = executePlan(nested, withoutRoundsOverride(request), mapped, path);
        state.appendState(nestedState);
        step.parameters().applyOutputs(nested.parameterSchema(), nestedState.parameters().buildChangeSet().finalValues(),
                plan.parameterSchema(), state.parameters());
    }
}

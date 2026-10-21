package xiaojin.gachaaddiction.execution;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import xiaojin.gachaaddiction.reward.number.GachaNumberContext;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterSet;
import xiaojin.gachaaddiction.reward.probability.GachaProbabilityDescribable;
import xiaojin.gachaaddiction.reward.probability.GachaProbabilityDescription;
import xiaojin.gachaaddiction.reward.quantity.GachaExecutionLimits;

import java.util.*;

public final class GachaExecutionPreviewService {
    private final GachaExecutionComponentResolver components;

    public GachaExecutionPreviewService(GachaExecutionComponentResolver components) {
        this.components = Objects.requireNonNull(components, "components");
    }

    private static void addIssue(List<GachaExecutionPreviewIssue> issues, GachaExecutionPreviewIssue issue) {
        if (!issues.contains(issue)) {
            issues.add(issue);
        }
    }

    private static void requireRoundsWithinLimit(long rounds, GachaExecutionLimits limits,
                                                 ResourceLocation planId) {
        if (rounds > limits.maxRounds()) {
            throw new IllegalArgumentException("Direct reward source round count exceeds execution limit: " + planId);
        }
    }

    private static void requireRoundsWithinLimit(long rounds, Optional<GachaExecutionLimits> limits,
                                                 ResourceLocation planId) {
        if (limits.isPresent() && rounds > limits.get().maxRounds()) {
            throw new IllegalArgumentException("Execution plan round count exceeds execution limit: " + planId);
        }
    }

    private static void requireSourceSelectionsWithinLimit(long rounds, Optional<GachaExecutionLimits> limits,
                                                           ResourceLocation planId) {
        if (limits.isPresent() && rounds > limits.get().maxSourceSelections()) {
            throw new IllegalArgumentException("Direct reward source selection count exceeds execution limit: "
                    + planId);
        }
    }

    private static void requireSourceSelectionsWithinLimit(long rounds, GachaExecutionLimits limits,
                                                           ResourceLocation planId) {
        if (rounds > limits.maxSourceSelections()) {
            throw new IllegalArgumentException("Direct reward source selection count exceeds execution limit: "
                    + planId);
        }
    }

    public GachaExecutionPreview preview(ResourceLocation planId, GachaParameterSet parameters, long previewSeed) {
        return preview(planId, parameters, previewSeed, OptionalInt.empty());
    }

    public GachaExecutionPreview preview(ResourceLocation planId, GachaParameterSet parameters, long previewSeed,
                                         int rounds) {
        return preview(planId, parameters, previewSeed, OptionalInt.of(rounds));
    }

    public GachaExecutionPreview preview(ResourceLocation planId, GachaParameterSet parameters, long previewSeed,
                                         OptionalInt roundsOverride) {
        GachaExecutionPlanDefinition plan = components.resolveExecutionPlan(
                Objects.requireNonNull(planId, "planId")).orElseThrow(() ->
                new IllegalArgumentException("Unknown execution plan: " + planId));
        GachaParameterSet normalized = plan.parameterSchema().normalizeInputs(
                Objects.requireNonNull(parameters, "parameters"));
        Objects.requireNonNull(roundsOverride, "roundsOverride");
        if (roundsOverride.isPresent() && roundsOverride.getAsInt() <= 0) {
            throw new IllegalArgumentException("Rounds override must be positive");
        }
        List<GachaExecutionStepPreview> steps = new ArrayList<>();
        List<GachaExecutionPreviewIssue> issues = new ArrayList<>();
        List<GachaProbabilityDescription> probabilityDescriptions = new ArrayList<>();
        collect(plan, normalized, RandomSource.create(previewSeed), roundsOverride,
                new ArrayDeque<>(), steps, issues, probabilityDescriptions);
        return new GachaExecutionPreview(planId, normalized, steps, issues, probabilityDescriptions);
    }

    private void collect(GachaExecutionPlanDefinition plan, GachaParameterSet parameters, RandomSource random,
                         OptionalInt roundsOverride,
                         Deque<ResourceLocation> path, List<GachaExecutionStepPreview> steps,
                         List<GachaExecutionPreviewIssue> issues,
                         List<GachaProbabilityDescription> probabilityDescriptions) {
        if (path.contains(plan.id()) || path.size() >= GachaExecutionPlanValidator.MAXIMUM_NESTING_DEPTH) {
            throw new IllegalArgumentException("Invalid nested execution plan preview: " + plan.id());
        }
        if (roundsOverride.isPresent() && plan.limits().isPresent()
                && roundsOverride.getAsInt() > plan.limits().get().maxRounds()) {
            throw new IllegalArgumentException("Execution plan round count exceeds execution limit: " + plan.id());
        }
        path.addLast(plan.id());
        try {
            GachaNumberContext numbers = new GachaNumberContext(parameters, parameters, random);
            for (GachaExecutionStep step : plan.steps()) {
                if (step instanceof GachaRewardPoolExecutionStep rewardPool) {
                    long rounds = roundsOverride.isPresent() ? roundsOverride.getAsInt() : rewardPool.rounds().get(numbers);
                    if (rounds <= 0) {
                        throw new IllegalArgumentException("Previewed round count must be positive: " + rounds);
                    }
                    requireRoundsWithinLimit(rounds, plan.limits(), plan.id());
                    components.resolveRewardPool(rewardPool.target())
                            .flatMap(pool -> pool.executionLimits())
                            .ifPresent(limits -> requireRoundsWithinLimit(rounds, limits, rewardPool.target()));
                    steps.add(new GachaExecutionStepPreview(step.type(), Optional.of(rewardPool.target()),
                            OptionalLong.of(rounds), List.of()));
                    components.resolveRewardPool(rewardPool.target())
                            .filter(GachaProbabilityDescribable.class::isInstance)
                            .map(GachaProbabilityDescribable.class::cast)
                            .map(GachaProbabilityDescribable::describeProbability)
                            .ifPresent(probabilityDescriptions::add);
                    if (rewardPool.writes().contains(GachaExecutionChannel.PARAMETERS)) {
                        addIssue(issues, GachaExecutionPreviewIssue.STATE_DEPENDENT_OUTPUTS);
                    }
                } else if (step instanceof GachaCustomTableExecutionStep customTable) {
                    long rounds = roundsOverride.isPresent() ? roundsOverride.getAsInt() : customTable.rounds().get(numbers);
                    if (rounds <= 0) {
                        throw new IllegalArgumentException("Previewed round count must be positive: " + rounds);
                    }
                    requireRoundsWithinLimit(rounds, plan.limits(), plan.id());
                    requireSourceSelectionsWithinLimit(rounds, plan.limits(), plan.id());
                    requireRoundsWithinLimit(rounds, customTable.limits(), plan.id());
                    requireSourceSelectionsWithinLimit(rounds, customTable.limits(), plan.id());
                    steps.add(new GachaExecutionStepPreview(step.type(), Optional.of(customTable.target()),
                            OptionalLong.of(rounds), List.of()));
                    components.resolveTable(customTable.target())
                            .map(table -> table.describeProbability())
                            .ifPresent(probabilityDescriptions::add);
                } else if (step instanceof GachaVanillaLootTableExecutionStep vanillaLootTable) {
                    long rounds = roundsOverride.isPresent()
                            ? roundsOverride.getAsInt() : vanillaLootTable.rounds().get(numbers);
                    if (rounds <= 0) {
                        throw new IllegalArgumentException("Previewed round count must be positive: " + rounds);
                    }
                    requireRoundsWithinLimit(rounds, plan.limits(), plan.id());
                    requireSourceSelectionsWithinLimit(rounds, plan.limits(), plan.id());
                    requireRoundsWithinLimit(rounds, vanillaLootTable.limits(), plan.id());
                    requireSourceSelectionsWithinLimit(rounds, vanillaLootTable.limits(), plan.id());
                    steps.add(new GachaExecutionStepPreview(step.type(),
                            Optional.of(vanillaLootTable.lootTable().location()), OptionalLong.of(rounds), List.of()));
                } else if (step instanceof GachaConsumptionExecutionStep consumption) {
                    steps.add(new GachaExecutionStepPreview(step.type(), Optional.empty(), OptionalLong.empty(),
                            consumption.consumption().stream().map(specification -> specification.type()).toList()));
                } else if (step instanceof GachaRuleGroupExecutionStep ruleGroup) {
                    steps.add(new GachaExecutionStepPreview(step.type(), Optional.of(ruleGroup.target()),
                            OptionalLong.empty(), List.of()));
                    if (ruleGroup.writes().contains(GachaExecutionChannel.PARAMETERS)) {
                        addIssue(issues, GachaExecutionPreviewIssue.STATE_DEPENDENT_OUTPUTS);
                    }
                } else if (step instanceof GachaQuantityRuleExecutionStep quantityRule) {
                    steps.add(new GachaExecutionStepPreview(step.type(), Optional.of(quantityRule.target()),
                            OptionalLong.empty(), List.of()));
                } else if (step instanceof GachaComponentReferenceStep reference
                        && reference.type().equals(GachaBuiltinExecutionSteps.EXECUTION_PLAN)) {
                    steps.add(new GachaExecutionStepPreview(step.type(), Optional.of(reference.target()),
                            OptionalLong.empty(), List.of()));
                    GachaExecutionPlanDefinition nested = components.resolveExecutionPlan(reference.target())
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Unknown execution plan: " + reference.target()));
                    GachaParameterSet mapped = reference.parameters().apply(nested.parameterSchema(), numbers);
                    collect(nested, mapped, random, OptionalInt.empty(), path, steps, issues, probabilityDescriptions);
                    if (reference.writes().contains(GachaExecutionChannel.PARAMETERS)) {
                        addIssue(issues, GachaExecutionPreviewIssue.STATE_DEPENDENT_OUTPUTS);
                    }
                } else {
                    steps.add(new GachaExecutionStepPreview(step.type(), Optional.empty(),
                            OptionalLong.empty(), List.of()));
                    addIssue(issues, GachaExecutionPreviewIssue.UNSUPPORTED_CUSTOM_STEP);
                }
            }
        } finally {
            path.removeLast();
        }
    }
}

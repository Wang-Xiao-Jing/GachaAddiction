package xiaojin.gachaaddiction.reward;

import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.reward.model.GachaResultEditor;
import xiaojin.gachaaddiction.reward.model.GachaRewardFactory;
import xiaojin.gachaaddiction.reward.number.GachaLongProvider;
import xiaojin.gachaaddiction.reward.number.GachaNumberContext;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterEditor;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterSchema;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterSet;
import xiaojin.gachaaddiction.reward.probability.GachaProbabilityDescribable;
import xiaojin.gachaaddiction.reward.probability.GachaProbabilityDescription;
import xiaojin.gachaaddiction.reward.probability.GachaProbabilitySelectionMode;
import xiaojin.gachaaddiction.reward.quantity.GachaExecutionLimits;
import xiaojin.gachaaddiction.reward.quantity.GachaExecutionUsage;
import xiaojin.gachaaddiction.reward.quantity.GachaRewardFinalizer;
import xiaojin.gachaaddiction.reward.rule.*;
import xiaojin.gachaaddiction.reward.source.*;

import java.util.*;

public final class GachaHybridRewardPool implements GachaRewardPool, GachaProbabilityDescribable {
    private final ResourceLocation id;
    private final List<GachaWeightedSource> sources;
    private final GachaSourceSelectionStrategy selectionStrategy;
    private final GachaLongProvider selectionCount;
    private final GachaExecutionLimits executionLimits;
    private final GachaParameterSchema parameterSchema;
    private final GachaRulePipeline rulePipeline;
    private final GachaRewardFactory rewardFactory = new GachaRewardFactory();

    public GachaHybridRewardPool(ResourceLocation id, List<GachaWeightedSource> sources,
                                 GachaSourceSelectionStrategy selectionStrategy,
                                 GachaLongProvider selectionCount, GachaExecutionLimits executionLimits,
                                 GachaParameterSchema parameterSchema, List<GachaRuleDefinition> rules) {
        this.id = Objects.requireNonNull(id, "id");
        this.sources = List.copyOf(sources);
        if (this.sources.isEmpty()) {
            throw new IllegalArgumentException("Reward pool must contain at least one source");
        }
        this.selectionStrategy = Objects.requireNonNull(selectionStrategy, "selectionStrategy");
        this.selectionCount = Objects.requireNonNull(selectionCount, "selectionCount");
        this.executionLimits = Objects.requireNonNull(executionLimits, "executionLimits");
        this.parameterSchema = Objects.requireNonNull(parameterSchema, "parameterSchema");
        this.rulePipeline = new GachaRulePipeline(Objects.requireNonNull(rules, "rules"));
    }

    @Override
    public ResourceLocation id() {
        return id;
    }

    @Override
    public GachaProbabilityDescription describeProbability() {
        GachaProbabilitySelectionMode mode = switch (selectionStrategy) {
            case SELECT_ONE_SOURCE -> GachaProbabilitySelectionMode.WEIGHTED_SINGLE;
            case COLLECT_ALL_SOURCES -> GachaProbabilitySelectionMode.COLLECT_ALL_ELIGIBLE;
            case SELECT_MULTIPLE_SOURCES -> GachaProbabilitySelectionMode.WEIGHTED_WITHOUT_REPLACEMENT;
            case FIRST_MATCH -> GachaProbabilitySelectionMode.FIRST_ELIGIBLE;
        };
        List<GachaProbabilityDescription.Candidate> candidates = sources.stream()
                .map(source -> new GachaProbabilityDescription.Candidate(source.source().id(),
                        source.weight().expression()))
                .toList();
        return new GachaProbabilityDescription(id, mode, selectionCount.expression(), candidates);
    }

    @Override
    public GachaParameterSchema parameterSchema() {
        return parameterSchema;
    }

    @Override
    public Optional<GachaExecutionLimits> executionLimits() {
        return Optional.of(executionLimits);
    }

    @Override
    public GachaRewardPoolResult draw(GachaRewardPoolRequest request) {
        GachaLogicalRewardPoolResult result = drawLogical(request);
        return new GachaRewardPoolResult(GachaRewardFinalizer.finalizeRewards(result.rewards(), executionLimits),
                result.parameterChanges(), result.problems());
    }

    @Override
    public GachaLogicalRewardPoolResult drawLogical(GachaRewardPoolRequest request) {
        Objects.requireNonNull(request, "request");
        if (request.rounds() > executionLimits.maxRounds()) {
            throw new IllegalArgumentException("Round count exceeds execution limit");
        }
        GachaParameterSet normalizedInputs = parameterSchema.normalizeInputs(request.parameters());
        GachaRewardPoolRequest executionRequest = new GachaRewardPoolRequest(request.rounds(), request.lootParams(),
                normalizedInputs, request.inputFingerprint(), request.lootTableResolver(), request.random());
        GachaParameterEditor parameters = new GachaParameterEditor(request.inputFingerprint(), normalizedInputs);
        GachaRuleExecutionBudget ruleBudget = new GachaRuleExecutionBudget(executionLimits.maxRuleActions());
        GachaGenerationBudget generationBudget = new GachaGenerationBudget(executionLimits.maxCustomTableDepth(),
                executionLimits.maxCustomEntryVisits());
        GachaResultEditor poolResults = new GachaResultEditor(List.of());
        GachaRuleContext poolContext = new GachaRuleContext(executionRequest, OptionalInt.empty(), Optional.empty(),
                OptionalInt.empty());
        rulePipeline.run(GachaRulePhase.PRE_SELECT, EnumSet.of(GachaRuleScope.POOL_RESULT_SET), poolContext,
                poolResults, parameters, ruleBudget);
        int sourceSelections = 0;
        for (int round = 0; round < request.rounds(); round++) {
            GachaResultEditor roundResults = new GachaResultEditor(List.of());
            GachaRuleContext roundContext = new GachaRuleContext(executionRequest, OptionalInt.of(round), Optional.empty(),
                    OptionalInt.empty());
            rulePipeline.run(GachaRulePhase.PRE_SELECT, EnumSet.of(GachaRuleScope.RESULT_SET), roundContext,
                    roundResults, parameters, ruleBudget);
            GachaNumberContext numberContext = new GachaNumberContext(normalizedInputs,
                    parameters.currentValues(), request.random());
            int count = Math.toIntExact(selectionCount.get(numberContext));
            List<GachaRewardSource> selectedSources = GachaSourceSelector.select(sources, selectionStrategy, count,
                    numberContext);
            sourceSelections = Math.addExact(sourceSelections, selectedSources.size());
            if (sourceSelections > executionLimits.maxSourceSelections()) {
                throw new IllegalArgumentException("Source selection count exceeds execution limit");
            }
            for (GachaRewardSource source : selectedSources) {
                GachaResultEditor sourceResults = new GachaResultEditor(List.of());
                GachaRuleContext sourceContext = new GachaRuleContext(executionRequest, OptionalInt.of(round),
                        Optional.of(source.id()), OptionalInt.empty());
                rulePipeline.run(GachaRulePhase.PRE_SELECT, EnumSet.of(GachaRuleScope.SOURCE), sourceContext,
                        sourceResults, parameters, ruleBudget);
                source.generate(new GachaSourceGenerationContext(executionRequest, round, source.id(),
                        parameters.currentValues(), rewardFactory, generationBudget)).forEach(sourceResults::append);
                runSourcePhases(sourceContext, sourceResults, parameters, ruleBudget);
                sourceResults.results().forEach(roundResults::append);
            }
            runRoundPhases(roundContext, roundResults, parameters, ruleBudget);
            for (var reward : roundResults.results()) {
                poolResults.append(reward.withPresentationRound(round));
            }
        }
        runPoolPhases(poolContext, poolResults, parameters, ruleBudget);
        return new GachaLogicalRewardPoolResult(poolResults.results(), parameters.buildChangeSet(), List.of(),
                Optional.of(executionLimits), new GachaExecutionUsage(sourceSelections, ruleBudget.usedActions(),
                generationBudget.entryVisits(), generationBudget.maximumDepthObserved()), request.rounds());
    }

    private void runSourcePhases(GachaRuleContext context, GachaResultEditor results,
                                 GachaParameterEditor parameters, GachaRuleExecutionBudget budget) {
        EnumSet<GachaRuleScope> scopes = EnumSet.of(GachaRuleScope.SOURCE, GachaRuleScope.EACH_RESULT,
                GachaRuleScope.RESULT_SET);
        for (GachaRulePhase phase : List.of(GachaRulePhase.POST_GENERATE, GachaRulePhase.FILTER,
                GachaRulePhase.TRANSFORM, GachaRulePhase.APPEND)) {
            rulePipeline.run(phase, scopes, context, results, parameters, budget);
        }
    }

    private void runRoundPhases(GachaRuleContext context, GachaResultEditor results,
                                GachaParameterEditor parameters, GachaRuleExecutionBudget budget) {
        EnumSet<GachaRuleScope> scopes = EnumSet.of(GachaRuleScope.EACH_RESULT, GachaRuleScope.RESULT_SET);
        for (GachaRulePhase phase : List.of(GachaRulePhase.POST_MERGE, GachaRulePhase.PITY,
                GachaRulePhase.FINALIZE)) {
            rulePipeline.run(phase, scopes, context, results, parameters, budget);
        }
    }

    private void runPoolPhases(GachaRuleContext context, GachaResultEditor results,
                               GachaParameterEditor parameters, GachaRuleExecutionBudget budget) {
        for (GachaRulePhase phase : GachaRulePhase.values()) {
            if (phase != GachaRulePhase.PRE_SELECT) {
                rulePipeline.run(phase, EnumSet.of(GachaRuleScope.POOL_RESULT_SET), context, results, parameters,
                        budget);
            }
        }
    }
}

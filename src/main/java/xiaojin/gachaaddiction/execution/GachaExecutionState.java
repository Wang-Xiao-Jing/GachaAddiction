package xiaojin.gachaaddiction.execution;

import net.minecraft.world.item.ItemStack;
import xiaojin.gachaaddiction.api.GachaSecondaryType;
import xiaojin.gachaaddiction.cardpool.transaction.GachaConsumptionSpec;
import xiaojin.gachaaddiction.reward.model.GachaLogicalReward;
import xiaojin.gachaaddiction.reward.model.GachaResultEditor;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterEditor;
import xiaojin.gachaaddiction.reward.problem.GachaProblem;
import xiaojin.gachaaddiction.reward.quantity.GachaExecutionLimits;
import xiaojin.gachaaddiction.reward.quantity.GachaExecutionUsage;
import xiaojin.gachaaddiction.reward.quantity.GachaRewardFinalizer;
import xiaojin.gachaaddiction.reward.rule.GachaRuleExecutionBudget;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class GachaExecutionState {
    private final GachaParameterEditor parameters;
    private final GachaResultEditor logicalRewards = new GachaResultEditor(List.of());
    private final List<ItemStack> finalizedRewards = new ArrayList<>();
    private final List<GachaConsumptionSpec> consumption = new ArrayList<>();
    private final List<GachaProblem> diagnostics = new ArrayList<>();
    private Optional<GachaExecutionLimits> logicalLimits = Optional.empty();
    private GachaRuleExecutionBudget ruleBudget;
    private GachaExecutionUsage usage = GachaExecutionUsage.empty();
    private int presentationRounds;

    public GachaExecutionState(GachaParameterEditor parameters) {
        this(parameters, Optional.empty());
    }

    public GachaExecutionState(GachaParameterEditor parameters, Optional<GachaExecutionLimits> limits) {
        this.parameters = Objects.requireNonNull(parameters, "parameters");
        this.logicalLimits = Objects.requireNonNull(limits, "limits");
    }

    public GachaParameterEditor parameters() {
        return parameters;
    }

    public void appendRewards(List<ItemStack> appended) {
        Objects.requireNonNull(appended, "appended").forEach(stack -> finalizedRewards.add(stack.copy()));
    }

    public void appendLogicalRewards(List<GachaLogicalReward> appended, Optional<GachaExecutionLimits> limits) {
        appendLogicalRewards(appended, limits, GachaExecutionUsage.empty());
    }

    public void appendLogicalRewards(List<GachaLogicalReward> appended, Optional<GachaExecutionLimits> limits,
                                     GachaExecutionUsage addedUsage) {
        appendLogicalRewards(appended, limits, addedUsage, 0);
    }

    public void appendLogicalRewards(List<GachaLogicalReward> appended, Optional<GachaExecutionLimits> limits,
                                     GachaExecutionUsage addedUsage, int rounds) {
        if (rounds > 0) {
            int offset = presentationRounds;
            appended = appended.stream().map(reward -> reward.presentationRound() < 0 ? reward
                    : reward.withPresentationRound(Math.addExact(offset, reward.presentationRound()))).toList();
            presentationRounds = Math.addExact(presentationRounds, rounds);
        }
        Objects.requireNonNull(appended, "appended").forEach(logicalRewards::append);
        Objects.requireNonNull(limits, "limits").ifPresent(this::restrictLogicalLimits);
        appendUsage(Objects.requireNonNull(addedUsage, "addedUsage"));
    }

    public void appendState(GachaExecutionState nested) {
        Objects.requireNonNull(nested, "nested");
        appendLogicalRewards(nested.logicalRewards.results(), nested.logicalLimits, GachaExecutionUsage.empty(), nested.presentationRounds);
        appendUsage(nested.usage());
        appendRewards(nested.finalizedRewards);
        addConsumption(nested.consumption);
        addDiagnostics(nested.diagnostics);
    }

    public GachaResultEditor logicalRewards() {
        return logicalRewards;
    }

    public GachaRuleExecutionBudget ruleBudget() {
        int maximumActions = logicalLimits.map(GachaExecutionLimits::maxRuleActions).orElse(Integer.MAX_VALUE);
        if (ruleBudget == null) {
            ruleBudget = new GachaRuleExecutionBudget(maximumActions);
        } else {
            ruleBudget.restrictTo(maximumActions);
        }
        return ruleBudget;
    }

    public GachaExecutionUsage usage() {
        return new GachaExecutionUsage(usage.sourceSelections(),
                ruleBudget == null ? usage.ruleActions() : ruleBudget.usedActions(),
                usage.customEntryVisits(), usage.maximumCustomTableDepth());
    }

    public void addDiagnostics(List<GachaProblem> added) {
        diagnostics.addAll(Objects.requireNonNull(added, "added"));
    }

    public void addConsumption(List<GachaConsumptionSpec> added) {
        consumption.addAll(Objects.requireNonNull(added, "added"));
    }

    public GachaExecutionResult result() {
        List<ItemStack> rewards = new ArrayList<>(finalizedRewards);
        if (logicalRewards.size() > 0) {
            GachaExecutionLimits limits = logicalLimits.orElseThrow(() -> new IllegalStateException(
                    "Logical execution results have no finalization limits"));
            rewards.addAll(GachaRewardFinalizer.finalizeRewards(logicalRewards.results(), limits));
        }
        return new GachaExecutionResult(rewards, consumption, parameters.buildChangeSet(), diagnostics, presentationGroups());
    }

    private List<List<ItemStack>> presentationGroups() {
        if (presentationRounds <= 1 || presentationRounds > GachaSecondaryType.MAXIMUM_ROUNDS || !finalizedRewards.isEmpty()
                || logicalRewards.results().stream().anyMatch(reward -> reward.presentationRound() < 0
                || reward.presentationRound() >= presentationRounds)) {
            return List.of();
        }

        List<List<ItemStack>> groups = new ArrayList<>();
        for (int round = 0; round < presentationRounds; round++) {
            groups.add(new ArrayList<>());
        }
        if (logicalRewards.size() == 0) {
            return groups;
        }
        GachaExecutionLimits limits = logicalLimits.orElseThrow();
        for (GachaLogicalReward reward : logicalRewards.results()) {
            groups.get(reward.presentationRound()).addAll(GachaRewardFinalizer.finalizeRewards(List.of(reward), limits));
        }
        return groups;
    }

    private void restrictLogicalLimits(GachaExecutionLimits limits) {
        logicalLimits = logicalLimits.map(existing -> existing.restrictWith(limits)).or(() -> Optional.of(limits));
        if (ruleBudget != null) {
            ruleBudget.restrictTo(limits.maxRuleActions());
        }
    }

    private void appendUsage(GachaExecutionUsage added) {
        usage = new GachaExecutionUsage(Math.addExact(usage.sourceSelections(), added.sourceSelections()),
                0,
                Math.addExact(usage.customEntryVisits(), added.customEntryVisits()),
                Math.max(usage.maximumCustomTableDepth(), added.maximumCustomTableDepth()));
        ruleBudget().consumeActions(added.ruleActions());
        logicalLimits.ifPresent(limits -> {
            if (usage.sourceSelections() > limits.maxSourceSelections()) {
                throw new IllegalArgumentException("Source selection count exceeds execution limit");
            }
            if (usage.customEntryVisits() > limits.maxCustomEntryVisits()) {
                throw new IllegalArgumentException("Custom entry visits exceed execution limit");
            }
            if (usage.maximumCustomTableDepth() > limits.maxCustomTableDepth()) {
                throw new IllegalArgumentException("Custom table depth exceeds execution limit");
            }
        });
    }
}

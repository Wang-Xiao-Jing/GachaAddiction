package xiaojin.gachaaddiction.reward.rule;

import xiaojin.gachaaddiction.reward.model.GachaResultEditor;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterEditor;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

public final class GachaRulePipeline {
    private final List<GachaRuleDefinition> rules;

    public GachaRulePipeline(List<GachaRuleDefinition> rules) {
        this.rules = rules.stream().sorted(Comparator.comparing(GachaRuleDefinition::phase)).toList();
    }

    private static void runForEachResult(GachaRuleDefinition rule, GachaRuleContext context,
                                         GachaResultEditor results, GachaParameterEditor parameters,
                                         GachaRuleExecutionBudget budget) {
        int initialSize = results.size();
        for (int index = initialSize - 1; index >= 0; index--) {
            if (index < results.size()) {
                GachaRuleRunner.run(rule, context.withRewardIndex(index), results, parameters, budget);
            }
        }
    }

    public void run(GachaRulePhase phase, EnumSet<GachaRuleScope> acceptedScopes, GachaRuleContext context,
                    GachaResultEditor results, GachaParameterEditor parameters, GachaRuleExecutionBudget budget) {
        Objects.requireNonNull(phase, "phase");
        Objects.requireNonNull(acceptedScopes, "acceptedScopes");
        Objects.requireNonNull(context, "context");
        for (GachaRuleDefinition rule : rules) {
            if (rule.phase() != phase || !acceptedScopes.contains(rule.scope())) {
                continue;
            }
            if (rule.scope() == GachaRuleScope.EACH_RESULT) {
                runForEachResult(rule, context, results, parameters, budget);
            } else {
                GachaRuleRunner.run(rule, context, results, parameters, budget);
            }
        }
    }
}

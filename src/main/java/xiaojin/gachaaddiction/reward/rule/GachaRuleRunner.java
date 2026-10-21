package xiaojin.gachaaddiction.reward.rule;

import xiaojin.gachaaddiction.reward.model.GachaResultEditor;
import xiaojin.gachaaddiction.reward.model.GachaResultSnapshot;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterEditor;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterSnapshot;

import java.util.List;
import java.util.Objects;

public final class GachaRuleRunner {
    private GachaRuleRunner() {
    }

    public static GachaRuleRunStatus run(GachaRuleDefinition rule, GachaRuleContext context,
                                         GachaResultEditor results, GachaParameterEditor parameters,
                                         GachaRuleExecutionBudget budget) {
        Objects.requireNonNull(rule, "rule");
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(results, "results");
        Objects.requireNonNull(parameters, "parameters");
        Objects.requireNonNull(budget, "budget");

        GachaResultSnapshot resultSnapshot = results.snapshot();
        GachaParameterSnapshot parameterSnapshot = parameters.snapshot();
        try {
            boolean matches = rule.conditions().stream()
                    .allMatch(condition -> condition.test(context, results.results(), parameters.currentValues()));
            if (!matches) {
                return GachaRuleRunStatus.NOT_MATCHED;
            }
            applyActions(rule.actions(), context, results, parameters, budget);
            return GachaRuleRunStatus.APPLIED;
        } catch (RuntimeException failure) {
            results.restore(resultSnapshot);
            parameters.restore(parameterSnapshot);
            return handleFailure(rule, context, results, parameters, budget, resultSnapshot, parameterSnapshot,
                    failure);
        }
    }

    private static GachaRuleRunStatus handleFailure(GachaRuleDefinition rule, GachaRuleContext context,
                                                    GachaResultEditor results, GachaParameterEditor parameters,
                                                    GachaRuleExecutionBudget budget,
                                                    GachaResultSnapshot resultSnapshot,
                                                    GachaParameterSnapshot parameterSnapshot,
                                                    RuntimeException failure) {
        return switch (rule.failurePolicy()) {
            case SKIP_RULE -> GachaRuleRunStatus.SKIPPED;
            case FAIL_DRAW, FAIL_BATCH -> throw new GachaRuleExecutionException(rule.id(), rule.failurePolicy(),
                    failure);
            case FALLBACK -> {
                try {
                    applyActions(rule.fallbackActions(), context, results, parameters, budget);
                    yield GachaRuleRunStatus.FALLBACK_APPLIED;
                } catch (RuntimeException fallbackFailure) {
                    results.restore(resultSnapshot);
                    parameters.restore(parameterSnapshot);
                    fallbackFailure.addSuppressed(failure);
                    throw new GachaRuleExecutionException(rule.id(), rule.failurePolicy(), fallbackFailure);
                }
            }
        };
    }

    private static void applyActions(List<IGachaRuleAction> actions, GachaRuleContext context,
                                     GachaResultEditor results, GachaParameterEditor parameters,
                                     GachaRuleExecutionBudget budget) {
        for (IGachaRuleAction action : actions) {
            budget.consumeAction();
            action.apply(context, results, parameters);
        }
    }
}

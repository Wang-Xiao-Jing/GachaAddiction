package xiaojin.gachaaddiction.execution;

import xiaojin.gachaaddiction.reward.GachaLogicalRewardPoolResult;
import xiaojin.gachaaddiction.reward.GachaRewardPoolRequest;
import xiaojin.gachaaddiction.reward.model.GachaLogicalReward;
import xiaojin.gachaaddiction.reward.model.GachaResultEditor;
import xiaojin.gachaaddiction.reward.number.GachaNumberContext;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterEditor;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterSet;
import xiaojin.gachaaddiction.reward.quantity.GachaExecutionLimits;
import xiaojin.gachaaddiction.reward.quantity.GachaExecutionUsage;
import xiaojin.gachaaddiction.reward.quantity.GachaQuantityContext;
import xiaojin.gachaaddiction.reward.quantity.GachaQuantityPipeline;
import xiaojin.gachaaddiction.reward.rule.*;

import java.util.*;

public final class GachaStandaloneRuleExecutor {
    public GachaLogicalRewardPoolResult applyRules(List<GachaRuleDefinition> rules,
                                                   GachaRewardPoolRequest request,
                                                   GachaParameterSet currentParameters,
                                                   List<GachaLogicalReward> rewards,
                                                   GachaExecutionLimits limits) {
        Objects.requireNonNull(rules, "rules");
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(currentParameters, "currentParameters");
        Objects.requireNonNull(rewards, "rewards");
        Objects.requireNonNull(limits, "limits");
        GachaParameterEditor parameters = new GachaParameterEditor(request.inputFingerprint(), currentParameters);
        GachaResultEditor results = new GachaResultEditor(rewards);
        GachaRuleContext context = new GachaRuleContext(request, OptionalInt.empty(), Optional.empty(),
                OptionalInt.empty());
        GachaRulePipeline pipeline = new GachaRulePipeline(rules);
        GachaRuleExecutionBudget budget = new GachaRuleExecutionBudget(limits.maxRuleActions());
        EnumSet<GachaRuleScope> scopes = EnumSet.allOf(GachaRuleScope.class);
        for (GachaRulePhase phase : GachaRulePhase.values()) {
            if (phase == GachaRulePhase.PRE_SELECT) {
                continue;
            }
            pipeline.run(phase, scopes, context, results, parameters, budget);
        }
        return new GachaLogicalRewardPoolResult(results.results(), parameters.buildChangeSet(), List.of(),
                Optional.of(limits), new GachaExecutionUsage(0, budget.usedActions(), 0, 0));
    }

    public GachaLogicalRewardPoolResult applyQuantityRule(GachaQuantityPipeline pipeline,
                                                          GachaRewardPoolRequest request,
                                                          GachaParameterSet currentParameters,
                                                          List<GachaLogicalReward> rewards,
                                                          GachaExecutionLimits limits) {
        Objects.requireNonNull(pipeline, "pipeline");
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(currentParameters, "currentParameters");
        Objects.requireNonNull(rewards, "rewards");
        Objects.requireNonNull(limits, "limits");
        GachaParameterEditor parameters = new GachaParameterEditor(request.inputFingerprint(), currentParameters);
        GachaResultEditor results = new GachaResultEditor(rewards);
        GachaNumberContext numbers = new GachaNumberContext(request.parameters(), parameters.currentValues(),
                request.random());
        GachaQuantityContext context = new GachaQuantityContext(numbers);
        for (int index = results.size() - 1; index >= 0; index--) {
            GachaLogicalReward reward = results.get(index);
            long quantity = pipeline.apply(reward.quantity(), context);
            if (quantity == 0) {
                results.remove(index);
            } else {
                results.replace(index, reward.withQuantity(quantity));
            }
        }
        return new GachaLogicalRewardPoolResult(results.results(), parameters.buildChangeSet(), List.of(),
                Optional.of(limits));
    }
}

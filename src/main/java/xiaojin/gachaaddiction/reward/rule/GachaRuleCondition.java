package xiaojin.gachaaddiction.reward.rule;

import xiaojin.gachaaddiction.reward.model.GachaLogicalReward;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterSet;

import java.util.List;

@FunctionalInterface
public interface GachaRuleCondition {
    boolean test(GachaRuleContext context, List<GachaLogicalReward> results, GachaParameterSet parameters);
}

package xiaojin.gachaaddiction.reward.rule;

import xiaojin.gachaaddiction.reward.number.GachaNumberExpression;

public interface GachaProbabilityCondition extends GachaRuleCondition {
    GachaNumberExpression probabilityExpression();
}

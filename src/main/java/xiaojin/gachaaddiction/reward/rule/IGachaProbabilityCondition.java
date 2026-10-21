package xiaojin.gachaaddiction.reward.rule;

import xiaojin.gachaaddiction.reward.number.IGachaNumberExpression;

public interface IGachaProbabilityCondition extends IGachaRuleCondition {
    IGachaNumberExpression probabilityExpression();
}

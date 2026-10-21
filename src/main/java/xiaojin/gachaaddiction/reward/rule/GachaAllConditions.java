package xiaojin.gachaaddiction.reward.rule;

import xiaojin.gachaaddiction.reward.model.GachaLogicalReward;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterSet;

import java.util.List;

public record GachaAllConditions(List<GachaRuleCondition> conditions) implements GachaRuleCondition {
    public GachaAllConditions {
        conditions = List.copyOf(conditions);
    }

    @Override
    public boolean test(GachaRuleContext context, List<GachaLogicalReward> results,
                        GachaParameterSet parameters) {
        return conditions.stream().allMatch(condition -> condition.test(context, results, parameters));
    }
}

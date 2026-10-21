package xiaojin.gachaaddiction.reward.rule;

import xiaojin.gachaaddiction.reward.model.GachaLogicalReward;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterSet;

import java.util.List;
import java.util.Objects;

public record GachaAnyRewardCondition(GachaRuleCondition condition) implements GachaRuleCondition {
    public GachaAnyRewardCondition {
        Objects.requireNonNull(condition, "condition");
    }

    @Override
    public boolean test(GachaRuleContext context, List<GachaLogicalReward> results,
                        GachaParameterSet parameters) {
        for (int index = 0; index < results.size(); index++) {
            if (condition.test(context.withRewardIndex(index), results, parameters)) {
                return true;
            }
        }
        return false;
    }
}

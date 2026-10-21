package xiaojin.gachaaddiction.reward.rule;

import xiaojin.gachaaddiction.reward.model.GachaLogicalReward;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterSet;

import java.util.List;
import java.util.Objects;

public record GachaNotCondition(GachaRuleCondition condition) implements GachaRuleCondition {
    public GachaNotCondition {
        Objects.requireNonNull(condition, "condition");
    }

    @Override
    public boolean test(GachaRuleContext context, List<GachaLogicalReward> results,
                        GachaParameterSet parameters) {
        return !condition.test(context, results, parameters);
    }
}

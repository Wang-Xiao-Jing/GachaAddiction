package xiaojin.gachaaddiction.reward.rule;

import xiaojin.gachaaddiction.reward.model.GachaLogicalReward;
import xiaojin.gachaaddiction.reward.number.GachaDoubleProvider;
import xiaojin.gachaaddiction.reward.number.GachaNumberContext;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterSet;

import java.util.List;
import java.util.Objects;

public record GachaNumberComparisonCondition(GachaDoubleProvider left, GachaComparisonOperator operator,
                                             GachaDoubleProvider right) implements GachaRuleCondition {
    public GachaNumberComparisonCondition {
        Objects.requireNonNull(left, "left");
        Objects.requireNonNull(operator, "operator");
        Objects.requireNonNull(right, "right");
    }

    @Override
    public boolean test(GachaRuleContext context, List<GachaLogicalReward> results,
                        GachaParameterSet parameters) {
        GachaNumberContext numbers = new GachaNumberContext(context.request().parameters(), parameters,
                context.request().random());
        double leftValue = left.get(numbers);
        double rightValue = right.get(numbers);
        if (!Double.isFinite(leftValue) || !Double.isFinite(rightValue)) {
            throw new IllegalArgumentException("Rule comparison values must be finite");
        }
        int comparison = Double.compare(leftValue, rightValue);
        return switch (operator) {
            case LESS_THAN -> comparison < 0;
            case LESS_THAN_OR_EQUAL -> comparison <= 0;
            case EQUAL -> comparison == 0;
            case GREATER_THAN_OR_EQUAL -> comparison >= 0;
            case GREATER_THAN -> comparison > 0;
            case NOT_EQUAL -> comparison != 0;
        };
    }
}

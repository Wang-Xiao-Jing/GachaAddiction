package xiaojin.gachaaddiction.reward.number;

import java.util.Objects;

public record GachaBinaryDoubleProvider(IGachaDoubleProvider left, GachaDoubleOperation operation,
                                        IGachaDoubleProvider right) implements IGachaDoubleProvider {
    public GachaBinaryDoubleProvider {
        Objects.requireNonNull(left, "left");
        Objects.requireNonNull(operation, "operation");
        Objects.requireNonNull(right, "right");
    }

    @Override
    public double get(GachaNumberContext context) {
        double leftValue = left.get(context);
        double rightValue = right.get(context);
        double result = switch (operation) {
            case ADD -> leftValue + rightValue;
            case SUBTRACT -> leftValue - rightValue;
            case MULTIPLY -> leftValue * rightValue;
            case DIVIDE -> leftValue / rightValue;
            case MIN -> Math.min(leftValue, rightValue);
            case MAX -> Math.max(leftValue, rightValue);
        };
        if (!Double.isFinite(result)) {
            throw new ArithmeticException("Dynamic probability calculation produced a non-finite value");
        }
        return result;
    }

    @Override
    public IGachaNumberExpression expression() {
        return new IGachaNumberExpression.Binary(operation, left.expression(), right.expression());
    }
}

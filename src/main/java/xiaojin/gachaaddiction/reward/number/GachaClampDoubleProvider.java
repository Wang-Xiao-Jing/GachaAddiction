package xiaojin.gachaaddiction.reward.number;

import java.util.Objects;

public record GachaClampDoubleProvider(GachaDoubleProvider value, GachaDoubleProvider minimum,
                                       GachaDoubleProvider maximum) implements GachaDoubleProvider {
    public GachaClampDoubleProvider {
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(minimum, "minimum");
        Objects.requireNonNull(maximum, "maximum");
    }

    @Override
    public double get(GachaNumberContext context) {
        double minimumValue = minimum.get(context);
        double maximumValue = maximum.get(context);
        if (!Double.isFinite(minimumValue) || !Double.isFinite(maximumValue) || minimumValue > maximumValue) {
            throw new IllegalArgumentException("Invalid dynamic probability clamp range");
        }
        return Math.max(minimumValue, Math.min(value.get(context), maximumValue));
    }

    @Override
    public GachaNumberExpression expression() {
        return new GachaNumberExpression.Clamp(value.expression(), minimum.expression(), maximum.expression());
    }
}

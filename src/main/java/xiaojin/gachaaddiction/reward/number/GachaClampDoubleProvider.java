package xiaojin.gachaaddiction.reward.number;

import java.util.Objects;

public record GachaClampDoubleProvider(IGachaDoubleProvider value, IGachaDoubleProvider minimum,
                                       IGachaDoubleProvider maximum) implements IGachaDoubleProvider {
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
    public IGachaNumberExpression expression() {
        return new IGachaNumberExpression.Clamp(value.expression(), minimum.expression(), maximum.expression());
    }
}

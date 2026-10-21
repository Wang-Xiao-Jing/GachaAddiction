package xiaojin.gachaaddiction.reward.number;

public record GachaConstantDoubleProvider(double value) implements GachaDoubleProvider {
    public GachaConstantDoubleProvider {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("Constant value must be finite");
        }
    }

    @Override
    public double get(GachaNumberContext context) {
        return value;
    }

    @Override
    public GachaNumberExpression expression() {
        return new GachaNumberExpression.DoubleConstant(value);
    }
}

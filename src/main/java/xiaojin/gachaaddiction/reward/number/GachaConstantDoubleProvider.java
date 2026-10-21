package xiaojin.gachaaddiction.reward.number;

public record GachaConstantDoubleProvider(double value) implements IGachaDoubleProvider {
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
    public IGachaNumberExpression expression() {
        return new IGachaNumberExpression.DoubleConstant(value);
    }
}

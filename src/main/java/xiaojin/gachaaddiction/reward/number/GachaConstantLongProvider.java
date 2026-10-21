package xiaojin.gachaaddiction.reward.number;

public record GachaConstantLongProvider(long value) implements GachaLongProvider {
    @Override
    public long get(GachaNumberContext context) {
        return value;
    }

    @Override
    public GachaNumberExpression expression() {
        return new GachaNumberExpression.LongConstant(value);
    }
}

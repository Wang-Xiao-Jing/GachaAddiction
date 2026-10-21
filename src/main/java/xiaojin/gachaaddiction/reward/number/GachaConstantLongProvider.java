package xiaojin.gachaaddiction.reward.number;

public record GachaConstantLongProvider(long value) implements IGachaLongProvider {
    @Override
    public long get(GachaNumberContext context) {
        return value;
    }

    @Override
    public IGachaNumberExpression expression() {
        return new IGachaNumberExpression.LongConstant(value);
    }
}

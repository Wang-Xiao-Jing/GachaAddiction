package xiaojin.gachaaddiction.reward.number;

@FunctionalInterface
public interface GachaLongProvider {
    long get(GachaNumberContext context);

    default GachaNumberExpression expression() {
        return GachaNumberExpression.Unavailable.INSTANCE;
    }
}

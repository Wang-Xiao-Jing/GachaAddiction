package xiaojin.gachaaddiction.reward.number;

@FunctionalInterface
public interface GachaDoubleProvider {
    double get(GachaNumberContext context);

    default GachaNumberExpression expression() {
        return GachaNumberExpression.Unavailable.INSTANCE;
    }
}

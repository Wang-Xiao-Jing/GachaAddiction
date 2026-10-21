package xiaojin.gachaaddiction.reward.number;

@FunctionalInterface
public interface IGachaDoubleProvider {
    double get(GachaNumberContext context);

    default IGachaNumberExpression expression() {
        return IGachaNumberExpression.Unavailable.INSTANCE;
    }
}

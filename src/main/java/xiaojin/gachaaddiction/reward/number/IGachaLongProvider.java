package xiaojin.gachaaddiction.reward.number;

@FunctionalInterface
public interface IGachaLongProvider {
    long get(GachaNumberContext context);

    default IGachaNumberExpression expression() {
        return IGachaNumberExpression.Unavailable.INSTANCE;
    }
}

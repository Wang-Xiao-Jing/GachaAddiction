package xiaojin.gachaaddiction.cardpool.transaction;

@FunctionalInterface
public interface GachaTransactionEffects {
    default void validate(GachaTransactionContext context) {
    }

    default boolean canRecover(GachaTransactionContext context) {
        return true;
    }

    void apply(GachaTransactionContext context, GachaTransactionResult result);
}

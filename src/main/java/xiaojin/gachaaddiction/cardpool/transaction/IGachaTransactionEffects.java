package xiaojin.gachaaddiction.cardpool.transaction;

@FunctionalInterface
public interface IGachaTransactionEffects {
    default void validate(GachaTransactionContext context) {
    }

    default boolean canRecover(GachaTransactionContext context) {
        return true;
    }

    void apply(GachaTransactionContext context, GachaTransactionResult result);
}

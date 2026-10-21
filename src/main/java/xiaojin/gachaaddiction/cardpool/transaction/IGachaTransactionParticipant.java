package xiaojin.gachaaddiction.cardpool.transaction;

public interface IGachaTransactionParticipant {
    void prepareCommit(GachaTransactionContext context, GachaTransactionResult result);

    void commit(GachaTransactionContext context, GachaTransactionResult result);

    void rollback(GachaTransactionContext context);

    void recover(GachaTransactionContext context, GachaTransactionResult result,
                 GachaTransactionStage durableStage);
}

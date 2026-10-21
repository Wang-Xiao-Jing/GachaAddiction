package xiaojin.gachaaddiction.cardpool.transaction;

public enum GachaTransactionStage {
    PREPARED,
    RESERVED,
    REWARD_COMPUTED,
    COMMITTED,
    DELIVERED,
    ROLLED_BACK
}

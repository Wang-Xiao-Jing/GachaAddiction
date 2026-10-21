package xiaojin.gachaaddiction.cardpool.transaction;

public enum GachaRewardDeliveryCapability {
    IDEMPOTENT_BY_TRANSACTION,
    DURABLE_BEFORE_ACKNOWLEDGEMENT,
    SUPPORTS_OFFLINE_PLAYER
}

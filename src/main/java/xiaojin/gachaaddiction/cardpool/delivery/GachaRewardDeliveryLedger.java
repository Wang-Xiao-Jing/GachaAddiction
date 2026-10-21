package xiaojin.gachaaddiction.cardpool.delivery;

import xiaojin.gachaaddiction.cardpool.transaction.GachaTransactionId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GachaRewardDeliveryLedger {
    Optional<GachaRewardDeliveryLedgerEntry> find(UUID playerId, GachaTransactionId transactionId);

    List<GachaRewardDeliveryLedgerEntry> pending(UUID playerId);

    void store(GachaRewardDeliveryLedgerEntry entry);
}

package xiaojin.gachaaddiction.cardpool.delivery;

import net.minecraft.world.item.ItemStack;
import xiaojin.gachaaddiction.cardpool.transaction.GachaRewardDelivery;
import xiaojin.gachaaddiction.cardpool.transaction.GachaTransactionContext;
import xiaojin.gachaaddiction.cardpool.transaction.GachaTransactionId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GachaRewardInbox extends GachaRewardDelivery {
    default void deliver(GachaTransactionContext context, List<ItemStack> rewards,
                         GachaRewardInboxEntryKind kind) {
        if (kind != GachaRewardInboxEntryKind.REWARD) {
            throw new UnsupportedOperationException("This reward inbox does not support entry kind: " + kind);
        }
        deliver(context, rewards);
    }

    Optional<GachaRewardInboxEntry> find(UUID playerId, GachaTransactionId transactionId);

    List<GachaRewardInboxEntry> pending(UUID playerId);

    Optional<GachaRewardInboxEntry> take(UUID playerId, GachaTransactionId transactionId);

    void restore(GachaRewardInboxEntry entry);
}

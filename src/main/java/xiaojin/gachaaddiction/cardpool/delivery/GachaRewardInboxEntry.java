package xiaojin.gachaaddiction.cardpool.delivery;

import net.minecraft.world.item.ItemStack;
import xiaojin.gachaaddiction.cardpool.transaction.GachaTransactionId;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record GachaRewardInboxEntry(GachaTransactionId transactionId, UUID playerId,
                                    GachaRewardInboxEntryKind kind, List<ItemStack> rewards) {
    public GachaRewardInboxEntry {
        Objects.requireNonNull(transactionId, "transactionId");
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(kind, "kind");
        rewards = rewards.stream().map(ItemStack::copy).toList();
    }

    @Override
    public List<ItemStack> rewards() {
        return rewards.stream().map(ItemStack::copy).toList();
    }
}

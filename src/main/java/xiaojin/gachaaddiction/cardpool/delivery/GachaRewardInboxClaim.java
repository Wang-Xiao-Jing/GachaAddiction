package xiaojin.gachaaddiction.cardpool.delivery;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import xiaojin.gachaaddiction.cardpool.transaction.GachaTransactionId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class GachaRewardInboxClaim {
    private GachaRewardInboxClaim() {
    }

    public static int claim(ServerPlayer player, GachaRewardInbox inbox, GachaTransactionId transactionId) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(inbox, "inbox");
        GachaRewardInboxEntry entry = inbox.take(player.getUUID(), Objects.requireNonNull(
                transactionId, "transactionId")).orElseThrow(() ->
                new IllegalStateException("Reward inbox entry does not exist"));
        List<ItemStack> rewards = entry.rewards();
        List<Boolean> delivered = new ArrayList<>(Collections.nCopies(rewards.size(), false));
        for (int index = 0; index < rewards.size(); index++) {
            ItemStack remainder = rewards.get(index).copy();
            try {
                player.getInventory().add(remainder);
                if (remainder.isEmpty()) {
                    delivered.set(index, true);
                }
                if (!remainder.isEmpty()) {
                    ItemEntity drop = player.drop(remainder, false);
                    if (drop == null) {
                        throw new IllegalStateException("Unable to deliver a reward stack");
                    }
                    delivered.set(index, true);
                    drop.setNoPickUpDelay();
                    drop.setTarget(player.getUUID());
                }
            } catch (RuntimeException failure) {
                List<ItemStack> undelivered = new ArrayList<>();
                if (!delivered.get(index) && !remainder.isEmpty()) {
                    undelivered.add(remainder);
                }
                for (int remainingIndex = index + 1; remainingIndex < rewards.size(); remainingIndex++) {
                    if (!delivered.get(remainingIndex)) {
                        undelivered.add(rewards.get(remainingIndex));
                    }
                }
                if (!undelivered.isEmpty()) {
                    inbox.restore(new GachaRewardInboxEntry(entry.transactionId(), entry.playerId(), entry.kind(),
                            undelivered));
                }
                throw failure;
            }
        }
        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
        return rewards.size();
    }
}

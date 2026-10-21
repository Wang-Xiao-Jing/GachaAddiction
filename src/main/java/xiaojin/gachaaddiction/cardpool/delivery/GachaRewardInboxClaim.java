package xiaojin.gachaaddiction.cardpool.delivery;

import net.minecraft.server.level.ServerPlayer;
import xiaojin.gachaaddiction.cardpool.transaction.GachaRewardDeliveryServices;
import xiaojin.gachaaddiction.cardpool.transaction.GachaPlayerRewardDeliveryHandler;
import xiaojin.gachaaddiction.cardpool.transaction.GachaTransactionId;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.Optional;
import java.util.Objects;

public final class GachaRewardInboxClaim {
    private GachaRewardInboxClaim() {
    }

    public static GachaTransactionId transactionIdFor(UUID playerId, GachaTransactionId inboxEntryId) {
        String identity = "gacha-inbox-claim:" + Objects.requireNonNull(playerId, "playerId")
                + ":" + Objects.requireNonNull(inboxEntryId, "inboxEntryId").value();
        return new GachaTransactionId(UUID.nameUUIDFromBytes(identity.getBytes(StandardCharsets.UTF_8)));
    }

    public static int claim(ServerPlayer player, GachaRewardDeliveryServices services,
                            GachaTransactionId transactionId) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(services, "services");
        Objects.requireNonNull(transactionId, "transactionId");
        if (!services.server().isSameThread()) {
            throw new IllegalStateException("Inbox claim requires the server thread");
        }

        var source = services.rewardInbox().find(player.getUUID(), transactionId);
        if (source.isEmpty()) {
            var committed = services.deliveryLedger().find(player.getUUID(),
                    transactionIdFor(player.getUUID(), transactionId));
            if (committed.isPresent() && committed.get().stage() == GachaRewardDeliveryLedgerStage.DELIVERED
                    && committed.get().inboxEntryId().equals(Optional.of(transactionId))) {
                return committed.get().rewards().size();
            }
            throw new IllegalStateException("Reward inbox entry does not exist");
        }
        return GachaPlayerRewardDeliveryHandler.claimInbox(player, source.get(), services);
    }
}

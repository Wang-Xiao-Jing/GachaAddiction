package xiaojin.gachaaddiction.cardpool.transaction;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import xiaojin.gachaaddiction.cardpool.delivery.GachaRewardDeliveryLedger;
import xiaojin.gachaaddiction.cardpool.delivery.GachaRewardInbox;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record GachaRewardDeliveryServices(MinecraftServer server, GachaRewardInbox rewardInbox,
                                          GachaRewardDeliveryLedger deliveryLedger) {
    public GachaRewardDeliveryServices {
        Objects.requireNonNull(server, "server");
        Objects.requireNonNull(rewardInbox, "rewardInbox");
        Objects.requireNonNull(deliveryLedger, "deliveryLedger");
    }

    public Optional<ServerPlayer> player(UUID playerId) {
        return Optional.ofNullable(server.getPlayerList().getPlayer(Objects.requireNonNull(playerId, "playerId")));
    }

    public void reconcilePlayer(ServerPlayer player) {
        GachaPlayerRewardDeliveryHandler.reconcilePlayer(player, this);
    }

    public void acknowledgePlayerSave(ServerPlayer player) {
        GachaPlayerRewardDeliveryHandler.acknowledgePlayerSave(player, this);
    }
}

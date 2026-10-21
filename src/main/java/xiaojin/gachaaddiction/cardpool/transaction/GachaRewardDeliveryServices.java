package xiaojin.gachaaddiction.cardpool.transaction;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import xiaojin.gachaaddiction.cardpool.delivery.IGachaRewardDeliveryLedger;
import xiaojin.gachaaddiction.cardpool.delivery.IGachaRewardInbox;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record GachaRewardDeliveryServices(MinecraftServer server, IGachaRewardInbox rewardInbox,
                                          IGachaRewardDeliveryLedger deliveryLedger) {
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

}

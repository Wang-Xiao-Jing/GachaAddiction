package xiaojin.gachaaddiction.resourcepack;

import net.minecraft.network.protocol.common.ClientboundResourcePackPopPacket;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class GachaVanillaResourcePackDispatcher {
    public UUID push(Collection<ServerPlayer> players, GachaResourcePackDescriptor descriptor) {
        Objects.requireNonNull(players, "players");
        Objects.requireNonNull(descriptor, "descriptor");
        UUID requestId = UUID.randomUUID();
        ClientboundResourcePackPushPacket packet = new ClientboundResourcePackPushPacket(requestId,
                descriptor.url(), descriptor.sha1(), descriptor.required(), descriptor.prompt());
        players.forEach(player -> Objects.requireNonNull(player, "player").connection.send(packet));
        return requestId;
    }

    public void pop(Collection<ServerPlayer> players, UUID requestId) {
        Objects.requireNonNull(players, "players");
        Objects.requireNonNull(requestId, "requestId");
        ClientboundResourcePackPopPacket packet = new ClientboundResourcePackPopPacket(Optional.of(requestId));
        players.forEach(player -> Objects.requireNonNull(player, "player").connection.send(packet));
    }
}

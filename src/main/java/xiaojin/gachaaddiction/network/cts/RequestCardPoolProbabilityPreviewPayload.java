package xiaojin.gachaaddiction.network.cts;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import xiaojin.gachaaddiction.GachaConstants;
import xiaojin.gachaaddiction.cardpool.GachaServerRuntimeManager;
import xiaojin.gachaaddiction.network.IToServerPayload;
import xiaojin.gachaaddiction.network.stc.OpenCardPoolProbabilityPreviewPayload;

import java.util.UUID;

public record RequestCardPoolProbabilityPreviewPayload(UUID token) implements IToServerPayload {
    public static final Type<RequestCardPoolProbabilityPreviewPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(GachaConstants.MODID, "request_card_pool_probability_preview"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestCardPoolProbabilityPreviewPayload> STREAM_CODEC =
            StreamCodec.composite(UUIDUtil.STREAM_CODEC, RequestCardPoolProbabilityPreviewPayload::token,
                    RequestCardPoolProbabilityPreviewPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void work(IPayloadContext context, ServerPlayer player) {
        GachaServerRuntimeManager.current()
                .flatMap(runtime -> runtime.pendingDraws().peek(token, player.getUUID()))
                .ifPresent(prepared -> {
                    var probabilities = prepared.preview().execution().probabilityDescriptions().stream()
                            .map(description -> new OpenCardPoolProbabilityPreviewPayload.Probability(
                                    description.id(), description.selectionMode().name(),
                                    description.selectionCount().toString(), description.candidates().stream()
                                    .map(candidate -> new OpenCardPoolProbabilityPreviewPayload.Candidate(
                                            candidate.id(), candidate.weight().toString()))
                                    .toList()))
                            .toList();
                    PacketDistributor.sendToPlayer(player, new OpenCardPoolProbabilityPreviewPayload(token, probabilities));
                });
    }
}

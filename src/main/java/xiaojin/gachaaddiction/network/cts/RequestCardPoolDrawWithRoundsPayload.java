package xiaojin.gachaaddiction.network.cts;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import xiaojin.gachaaddiction.GachaConstants;
import xiaojin.gachaaddiction.cardpool.GachaPublicCardPoolEndpoint;
import xiaojin.gachaaddiction.network.IToServerPayload;
import xiaojin.gachaaddiction.network.stc.OpenCardPoolDrawConfirmationPayload;
import xiaojin.gachaaddiction.util.TranslationKeys;

public record RequestCardPoolDrawWithRoundsPayload(
        ResourceLocation cardPoolId,
        ResourceLocation packageId,
        CompoundTag playerChoices,
        int rounds
) implements IToServerPayload {
    public static final Type<RequestCardPoolDrawWithRoundsPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(GachaConstants.MODID, "request_card_pool_draw_with_rounds"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestCardPoolDrawWithRoundsPayload> STREAM_CODEC =
            StreamCodec.composite(ResourceLocation.STREAM_CODEC, RequestCardPoolDrawWithRoundsPayload::cardPoolId,
                    ResourceLocation.STREAM_CODEC, RequestCardPoolDrawWithRoundsPayload::packageId,
                    ByteBufCodecs.COMPOUND_TAG, RequestCardPoolDrawWithRoundsPayload::playerChoices,
                    ByteBufCodecs.VAR_INT, RequestCardPoolDrawWithRoundsPayload::rounds,
                    RequestCardPoolDrawWithRoundsPayload::new);

    public RequestCardPoolDrawWithRoundsPayload {
        playerChoices = playerChoices.copy();
        if (rounds <= 0) {
            throw new IllegalArgumentException("Rounds must be positive");
        }
    }

    @Override
    public CompoundTag playerChoices() {
        return playerChoices.copy();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void work(IPayloadContext context, ServerPlayer player) {
        try {
            var prepared = GachaPublicCardPoolEndpoint.prepare(player, cardPoolId, packageId, rounds, playerChoices);
            PacketDistributor.sendToPlayer(player, new OpenCardPoolDrawConfirmationPayload(prepared.token(),
                    prepared.cardPoolId(), prepared.packageId(), prepared.titleTranslationKey(),
                    prepared.preview().execution().steps(), prepared.preview().execution().issues(),
                    prepared.expiresAt().toEpochMilli()));
        } catch (RuntimeException failure) {
            GachaConstants.LOGGER.error("Public card pool draw preparation with rounds failed for player {}",
                    player.getUUID(), failure);
            player.sendSystemMessage(Component.translatable(TranslationKeys.GUI_GACHA_DRAW_FAILED));
        }
    }
}

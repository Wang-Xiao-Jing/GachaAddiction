package xiaojin.gachaaddiction.network.cts;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import xiaojin.gachaaddiction.GachaAddiction;
import xiaojin.gachaaddiction.network.IToServerPayload;
import xiaojin.gachaaddiction.util.GachaPresentations;

import java.util.UUID;

public record GachaPresentationControlPayload(UUID token, boolean completed) implements IToServerPayload {
    public static final Type<GachaPresentationControlPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(GachaAddiction.MODID, "presentation_control"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GachaPresentationControlPayload> STREAM_CODEC =
            StreamCodec.composite(UUIDUtil.STREAM_CODEC, GachaPresentationControlPayload::token,
                    ByteBufCodecs.BOOL, GachaPresentationControlPayload::completed, GachaPresentationControlPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }


    @Override
    public void work(IPayloadContext context, ServerPlayer player) {
        GachaPresentations.finish(player, token, completed);

    }
}

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
import xiaojin.gachaaddiction.item.GachaItemDraws;
import xiaojin.gachaaddiction.network.IToServerPayload;

import java.util.UUID;

public record FinishGachaItemDrawPayload(UUID session, boolean cancel) implements IToServerPayload {
    public static final Type<FinishGachaItemDrawPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(GachaAddiction.MODID, "finish_item_draw"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FinishGachaItemDrawPayload> STREAM_CODEC =
            StreamCodec.composite(UUIDUtil.STREAM_CODEC, FinishGachaItemDrawPayload::session,
                    ByteBufCodecs.BOOL, FinishGachaItemDrawPayload::cancel, FinishGachaItemDrawPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }


    @Override
    public void work(IPayloadContext context, ServerPlayer player) {
        GachaItemDraws.finish(player, session, cancel);
    }
}


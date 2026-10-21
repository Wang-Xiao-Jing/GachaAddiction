package xiaojin.gachaaddiction.network.cts;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import xiaojin.gachaaddiction.GachaConstants;
import xiaojin.gachaaddiction.cardpool.GachaPublicCardPoolCatalog;
import xiaojin.gachaaddiction.network.IToServerPayload;
import xiaojin.gachaaddiction.network.stc.OpenCardPoolCatalogPayload;

public record RequestCardPoolCatalogPayload() implements IToServerPayload {
    public static final RequestCardPoolCatalogPayload INSTANCE = new RequestCardPoolCatalogPayload();
    public static final Type<RequestCardPoolCatalogPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(GachaConstants.MODID, "request_card_pool_catalog"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestCardPoolCatalogPayload> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void work(IPayloadContext context, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player,
                new OpenCardPoolCatalogPayload(GachaPublicCardPoolCatalog.visibleTo(player)));
    }
}

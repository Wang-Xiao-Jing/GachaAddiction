package xiaojin.gachaaddiction.network.stc;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.GachaConstants;
import xiaojin.gachaaddiction.cardpool.GachaCardPoolCatalogEntry;
import xiaojin.gachaaddiction.network.GachaCardPoolCatalogCodecs;
import xiaojin.gachaaddiction.network.IToClientPayload;

import java.util.List;

public record OpenCardPoolCatalogPayload(List<GachaCardPoolCatalogEntry> cardPools)
        implements IToClientPayload {
    public static final Type<OpenCardPoolCatalogPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(GachaConstants.MODID, "open_card_pool_catalog"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenCardPoolCatalogPayload> STREAM_CODEC =
            StreamCodec.composite(GachaCardPoolCatalogCodecs.CARD_POOL.apply(ByteBufCodecs.list(
                            GachaCardPoolCatalogCodecs.MAXIMUM_CARD_POOLS)),
                    OpenCardPoolCatalogPayload::cardPools, OpenCardPoolCatalogPayload::new);

    public OpenCardPoolCatalogPayload {
        cardPools = List.copyOf(cardPools);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}

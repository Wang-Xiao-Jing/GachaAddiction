package xiaojin.gachaaddiction.network.stc;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import xiaojin.gachaaddiction.GachaAddiction;
import xiaojin.gachaaddiction.cardpool.GachaCardPoolCatalogEntry;
import xiaojin.gachaaddiction.client.GachaPresentationClient;
import xiaojin.gachaaddiction.client.gui.screen.CardPoolSelectionScreen;
import xiaojin.gachaaddiction.network.GachaCardPoolCatalogCodecs;
import xiaojin.gachaaddiction.network.IToClientPayload;

import java.util.List;

public record OpenCardPoolCatalogPayload(List<GachaCardPoolCatalogEntry> cardPools)
        implements IToClientPayload {
    public static final Type<OpenCardPoolCatalogPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(GachaAddiction.MODID, "open_card_pool_catalog"));
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

    @Override
    public void work(IPayloadContext context, AbstractClientPlayer player) {
        GachaPresentationClient.rememberCardPools(cardPools);
        Minecraft.getInstance().setScreen(new CardPoolSelectionScreen(cardPools));
    }
}

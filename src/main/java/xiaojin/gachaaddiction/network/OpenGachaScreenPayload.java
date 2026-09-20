package xiaojin.gachaaddiction.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import xiaojin.gachaaddiction.GachaAddiction;
import xiaojin.gachaaddiction.api.GachaType;
import xiaojin.gachaaddiction.api.ItemStackEntry;

import java.util.List;
import java.util.Optional;

public record OpenGachaScreenPayload(
        Optional<GachaType> gachaType,
        List<ResourceLocation> lootTableIds,
        List<ItemStackEntry> entries,
        List<ItemStack> rewards
) implements CustomPacketPayload, ToClientPayload {

    public static final CustomPacketPayload.Type<OpenGachaScreenPayload> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(GachaAddiction.MODID, "open_gacha_screen"));

    private static final StreamCodec<ByteBuf, List<ResourceLocation>> LOOT_TABLE_ID_STREAM_CODEC =
            ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list());

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenGachaScreenPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(GachaType.STREAM_CODEC), OpenGachaScreenPayload::gachaType,
            LOOT_TABLE_ID_STREAM_CODEC, OpenGachaScreenPayload::lootTableIds,
            ItemStackEntry.LIST_STREAM_CODEC, OpenGachaScreenPayload::entries,
            ItemStack.LIST_STREAM_CODEC, OpenGachaScreenPayload::rewards,
            OpenGachaScreenPayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

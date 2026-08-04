package xiaojin.gachaaddiction.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import xiaojin.gachaaddiction.GachaAddiction;
import xiaojin.gachaaddiction.GachaAddictionConfig;
import xiaojin.gachaaddiction.api.GachaType;
import xiaojin.gachaaddiction.api.ItemStackEntry;
import xiaojin.gachaaddiction.init.client.GachaTypeClients;

import java.util.List;
import java.util.Optional;

public record OpenGachaScreenPayload(
        Optional<GachaType> gachaType,
        List<ResourceLocation> lootTableIds,
        List<ItemStackEntry> entries,
        List<ItemStack> rewards
) implements CustomPacketPayload {

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

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            GachaType gachaType;
            if (this.gachaType.isPresent()) {
                if (this.gachaType.get().isEmpty()) {
                    return;
                }
                gachaType = this.gachaType.get();
            } else {
                gachaType = GachaAddictionConfig.CLIENT.getDefaultGachaaType();
            }
            var client = GachaTypeClients.get(gachaType);
            if (client == null) {
                return;
            }
            var screen = client.open(lootTableIds, entries);
            NonNullList<ItemStack> items = NonNullList.of(ItemStack.EMPTY, rewards.toArray(ItemStack[]::new));
            screen.updateRewards(items);
        });
    }
}

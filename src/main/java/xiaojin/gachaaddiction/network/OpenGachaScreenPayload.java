package xiaojin.gachaaddiction.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import xiaojin.gachaaddiction.GachaAddiction;
import xiaojin.gachaaddiction.GachaAddictionConfig;
import xiaojin.gachaaddiction.api.GachaType;
import xiaojin.gachaaddiction.api.ItemStackEntry;
import xiaojin.gachaaddiction.init.client.GachaTypeClients;

import java.util.List;
import java.util.Optional;

public record OpenGachaScreenPayload(
        Optional<GachaType> gachaType,
        List<Identifier> lootTableIds,
        List<ItemStackEntry> entries,
        List<ItemStack> rewards
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<OpenGachaScreenPayload> TYPE = new CustomPacketPayload.Type<>(
            Identifier.fromNamespaceAndPath(GachaAddiction.MODID, "open_gacha_screen"));

    private static final StreamCodec<ByteBuf, List<Identifier>> LOOT_TABLE_ID_STREAM_CODEC =
            Identifier.STREAM_CODEC.apply(ByteBufCodecs.list());

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenGachaScreenPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(GachaType.STREAM_CODEC), OpenGachaScreenPayload::gachaType,
            LOOT_TABLE_ID_STREAM_CODEC, OpenGachaScreenPayload::lootTableIds,
            ItemStackEntry.LIST_STREAM_CODEC, OpenGachaScreenPayload::entries,
            ItemStack.OPTIONAL_LIST_STREAM_CODEC, OpenGachaScreenPayload::rewards,
            OpenGachaScreenPayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /** 在客户端注册处理器；服务端不会加载到这里的客户端类。 */
    @Environment(EnvType.CLIENT)
    public static void registerClientReceiver() {
        ClientPlayNetworking.registerGlobalReceiver(TYPE, (payload, context) ->
                context.client().execute(() -> payload.handle()));
    }

    @Environment(EnvType.CLIENT)
    private void handle() {
        {
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
        }
    }
}

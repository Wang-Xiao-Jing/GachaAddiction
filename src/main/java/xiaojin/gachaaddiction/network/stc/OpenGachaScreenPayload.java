package xiaojin.gachaaddiction.network.stc;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import xiaojin.gachaaddiction.GachaAddiction;
import xiaojin.gachaaddiction.api.GachaAnnouncement;
import xiaojin.gachaaddiction.api.GachaType;
import xiaojin.gachaaddiction.api.ItemStackEntry;
import xiaojin.gachaaddiction.client.GachaAnnouncementClient;
import xiaojin.gachaaddiction.client.GachaPresentationClient;
import xiaojin.gachaaddiction.config.GachaAddictionConfig;
import xiaojin.gachaaddiction.network.IToClientPayload;
import xiaojin.gachaaddiction.registry.client.GachaTypeClients;

import java.util.List;
import java.util.Optional;

public record OpenGachaScreenPayload(
        Optional<GachaType> gachaType,
        List<ResourceLocation> lootTableIds,
        List<ItemStackEntry> entries,
        List<ItemStack> rewards
) implements IToClientPayload {

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

    @Override
    public void work(IPayloadContext context, AbstractClientPlayer player) {
        GachaAnnouncementClient.INSTANCE.rewards(GachaAnnouncement.EXPLICIT_REWARDS_MENU, rewards);
        GachaType gachaType;
        gachaType = this.gachaType.orElseGet(GachaAddictionConfig.CLIENT::getDefaultGachaaType);
        gachaType = GachaPresentationClient.animation(-1, gachaType);
        var client = GachaTypeClients.get(gachaType);
        if (client == null) {
            GachaAnnouncementClient.INSTANCE.presentation(null);
            return;
        }
        var screen = GachaPresentationClient.apply(
                client.create(Minecraft.getInstance().screen, lootTableIds, entries), -1, gachaType);
        Minecraft.getInstance().setScreen(screen);
        NonNullList<ItemStack> items = NonNullList.of(ItemStack.EMPTY, rewards.toArray(ItemStack[]::new));
        screen.updateRewards(items);
        GachaAnnouncementClient.INSTANCE.presentation(Minecraft.getInstance().screen);
    }
}

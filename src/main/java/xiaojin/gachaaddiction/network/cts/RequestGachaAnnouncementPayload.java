package xiaojin.gachaaddiction.network.cts;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import xiaojin.gachaaddiction.GachaConstants;
import xiaojin.gachaaddiction.api.GachaAnnouncement;
import xiaojin.gachaaddiction.api.IGachaAnnouncementApi.Trigger;
import xiaojin.gachaaddiction.network.IToServerPayload;
import xiaojin.gachaaddiction.announcement.GachaAnnouncements;

import java.util.List;
import java.util.UUID;

public record RequestGachaAnnouncementPayload(UUID token, boolean manual, List<ItemStack> items)
        implements IToServerPayload {
    public static final Type<RequestGachaAnnouncementPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(GachaConstants.MODID, "request_announcement"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestGachaAnnouncementPayload> STREAM_CODEC =
            StreamCodec.composite(UUIDUtil.STREAM_CODEC, RequestGachaAnnouncementPayload::token,
                    ByteBufCodecs.BOOL, RequestGachaAnnouncementPayload::manual,
                    ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list(GachaAnnouncement.MAXIMUM_ITEMS)),
                    RequestGachaAnnouncementPayload::items, RequestGachaAnnouncementPayload::new);

    public RequestGachaAnnouncementPayload {
        items = items.stream().map(ItemStack::copy).toList();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void work(IPayloadContext context, ServerPlayer player) {
        if (items.isEmpty()) {
            GachaAnnouncements.close(player, token);
            return;
        }
        GachaAnnouncements.request(player, token, items, manual ? Trigger.MANUAL : Trigger.AUTOMATIC);
    }
}

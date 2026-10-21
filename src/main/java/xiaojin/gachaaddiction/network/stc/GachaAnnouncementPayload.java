package xiaojin.gachaaddiction.network.stc;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import xiaojin.gachaaddiction.GachaAddiction;
import xiaojin.gachaaddiction.api.GachaAnnouncement;
import xiaojin.gachaaddiction.client.GachaAnnouncementClient;
import xiaojin.gachaaddiction.network.IToClientPayload;

public record GachaAnnouncementPayload(GachaAnnouncement announcement) implements IToClientPayload {
    public static final Type<GachaAnnouncementPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(GachaAddiction.MODID, "announcement"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GachaAnnouncementPayload> STREAM_CODEC =
            GachaAnnouncement.STREAM_CODEC.map(GachaAnnouncementPayload::new, GachaAnnouncementPayload::announcement);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void work(IPayloadContext context, AbstractClientPlayer player) {
        GachaAnnouncementClient.INSTANCE.receive(announcement);
    }
}

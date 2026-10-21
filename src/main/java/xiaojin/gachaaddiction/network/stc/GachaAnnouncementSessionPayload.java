package xiaojin.gachaaddiction.network.stc;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import xiaojin.gachaaddiction.GachaAddiction;
import xiaojin.gachaaddiction.client.GachaAnnouncementClient;
import xiaojin.gachaaddiction.network.IToClientPayload;

import java.util.UUID;

public record GachaAnnouncementSessionPayload(UUID token, int menuId) implements IToClientPayload {
    public static final Type<GachaAnnouncementSessionPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(GachaAddiction.MODID, "announcement_session"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GachaAnnouncementSessionPayload> STREAM_CODEC =
            StreamCodec.composite(UUIDUtil.STREAM_CODEC, GachaAnnouncementSessionPayload::token,
                    ByteBufCodecs.VAR_INT, GachaAnnouncementSessionPayload::menuId,
                    GachaAnnouncementSessionPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void work(IPayloadContext context, AbstractClientPlayer player) {
        GachaAnnouncementClient.INSTANCE.begin(token, menuId);
    }
}

package xiaojin.gachaaddiction.network.stc;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.GachaConstants;
import xiaojin.gachaaddiction.network.IToClientPayload;

import java.util.UUID;

public record GachaAnnouncementSessionPayload(UUID token, int menuId) implements IToClientPayload {
    public static final Type<GachaAnnouncementSessionPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(GachaConstants.MODID, "announcement_session"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GachaAnnouncementSessionPayload> STREAM_CODEC =
            StreamCodec.composite(UUIDUtil.STREAM_CODEC, GachaAnnouncementSessionPayload::token,
                    ByteBufCodecs.VAR_INT, GachaAnnouncementSessionPayload::menuId,
                    GachaAnnouncementSessionPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}

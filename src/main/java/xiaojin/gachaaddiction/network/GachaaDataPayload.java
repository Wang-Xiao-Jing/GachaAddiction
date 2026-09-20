package xiaojin.gachaaddiction.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.GachaAddiction;
import xiaojin.gachaaddiction.api.GachaaData;

import java.util.Optional;

public record GachaaDataPayload(
        int containerId,
        Optional<GachaaData> data
) implements CustomPacketPayload, ToClientPayload {

    public static final CustomPacketPayload.Type<GachaaDataPayload> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(GachaAddiction.MODID, "gachaa_data"));

    public static final StreamCodec<RegistryFriendlyByteBuf, GachaaDataPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, GachaaDataPayload::containerId,
            GachaaData.OPTIONAL_STREAM_CODEC, GachaaDataPayload::data,
            GachaaDataPayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

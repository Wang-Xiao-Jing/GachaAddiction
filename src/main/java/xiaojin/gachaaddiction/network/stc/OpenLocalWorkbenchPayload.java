package xiaojin.gachaaddiction.network.stc;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.GachaConstants;
import xiaojin.gachaaddiction.network.IToClientPayload;

public record OpenLocalWorkbenchPayload() implements IToClientPayload {
    public static final OpenLocalWorkbenchPayload INSTANCE = new OpenLocalWorkbenchPayload();
    public static final Type<OpenLocalWorkbenchPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(GachaConstants.MODID, "open_local_workbench"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenLocalWorkbenchPayload> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}

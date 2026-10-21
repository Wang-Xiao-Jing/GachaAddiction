package xiaojin.gachaaddiction.network.stc;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import xiaojin.gachaaddiction.GachaAddiction;
import xiaojin.gachaaddiction.client.AdminWorkbenchClient;
import xiaojin.gachaaddiction.network.IToClientPayload;

public record OpenLocalWorkbenchPayload() implements IToClientPayload {
    public static final OpenLocalWorkbenchPayload INSTANCE = new OpenLocalWorkbenchPayload();
    public static final Type<OpenLocalWorkbenchPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(GachaAddiction.MODID, "open_local_workbench"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenLocalWorkbenchPayload> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void work(IPayloadContext context, AbstractClientPlayer player) {
        AdminWorkbenchClient.open();
    }
}

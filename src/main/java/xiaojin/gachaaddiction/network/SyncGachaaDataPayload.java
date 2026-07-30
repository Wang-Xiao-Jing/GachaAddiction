package xiaojin.gachaaddiction.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import xiaojin.gachaaddiction.GachaAddiction;
import xiaojin.gachaaddiction.api.GachaType;
import xiaojin.gachaaddiction.api.GachaaData;
import xiaojin.gachaaddiction.client.gui.screen.BasicGachaScreen;
import xiaojin.gachaaddiction.mixed.IAbstractContainerMenu;
import xiaojin.gachaaddiction.util.client.ModClientUtil;

/**
 * 把容器的抽奖数据同步给客户端。
 * <p>
 * NeoForge 可以把额外数据塞进开界面的数据包里（{@code MenuProvider#writeClientSideData}），
 * 原版 26.2 没有这个通道，所以改成按 containerId 单独发一个包。
 * 服务端先发 {@code ClientboundOpenScreenPacket} 再发本包，
 * 客户端收到时界面已经打开，可以直接替换。
 */
public record SyncGachaaDataPayload(int containerId, GachaaData gachaaData) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncGachaaDataPayload> TYPE = new CustomPacketPayload.Type<>(
            Identifier.fromNamespaceAndPath(GachaAddiction.MODID, "sync_gachaa_data"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncGachaaDataPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncGachaaDataPayload::containerId,
            GachaaData.STREAM_CODEC, SyncGachaaDataPayload::gachaaData,
            SyncGachaaDataPayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Environment(EnvType.CLIENT)
    public static void registerClientReceiver() {
        ClientPlayNetworking.registerGlobalReceiver(TYPE, (payload, context) ->
                context.client().execute(() -> payload.apply(context.client())));
    }

    @Environment(EnvType.CLIENT)
    private void apply(Minecraft minecraft) {
        Screen screen = minecraft.gui.screen();
        if (!(screen instanceof MenuAccess<?> menuAccess)) {
            return;
        }
        if (menuAccess.getMenu().containerId != containerId) {
            return;
        }

        IAbstractContainerMenu.of(menuAccess.getMenu()).gachaaddiction$setGachaaData(gachaaData);

        if (!gachaaData.isOpenGachaaUi()) {
            return;
        }

        GachaType gachaType = gachaaData.getGachaType();
        if (gachaType.isEmpty()) {
            return;
        }

        BasicGachaScreen gachaScreen = gachaType.getClientData().create(screen, gachaaData);
        minecraft.gui.setScreen(gachaScreen);

        // 容器内容包可能已经先到了（那时抽奖界面还不存在，
        // ClientPacketListenerMixin 会直接返回），所以这里补取一次。
        ModClientUtil.applyContainerRewards(gachaScreen, menuAccess.getMenu(), false);
    }
}

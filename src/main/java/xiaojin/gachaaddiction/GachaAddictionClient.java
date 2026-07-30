package xiaojin.gachaaddiction;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import xiaojin.gachaaddiction.init.client.GachaTypeClients;
import xiaojin.gachaaddiction.network.OpenGachaScreenPayload;
import xiaojin.gachaaddiction.network.SyncGachaaDataPayload;
import xiaojin.gachaaddiction.registry.GachaaAdictionConfigData;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class GachaAddictionClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        GachaTypeClients.init();
        OpenGachaScreenPayload.registerClientReceiver();
        SyncGachaaDataPayload.registerClientReceiver();
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES)
                .registerReloadListener(new ConfigDataReloadListener());
    }

    /**
     * Fabric 要求重载监听器带有唯一 id，这里包一层
     * {@link GachaaAdictionConfigData}。
     */
    private static final class ConfigDataReloadListener implements IdentifiableResourceReloadListener {
        private static final Identifier ID =
                Identifier.fromNamespaceAndPath(GachaAddiction.MODID, "config_data");

        @Override
        public Identifier getFabricId() {
            return ID;
        }

        @Override
        public CompletableFuture<Void> reload(PreparableReloadListener.SharedState sharedState,
                                              Executor backgroundExecutor,
                                              PreparableReloadListener.PreparationBarrier barrier,
                                              Executor gameExecutor) {
            return GachaaAdictionConfigData.INSTANCE.reload(sharedState, backgroundExecutor, barrier, gameExecutor);
        }
    }
}

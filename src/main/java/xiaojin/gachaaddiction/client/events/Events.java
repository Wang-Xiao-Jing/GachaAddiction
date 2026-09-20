package xiaojin.gachaaddiction.client.events;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.packs.PackType;
import xiaojin.gachaaddiction.registry.GachaaAdictionConfigData;

@Environment(EnvType.CLIENT)
public class Events {
    private Events() {
    }

    public static void register() {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES)
                .registerReloadListener(GachaaAdictionConfigData.INSTANCE);
    }
}

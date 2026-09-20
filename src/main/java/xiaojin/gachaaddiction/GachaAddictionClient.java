package xiaojin.gachaaddiction;

import fuzs.forgeconfigapiport.fabric.api.v5.client.ConfigScreenFactoryRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import xiaojin.gachaaddiction.client.events.Events;
import xiaojin.gachaaddiction.network.ModClientNetworking;
import xiaojin.gachaaddiction.registry.client.GachaTypeClients;

public class GachaAddictionClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        GachaTypeClients.init();
        ModClientNetworking.register();
        Events.register();
        ConfigScreenFactoryRegistry.INSTANCE.register(GachaAddiction.MODID, ConfigurationScreen::new);
    }
}

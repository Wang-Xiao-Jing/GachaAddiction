package xiaojin.gachaaddiction;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import xiaojin.gachaaddiction.client.compat.ldlib2.LDLib2Util;
import xiaojin.gachaaddiction.registry.client.GachaTypeClients;

@Mod(value = GachaAddiction.MODID, dist = Dist.CLIENT)
public class GachaAddictionClient {
    public GachaAddictionClient(IEventBus modEventBus, ModContainer container) {
        GachaTypeClients.init();
        IConfigScreenFactory iExtensionPoint;
        if (ModList.get().isLoaded("ldlib2")) {
            iExtensionPoint = LDLib2Util.createConfigScreenFactory();
        } else {
            iExtensionPoint = ConfigurationScreen::new;
        }
        container.registerExtensionPoint(IConfigScreenFactory.class, iExtensionPoint);
    }
}

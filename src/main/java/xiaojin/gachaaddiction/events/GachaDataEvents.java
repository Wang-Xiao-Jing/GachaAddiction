package xiaojin.gachaaddiction.events;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import xiaojin.gachaaddiction.GachaConstants;
import xiaojin.gachaaddiction.generator.Datagen;

@EventBusSubscriber(modid = GachaConstants.MODID)
public class GachaDataEvents {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        Datagen.registerProviders(event);
    }
}

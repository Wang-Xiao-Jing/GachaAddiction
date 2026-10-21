package xiaojin.gachaaddiction;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import xiaojin.gachaaddiction.compat.GachaCompatibility;
import xiaojin.gachaaddiction.config.GachaAddictionConfig;
import xiaojin.gachaaddiction.registry.GachaSources;
import xiaojin.gachaaddiction.registry.GachaTypes;
import xiaojin.gachaaddiction.registry.item.GachaDrawItems;
import xiaojin.gachaaddiction.registry.sound.ModSoundEvents;

@Mod(GachaConstants.MODID)
public class GachaAddiction {

    public GachaAddiction(IEventBus modEventBus, ModContainer modContainer) {
        GachaCompatibility.initialize();
        GachaTypes.init();
        GachaAddictionConfig.register(modContainer);
        ModSoundEvents.REGISTRY.register(modEventBus);
        GachaDrawItems.REGISTRY.register(modEventBus);
        GachaSources.register(modEventBus);
    }
}

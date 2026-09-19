package xiaojin.gachaaddiction;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import xiaojin.gachaaddiction.config.GachaAddictionConfig;
import xiaojin.gachaaddiction.event.Events;
import xiaojin.gachaaddiction.network.ModNetworking;
import xiaojin.gachaaddiction.registry.GachaTypes;
import xiaojin.gachaaddiction.registry.sound.ModSoundEvents;

public class GachaAddiction implements ModInitializer {
    public static final String MODID = "gachaaddiction";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final boolean LOOTR_LOADED = FabricLoader.getInstance().isModLoaded("lootr");
    public static final boolean RARITYCORE_LOADED = FabricLoader.getInstance().isModLoaded("raritycore");
    public static final boolean CONFLUENCE_MAGIC_LIB_LOADED = FabricLoader.getInstance().isModLoaded("confluence_magic_lib");

    @Override
    public void onInitialize() {
        GachaAddictionConfig.register();
        GachaTypes.init();
        ModSoundEvents.init();
        ModNetworking.register();
        Events.register();
    }
}

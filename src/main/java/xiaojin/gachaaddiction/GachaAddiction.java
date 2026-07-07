package xiaojin.gachaaddiction;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;
import xiaojin.gachaaddiction.init.ModSoundEvents;

// TODO 提供 ILootrInventory 兼容
@Mod(GachaAddiction.MODID)
public class GachaAddiction {
    public static final String MODID = "gachaaddiction";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final boolean LOOTR_LOADED = ModList.get().isLoaded("lootr");
    public static final boolean RARITYCORE_LOADED = ModList.get().isLoaded("raritycore");
    public static final boolean CONFLUENCE_MAGIC_LIB_LOADED = ModList.get().isLoaded("confluence_magic_lib");

    public GachaAddiction(IEventBus modEventBus, ModContainer modContainer) {
        NeoForge.EVENT_BUS.register(this);
        ModSoundEvents.REGISTRY.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, GachaAddictionConfig.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("HELLO from server starting");
    }
}

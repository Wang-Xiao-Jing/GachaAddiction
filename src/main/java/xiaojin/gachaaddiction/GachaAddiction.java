package xiaojin.gachaaddiction;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.slf4j.Logger;
import xiaojin.gachaaddiction.init.ModSoundEvents;
import xiaojin.gachaaddiction.network.OpenGachaScreenPayload;

@Mod(GachaAddiction.MODID)
public final class GachaAddiction {
    public static final String MODID = "gachaaddiction";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final boolean LOOTR_LOADED = ModList.get().isLoaded("lootr");
    public static final boolean RARITYCORE_LOADED = ModList.get().isLoaded("raritycore");
    public static final boolean CONFLUENCE_MAGIC_LIB_LOADED = ModList.get().isLoaded("confluence_magic_lib");

    public GachaAddiction(IEventBus modEventBus, ModContainer modContainer) {
        GachaAddictionConfig.register(modContainer);
        ModSoundEvents.REGISTRY.register(modEventBus);
        modEventBus.addListener(RegisterPayloadHandlersEvent.class, event -> {
            var registrar = event.registrar("1");
            registrar.playToClient(
                    OpenGachaScreenPayload.TYPE,
                    OpenGachaScreenPayload.STREAM_CODEC,
                    OpenGachaScreenPayload::handle
            );
        });
    }
}

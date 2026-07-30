package xiaojin.gachaaddiction;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import xiaojin.gachaaddiction.events.Events;
import xiaojin.gachaaddiction.init.GachaTypes;
import xiaojin.gachaaddiction.init.ModSoundEvents;
import xiaojin.gachaaddiction.network.OpenGachaScreenPayload;
import xiaojin.gachaaddiction.network.SyncGachaaDataPayload;

public final class GachaAddiction implements ModInitializer {
    public static final String MODID = "gachaaddiction";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final boolean LOOTR_LOADED = FabricLoader.getInstance().isModLoaded("lootr");

    @Override
    public void onInitialize() {
        GachaTypes.init();
        GachaAddictionConfig.register();
        ModSoundEvents.init();
        PayloadTypeRegistry.clientboundPlay().register(OpenGachaScreenPayload.TYPE, OpenGachaScreenPayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SyncGachaaDataPayload.TYPE, SyncGachaaDataPayload.STREAM_CODEC);
        Events.register();
    }
}

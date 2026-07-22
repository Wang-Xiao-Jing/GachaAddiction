package xiaojin.gachaaddiction;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import xiaojin.gachaaddiction.config.*;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class GachaAddictionConfig {
    public static final ClientConfig CLIENT;
    public static final ServerConfig SERVER;
    public static final CommonConfig COMMON;

    private static final ModConfigSpec CLIENT_SPEC;
    private static final ModConfigSpec SERVER_SPEC;
    private static final ModConfigSpec COMMON_SPEC;

    static {
        var clientPair = new ModConfigSpec.Builder().configure(ClientConfig::new);
        CLIENT = clientPair.getKey();
        CLIENT_SPEC = clientPair.getValue();

        var serverPair = new ModConfigSpec.Builder().configure(ServerConfig::new);
        SERVER = serverPair.getKey();
        SERVER_SPEC = serverPair.getValue();

        var commonPair = new ModConfigSpec.Builder().configure(CommonConfig::new);
        COMMON = commonPair.getKey();
        COMMON_SPEC = commonPair.getValue();
    }

    private GachaAddictionConfig() {
    }

    public static void register(ModContainer modContainer) {
        GachaAddiction.LOGGER.info("Initialize the Gacha Addiction config files");
        modContainer.registerConfig(ModConfig.Type.COMMON, COMMON_SPEC);
        modContainer.registerConfig(ModConfig.Type.SERVER, SERVER_SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC);
    }
}

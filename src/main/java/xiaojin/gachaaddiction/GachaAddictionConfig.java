package xiaojin.gachaaddiction;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import xiaojin.gachaaddiction.config.GachaAddictionClientConfig;
import xiaojin.gachaaddiction.config.GachaAddictionCommonConfig;
import xiaojin.gachaaddiction.config.GachaAddictionServerConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public final class GachaAddictionConfig {
    public static final GachaAddictionClientConfig CLIENT;
    public static final GachaAddictionServerConfig SERVER;
    public static final GachaAddictionCommonConfig COMMON;
    public static final ModConfigSpec CLIENT_SPEC;
    public static final ModConfigSpec SERVER_SPEC;
    public static final ModConfigSpec COMMON_SPEC;

    static {
        Pair<GachaAddictionClientConfig, ModConfigSpec> clientPair = new ModConfigSpec.Builder().configure(GachaAddictionClientConfig::new);
        CLIENT = clientPair.getKey();
        CLIENT_SPEC = clientPair.getValue();

        Pair<GachaAddictionServerConfig, ModConfigSpec> serverPair = new ModConfigSpec.Builder().configure(GachaAddictionServerConfig::new);
        SERVER = serverPair.getKey();
        SERVER_SPEC = serverPair.getValue();

        Pair<GachaAddictionCommonConfig, ModConfigSpec> commonPair = new ModConfigSpec.Builder().configure(GachaAddictionCommonConfig::new);
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

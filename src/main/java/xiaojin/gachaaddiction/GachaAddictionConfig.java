package xiaojin.gachaaddiction;

import xiaojin.gachaaddiction.config.ClientConfig;
import xiaojin.gachaaddiction.config.ConfigSpec;

public final class GachaAddictionConfig {
    public static final ClientConfig CLIENT;

    private static final ConfigSpec CLIENT_SPEC;

    static {
        CLIENT_SPEC = new ConfigSpec(GachaAddiction.MODID + "-client");
        CLIENT = new ClientConfig(CLIENT_SPEC);
    }

    private GachaAddictionConfig() {
    }

    public static void register() {
        GachaAddiction.LOGGER.info("Initialize the Gacha Addiction config files");
        CLIENT_SPEC.load();
    }

    public static void save() {
        CLIENT_SPEC.save();
    }
}

package xiaojin.gachaaddiction.config;

import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import xiaojin.gachaaddiction.GachaAddiction;
import xiaojin.gachaaddiction.config.entry.BasicConfigEntry;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * 配置入口：构建三端 ModConfigSpec 并注册到 NeoForge
 * （保留 NeoForge 的加载/保存/服务器同步能力）。
 */
public class GachaAddictionConfig {
    public static final ClientConfig CLIENT;
    public static final ServerConfig SERVER;
    public static final CommonConfig COMMON;

    private static final ModConfigSpec CLIENT_SPEC;
    private static final ModConfigSpec SERVER_SPEC;
    private static final ModConfigSpec COMMON_SPEC;
    private static final List<BasicConfigEntry<?>> CLIENT_ENTRIES;
    private static final List<BasicConfigEntry<?>> SERVER_ENTRIES;
    private static final List<BasicConfigEntry<?>> COMMON_ENTRIES;
    private static final Map<BasicConfigEntry<?>, ModConfigSpec> ENTRY_SPECS;

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

        CLIENT_ENTRIES = CLIENT.entries();
        SERVER_ENTRIES = SERVER.entries();
        COMMON_ENTRIES = COMMON.entries();
        var entrySpecs = new IdentityHashMap<BasicConfigEntry<?>, ModConfigSpec>();
        CLIENT_ENTRIES.forEach(entry -> entrySpecs.put(entry, CLIENT_SPEC));
        SERVER_ENTRIES.forEach(entry -> entrySpecs.put(entry, SERVER_SPEC));
        COMMON_ENTRIES.forEach(entry -> entrySpecs.put(entry, COMMON_SPEC));
        ENTRY_SPECS = Collections.unmodifiableMap(entrySpecs);
    }

    private GachaAddictionConfig() {
    }

    /**
     * 生命周期入口：在 {@link GachaAddiction} 主类构造时调用，注册三端 spec 到 NeoForge
     * （自动管理加载/保存，SERVER 配置会同步到客户端）。
     */
    public static void register() {
        GachaAddiction.LOGGER.info("Initialize the Gacha Addiction config files");
        NeoForgeConfigRegistry.INSTANCE.register(GachaAddiction.MODID, ModConfig.Type.COMMON, COMMON_SPEC);
        NeoForgeConfigRegistry.INSTANCE.register(GachaAddiction.MODID, ModConfig.Type.SERVER, SERVER_SPEC);
        NeoForgeConfigRegistry.INSTANCE.register(GachaAddiction.MODID, ModConfig.Type.CLIENT, CLIENT_SPEC);
    }

    public static List<BasicConfigEntry<?>> clientEntries() {
        return CLIENT_ENTRIES;
    }

    public static List<BasicConfigEntry<?>> commonEntries() {
        return COMMON_ENTRIES;
    }

    public static List<BasicConfigEntry<?>> serverEntries() {
        return SERVER_ENTRIES;
    }

    @SuppressWarnings("unused")
    public static void saveClient() {
        CLIENT_SPEC.save();
    }

    public static void save(BasicConfigEntry<?> entry) {
        ModConfigSpec spec = ENTRY_SPECS.get(entry);
        if (spec == null) {
            throw new IllegalArgumentException("Config entry is not registered");
        }
        spec.save();
    }

}

package xiaojin.gachaaddiction.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import xiaojin.gachaaddiction.config.entry.BooleanConfigEntry;

@SuppressWarnings("unused")
public class CommonConfig extends BasicConfig {
    private static final String AUTO_TAKE_CATEGORY = "auto_take";
    private static final String ENABLED_KEY = "enabled";
    private static final String ALLOWED_KEY = "allowed";

    /**
     * 由本地客户端读取，不使用服务端的配置值。
     */
    public final BooleanConfigEntry autoTakeEnabled;
    /**
     * 由逻辑服务端读取，只控制是否接受自动拿取请求。
     */
    public final BooleanConfigEntry allowAutoTake;

    public CommonConfig(ModConfigSpec.Builder builder) {
        super(builder);
        push(AUTO_TAKE_CATEGORY);
        autoTakeEnabled = define(false, ENABLED_KEY);
        allowAutoTake = define(true, ALLOWED_KEY);
        pop();
    }
}

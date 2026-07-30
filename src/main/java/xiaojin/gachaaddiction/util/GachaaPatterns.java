package xiaojin.gachaaddiction.util;

import java.util.regex.Pattern;

/**
 * 配置项的匹配规则。
 * <p>
 * 这些常量原本挂在 {@code GachaaAdictionConfigData} 上，但那个类的静态初始化会构造
 * {@code SimpleSoundInstance}（仅客户端可用）。配置解析在专用服务端也会走到，
 * 因此把纯规则抽到这里，避免服务端触发客户端类的初始化。
 */
public final class GachaaPatterns {
    public static final Pattern TAG_PATTERN = Pattern.compile(
            "^#[a-z0-9_.-]+:[a-z0-9_.-]+$");
    public static final Pattern ID_PATTERN = Pattern.compile(
            "^[a-z0-9_.-]+:[a-z0-9_.-]+$");
    public static final Pattern OPERATOR_RARITY_PATTERN = Pattern.compile(
            "^(>=|<=|>|<)?(-?\\d+)$");
    public static final Pattern RARITY_PATTERN = Pattern.compile(
            "^-?\\d+$");

    private GachaaPatterns() {
    }
}

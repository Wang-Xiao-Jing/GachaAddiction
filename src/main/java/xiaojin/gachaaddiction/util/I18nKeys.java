package xiaojin.gachaaddiction.util;

import xiaojin.gachaaddiction.GachaAddiction;

/**
 * 翻译键工具。原先由 datagen 的 DatagenI18n 提供，
 * Fabric 版本不再生成语言文件，但运行时仍需要这些键。
 */
public final class I18nKeys {
    private I18nKeys() {
    }

    public static String getFormattedKey(String... key) {
        StringBuilder builder = new StringBuilder(GachaAddiction.MODID);
        builder.append(".commands");
        for (String s : key) {
            builder.append(".").append(s);
        }
        return builder.toString();
    }

    public static String getConfigTranslation(String modId, String... keys) {
        if (keys.length == 0) {
            return modId + ".configuration";
        }
        StringBuilder builder = new StringBuilder();
        for (String key : keys) {
            builder.append(".");
            builder.append(key);
        }
        return modId + ".configuration" + builder;
    }
}

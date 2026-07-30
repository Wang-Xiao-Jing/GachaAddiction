package xiaojin.gachaaddiction.config;

import xiaojin.gachaaddiction.GachaAddiction;
import xiaojin.gachaaddiction.util.I18nKeys;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 配置分组基类。保留原先 {@code define/push/pop} 的写法，底层换成 {@link ConfigSpec}。
 */
public class BasicConfig {
    protected final String modId;
    protected final ConfigSpec spec;

    public BasicConfig(ConfigSpec spec) {
        this.modId = GachaAddiction.MODID;
        this.spec = spec;
    }

    protected ConfigValue.DoubleValue define(double defaultValue, double min, double max, String key, String... comment) {
        return new ConfigValue.DoubleValue(spec, pathOf(key), defaultValue, min, max, joinComment(comment));
    }

    protected ConfigValue.IntValue define(int defaultValue, int min, int max, String key, String... comment) {
        return new ConfigValue.IntValue(spec, pathOf(key), defaultValue, min, max, joinComment(comment));
    }

    protected ConfigValue.BooleanValue define(boolean defaultValue, String key, String... comment) {
        return new ConfigValue.BooleanValue(spec, pathOf(key), defaultValue, joinComment(comment));
    }

    protected ConfigValue<String> define(String defaultValue, String key, String... comment) {
        return new ConfigValue<>(spec, pathOf(key), defaultValue, joinComment(comment));
    }

    protected ConfigValue<List<? extends String>> defineList(List<String> defaultValue, String key,
                                                             java.util.function.Predicate<Object> validator,
                                                             String... comment) {
        return new ConfigValue.Validated<>(spec, pathOf(key), defaultValue, joinComment(comment), validator);
    }

    protected void push(String... key) {
        spec.push(key);
    }

    protected void pop(int count) {
        spec.pop(count);
    }

    protected void pop() {
        spec.pop(1);
    }

    /** 配置项的完整路径，同时用于生成翻译键。 */
    protected List<String> pathOf(String key) {
        List<String> path = new ArrayList<>(spec.currentPath());
        path.add(key);
        return path;
    }

    protected String[] getCurrentPath(String... key) {
        List<String> currentPath = new ArrayList<>(spec.currentPath());
        currentPath.addAll(Arrays.asList(key));
        return currentPath.toArray(String[]::new);
    }

    /** 翻译键，保持与旧版一致，方便沿用现有语言文件。 */
    protected String translationKey(String key) {
        return I18nKeys.getConfigTranslation(modId, getCurrentPath(key));
    }

    private static String joinComment(String... comment) {
        return comment == null || comment.length == 0 ? "" : String.join(" / ", comment);
    }
}

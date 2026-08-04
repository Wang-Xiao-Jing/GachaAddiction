package xiaojin.gachaaddiction.client.gui.config;

import net.minecraft.client.gui.components.AbstractWidget;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 通用自定义配置项渲染器注册表。
 * Mixin 拦截 {@code createStringValue} 时会查询此注册表，
 * 如果命中则用注册的工厂创建 {@link AbstractWidget} 替代默认的文本框。
 */
public class CustomConfigEntries {
    private static final Map<String, WidgetFactory> WIDGETS = new HashMap<>();

    @FunctionalInterface
    public interface WidgetFactory {
        /**
         * 创建配置项的 GUI 控件。
         *
         * @param source   当前值提供器
         * @param target   值变更回调（调用 target.accept(newValue) 提交修改）
         * @param key      配置短 key
         * @return 控件实例
         */
        AbstractWidget create(Supplier<String> source,
                              Consumer<String> target,
                              String key);
    }

    public static void register(String configKey, WidgetFactory factory) {
        WIDGETS.put(configKey, factory);
    }

    @Nullable
    public static WidgetFactory get(String configKey) {
        return WIDGETS.get(configKey);
    }
}

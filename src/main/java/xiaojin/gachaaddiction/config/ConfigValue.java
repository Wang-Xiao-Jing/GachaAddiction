package xiaojin.gachaaddiction.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * 单个配置项。行为对齐 NeoForge 的 {@code ModConfigSpec.ConfigValue}：
 * {@link #get()} 取值，{@link #getPath()} 取完整路径。
 */
public class ConfigValue<T> {
    private final List<String> path;
    private final T defaultValue;
    private final String comment;
    private T value;

    ConfigValue(ConfigSpec spec, List<String> path, T defaultValue, String comment) {
        this.path = List.copyOf(path);
        this.defaultValue = defaultValue;
        this.comment = comment;
        this.value = defaultValue;
        spec.register(this);
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }

    public T getDefault() {
        return defaultValue;
    }

    public List<String> getPath() {
        return path;
    }

    public String getComment() {
        return comment;
    }

    JsonElement toJson() {
        return toJson(value);
    }

    @SuppressWarnings("unchecked")
    protected JsonElement toJson(T value) {
        if (value instanceof List<?> list) {
            JsonArray array = new JsonArray();
            list.forEach(entry -> array.add(String.valueOf(entry)));
            return array;
        }
        return new JsonPrimitive((String) value);
    }

    @SuppressWarnings("unchecked")
    protected void fromJson(JsonElement element) {
        if (defaultValue instanceof List<?>) {
            if (!element.isJsonArray()) {
                return;
            }
            List<String> list = new ArrayList<>();
            element.getAsJsonArray().forEach(entry -> list.add(entry.getAsString()));
            this.value = (T) list;
            return;
        }
        if (element.isJsonPrimitive()) {
            this.value = (T) element.getAsString();
        }
    }

    /** 校验值，非法时回退到默认值。 */
    public static class Validated<T> extends ConfigValue<T> {
        private final Predicate<Object> validator;

        Validated(ConfigSpec spec, List<String> path, T defaultValue, String comment, Predicate<Object> validator) {
            super(spec, path, defaultValue, comment);
            this.validator = validator;
        }

        @Override
        protected void fromJson(JsonElement element) {
            super.fromJson(element);
            Object current = get();
            if (current instanceof List<?> list) {
                list.removeIf(entry -> !validator.test(entry));
            } else if (!validator.test(current)) {
                set(getDefault());
            }
        }
    }

    public static class BooleanValue extends ConfigValue<Boolean> {
        BooleanValue(ConfigSpec spec, List<String> path, boolean defaultValue, String comment) {
            super(spec, path, defaultValue, comment);
        }

        @Override
        protected JsonElement toJson(Boolean value) {
            return new JsonPrimitive(value);
        }

        @Override
        protected void fromJson(JsonElement element) {
            if (element.isJsonPrimitive()) {
                set(element.getAsBoolean());
            }
        }
    }

    public static class IntValue extends ConfigValue<Integer> {
        private final int min;
        private final int max;

        IntValue(ConfigSpec spec, List<String> path, int defaultValue, int min, int max, String comment) {
            super(spec, path, defaultValue, comment);
            this.min = min;
            this.max = max;
        }

        @Override
        protected JsonElement toJson(Integer value) {
            return new JsonPrimitive(value);
        }

        @Override
        protected void fromJson(JsonElement element) {
            if (element.isJsonPrimitive()) {
                set(Math.clamp(element.getAsInt(), min, max));
            }
        }
    }

    public static class DoubleValue extends ConfigValue<Double> {
        private final double min;
        private final double max;

        DoubleValue(ConfigSpec spec, List<String> path, double defaultValue, double min, double max, String comment) {
            super(spec, path, defaultValue, comment);
            this.min = min;
            this.max = max;
        }

        @Override
        protected JsonElement toJson(Double value) {
            return new JsonPrimitive(value);
        }

        @Override
        protected void fromJson(JsonElement element) {
            if (element.isJsonPrimitive()) {
                set(Math.clamp(element.getAsDouble(), min, max));
            }
        }
    }
}

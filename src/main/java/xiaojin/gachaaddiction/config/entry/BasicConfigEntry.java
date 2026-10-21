package xiaojin.gachaaddiction.config.entry;

import com.mojang.logging.LogUtils;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.slf4j.Logger;
import xiaojin.gachaaddiction.registry.language.GachaTranslations;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class BasicConfigEntry<T> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final ModConfigSpec.ConfigValue<?> rawValue;
    private final List<String> path;
    private final String translationKey;
    private final List<String> comments;
    private final T defaultValue;
    private final Predicate<T> validator;
    private final Supplier<T> getter;
    private final Consumer<T> setter;

    public BasicConfigEntry(ModConfigSpec.ConfigValue<?> rawValue, List<String> path, String translationKey,
                            List<String> comments, T defaultValue, Predicate<T> validator,
                            Supplier<T> getter, Consumer<T> setter) {
        this.rawValue = Objects.requireNonNull(rawValue);
        this.path = List.copyOf(path);
        this.translationKey = Objects.requireNonNull(translationKey);
        this.comments = List.copyOf(comments);
        this.defaultValue = Objects.requireNonNull(defaultValue);
        this.validator = Objects.requireNonNull(validator);
        this.getter = Objects.requireNonNull(getter);
        this.setter = Objects.requireNonNull(setter);
        if (this.path.isEmpty()) {
            throw new IllegalArgumentException("Config path must not be empty");
        }
        if (!isValid(defaultValue)) {
            throw new IllegalArgumentException("Default value must pass validation");
        }
    }

    public T get() {
        try {
            T value = getter.get();
            if (isValid(value)) {
                return value;
            }
        } catch (RuntimeException ignored) {
        }
        return defaultValue;
    }

    public boolean set(T value) {
        if (!isValid(value)) {
            return false;
        }
        try {
            setter.accept(value);
            return true;
        } catch (RuntimeException exception) {
            LOGGER.error("Failed to update config entry {}", String.join(".", path), exception);
            return false;
        }
    }

    public boolean reset() {
        return set(defaultValue);
    }

    public boolean isValid(T value) {
        try {
            return value != null && validator.test(value);
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    public T defaultValue() {
        return defaultValue;
    }

    public ModConfigSpec.ConfigValue<?> rawValue() {
        return rawValue;
    }

    public List<String> path() {
        return path;
    }

    public String translationKey() {
        return translationKey;
    }

    public List<String> comments() {
        return comments;
    }

    public abstract static class Builder<E extends BasicConfigEntry<?>, B extends Builder<E, B>> {
        protected final ModConfigSpec.Builder specBuilder;
        protected final String modId;
        protected final List<String> currentPath;
        protected String key;
        protected List<String> comments = List.of();

        protected Builder(ModConfigSpec.Builder specBuilder, String modId, List<String> currentPath) {
            this.specBuilder = Objects.requireNonNull(specBuilder);
            this.modId = Objects.requireNonNull(modId);
            this.currentPath = List.copyOf(currentPath);
            if (modId.isBlank()) {
                throw new IllegalArgumentException("Mod id must not be blank");
            }
        }

        protected abstract B self();

        public B key(String key) {
            this.key = Objects.requireNonNull(key);
            return self();
        }

        public B comments(String... comments) {
            this.comments = List.of(Objects.requireNonNull(comments));
            return self();
        }

        protected List<String> path() {
            validateBaseDefinition();
            var path = new ArrayList<>(currentPath);
            path.add(key);
            return List.copyOf(path);
        }

        protected String translationKey() {
            validateBaseDefinition();
            return GachaTranslations.getConfigTranslation(modId, path().toArray(String[]::new));
        }

        protected ModConfigSpec.Builder prepareDefinition() {
            validateBaseDefinition();
            return specBuilder.comment(comments.toArray(String[]::new)).translation(translationKey());
        }

        private void validateBaseDefinition() {
            if (key == null || key.isBlank()) {
                throw new IllegalStateException("Config key must not be blank");
            }
        }

        public abstract E build();
    }
}

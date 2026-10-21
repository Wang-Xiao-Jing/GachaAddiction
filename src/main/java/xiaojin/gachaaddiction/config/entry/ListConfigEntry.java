package xiaojin.gachaaddiction.config.entry;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class ListConfigEntry<V> extends BasicConfigEntry<List<V>> {
    private final Function<String, V> parse;
    private final Function<V, String> encode;
    private final Predicate<Object> elementValidator;

    public ListConfigEntry(ModConfigSpec.ConfigValue<List<? extends String>> rawValue, List<String> path,
                           String translationKey, List<String> comments, List<V> defaultValue,
                           Predicate<Object> elementValidator, Function<String, V> parse, Function<V, String> encode) {
        super(rawValue, path, translationKey, comments, List.copyOf(defaultValue),
                values -> values.stream().allMatch(value -> isElementValid(value, elementValidator, encode)),
                () -> rawValue.get().stream().map(parse).toList(),
                values -> rawValue.set(values.stream().map(encode).toList()));
        this.elementValidator = Objects.requireNonNull(elementValidator);
        this.parse = Objects.requireNonNull(parse);
        this.encode = Objects.requireNonNull(encode);
    }

    private static <V> boolean isElementValid(V value, Predicate<Object> validator, Function<V, String> encode) {
        if (value == null) {
            return false;
        }
        try {
            String encoded = encode.apply(value);
            return encoded != null && validator.test(encoded);
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    @SuppressWarnings("unused")
    public Predicate<Object> elementValidator() {
        return elementValidator;
    }

    public Optional<V> tryParse(String string) {
        try {
            V value = parse.apply(string);
            return isElementValid(value, elementValidator, encode) ? Optional.of(value) : Optional.empty();
        } catch (RuntimeException ignored) {
            return Optional.empty();
        }
    }

    @SuppressWarnings("unused")
    public String encode(V value) {
        if (!isElementValid(value, elementValidator, encode)) {
            return null;
        }
        try {
            return encode.apply(value);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    public abstract static class Builder<V, E extends ListConfigEntry<V>, B extends Builder<V, E, B>>
            extends BasicConfigEntry.Builder<E, B> {
        protected List<V> defaultValue = List.of();
        protected boolean defaultValueSet;
        protected Predicate<Object> elementValidator;
        protected Function<String, V> parse;
        protected Function<V, String> encode;

        protected Builder(ModConfigSpec.Builder specBuilder, String modId, List<String> currentPath) {
            super(specBuilder, modId, currentPath);
        }

        public B defaultValue(List<? extends V> defaultValue) {
            this.defaultValue = List.copyOf(defaultValue);
            this.defaultValueSet = true;
            return self();
        }

        public B elementValidator(Predicate<Object> elementValidator) {
            this.elementValidator = Objects.requireNonNull(elementValidator);
            return self();
        }

        public B codec(Function<String, V> parse, Function<V, String> encode) {
            this.parse = Objects.requireNonNull(parse);
            this.encode = Objects.requireNonNull(encode);
            return self();
        }

        protected void validateDefinition() {
            if (!defaultValueSet || elementValidator == null || parse == null || encode == null) {
                throw new IllegalStateException("Default value, element validator and codec must be configured");
            }
            if (!defaultValue.stream().allMatch(value -> isElementValid(value, elementValidator, encode))) {
                throw new IllegalStateException("Every default list element must pass validation");
            }
            for (V value : defaultValue) {
                V decoded = parse.apply(encode.apply(value));
                if (!Objects.equals(value, decoded)) {
                    throw new IllegalStateException("Every default list element must survive codec conversion");
                }
            }
        }

        protected List<String> encodedDefaultValue() {
            return defaultValue.stream().map(encode).toList();
        }
    }
}

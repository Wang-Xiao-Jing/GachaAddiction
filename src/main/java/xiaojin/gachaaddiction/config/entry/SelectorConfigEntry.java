package xiaojin.gachaaddiction.config.entry;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.HashSet;
import java.util.List;
import java.util.function.Function;

public class SelectorConfigEntry<V> extends ConfigEntry<V> {
    private final List<V> allowedValues;

    public SelectorConfigEntry(ModConfigSpec.ConfigValue<String> rawValue, List<String> path, String translationKey,
                               List<String> comments, V defaultValue,
                               Function<String, V> decoding, Function<V, String> encode, List<V> allowedValues) {
        super(rawValue, path, translationKey, comments, defaultValue, decoding, encode, allowedValues::contains);
        this.allowedValues = List.copyOf(allowedValues);
    }

    public static <V> Builder<V> builder(ModConfigSpec.Builder specBuilder, String modId, List<String> currentPath) {
        return new Builder<>(specBuilder, modId, currentPath);
    }

    public List<V> allowedValues() {
        return allowedValues;
    }

    public static class Builder<V> extends ConfigEntry.Builder<V, SelectorConfigEntry<V>, Builder<V>> {
        private List<V> allowedValues = List.of();

        private Builder(ModConfigSpec.Builder specBuilder, String modId, List<String> currentPath) {
            super(specBuilder, modId, currentPath);
        }

        @Override
        protected Builder<V> self() {
            return this;
        }

        public Builder<V> allowedValues(List<V> allowedValues) {
            this.allowedValues = List.copyOf(allowedValues);
            validator(this.allowedValues::contains);
            return this;
        }

        @Override
        public SelectorConfigEntry<V> build() {
            if (allowedValues.isEmpty()) {
                throw new IllegalStateException("Allowed values must not be empty");
            }
            validateDefinition();
            var encodedValues = allowedValues.stream().map(encode).toList();
            if (encodedValues.stream().anyMatch(value -> value == null || value.isBlank())
                    || new HashSet<>(encodedValues).size() != encodedValues.size()) {
                throw new IllegalStateException("Allowed values must encode to unique non-blank strings");
            }
            for (int i = 0; i < allowedValues.size(); i++) {
                if (!allowedValues.get(i).equals(decoding.apply(encodedValues.get(i)))) {
                    throw new IllegalStateException("Every allowed value must survive codec conversion");
                }
            }
            var value = prepareDefinition().defineInList(key, encode.apply(defaultValue),
                    encodedValues);
            return new SelectorConfigEntry<>(value, path(), translationKey(), comments, defaultValue,
                    decoding, encode, allowedValues);
        }
    }
}

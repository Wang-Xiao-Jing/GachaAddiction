package xiaojin.gachaaddiction.reward.parameter;

import java.util.*;

public final class GachaParameterSet {
    public static final GachaParameterSet EMPTY = new GachaParameterSet(Map.of());

    private final Map<GachaParameterKey, Object> values;

    private GachaParameterSet(Map<GachaParameterKey, Object> values) {
        this.values = Collections.unmodifiableMap(new LinkedHashMap<>(values));
    }

    public static Builder builder() {
        return new Builder();
    }

    public Optional<Object> get(GachaParameterKey key) {
        return Optional.ofNullable(values.get(Objects.requireNonNull(key, "key")));
    }

    public <T> Optional<T> get(GachaParameterDefinition<T> definition) {
        Object value = values.get(Objects.requireNonNull(definition, "definition").key());
        if (value == null) {
            return Optional.empty();
        }
        if (!definition.type().valueClass().isInstance(value)) {
            throw new IllegalStateException("Parameter value does not match definition: " + definition.key());
        }
        return Optional.of(definition.type().copy(definition.type().valueClass().cast(value)));
    }

    public Map<GachaParameterKey, Object> asMap() {
        return values;
    }

    public Builder toBuilder() {
        return new Builder(values);
    }

    public static final class Builder {
        private final Map<GachaParameterKey, Object> values;

        private Builder() {
            this.values = new LinkedHashMap<>();
        }

        private Builder(Map<GachaParameterKey, Object> values) {
            this.values = new LinkedHashMap<>(values);
        }

        private static <T> T copyValue(GachaParameterType<T> type, Object value) {
            return type.copy(type.valueClass().cast(value));
        }

        public Builder put(GachaParameterKey key, Object value) {
            values.put(Objects.requireNonNull(key, "key"), Objects.requireNonNull(value, "value"));
            return this;
        }

        public Builder put(GachaParameterDefinition<?> definition, Object value) {
            Objects.requireNonNull(definition, "definition");
            Objects.requireNonNull(value, "value");
            if (!definition.type().valueClass().isInstance(value)) {
                throw new IllegalArgumentException("Parameter value does not match definition: " + definition.key());
            }
            return put(definition.key(), copyValue(definition.type(), value));
        }

        public Builder remove(GachaParameterKey key) {
            values.remove(Objects.requireNonNull(key, "key"));
            return this;
        }

        public GachaParameterSet build() {
            return values.isEmpty() ? EMPTY : new GachaParameterSet(values);
        }
    }
}

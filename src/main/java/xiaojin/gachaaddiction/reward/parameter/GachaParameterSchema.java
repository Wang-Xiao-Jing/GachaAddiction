package xiaojin.gachaaddiction.reward.parameter;

import java.util.*;

public final class GachaParameterSchema {
    private final Map<GachaParameterKey, GachaParameterDefinition<?>> definitions;

    public GachaParameterSchema(List<GachaParameterDefinition<?>> definitions) {
        Objects.requireNonNull(definitions, "definitions");
        Map<GachaParameterKey, GachaParameterDefinition<?>> indexed = new LinkedHashMap<>();
        for (GachaParameterDefinition<?> definition : definitions) {
            Objects.requireNonNull(definition, "definition");
            if (indexed.putIfAbsent(definition.key(), definition) != null) {
                throw new IllegalArgumentException("Duplicate parameter definition: " + definition.key());
            }
        }
        this.definitions = Collections.unmodifiableMap(indexed);
    }

    public GachaParameterDefinition<?> require(GachaParameterKey key) {
        GachaParameterDefinition<?> definition = definitions.get(Objects.requireNonNull(key, "key"));
        if (definition == null) {
            throw new IllegalArgumentException("Unknown parameter: " + key);
        }
        return definition;
    }

    public Optional<GachaParameterDefinition<?>> find(GachaParameterKey key) {
        return Optional.ofNullable(definitions.get(Objects.requireNonNull(key, "key")));
    }

    public GachaParameterSet normalizeInputs(GachaParameterSet supplied) {
        Objects.requireNonNull(supplied, "supplied");
        for (GachaParameterKey key : supplied.asMap().keySet()) {
            GachaParameterDefinition<?> definition = require(key);
            if (definition.direction() == GachaParameterDirection.OUTPUT) {
                throw new IllegalArgumentException("Output parameter cannot be supplied as input: " + key);
            }
        }
        GachaParameterSet.Builder normalized = GachaParameterSet.builder();
        for (GachaParameterDefinition<?> definition : definitions.values()) {
            if (definition.direction() == GachaParameterDirection.OUTPUT) {
                continue;
            }
            Object value = supplied.get(definition.key()).orElseGet(() -> definition.defaultValue().orElse(null));
            if (value == null) {
                if (definition.required()) {
                    throw new IllegalArgumentException("Missing required parameter: " + definition.key());
                }
                continue;
            }
            normalized.put(definition, value);
        }
        return normalized.build();
    }

    public List<GachaParameterDefinition<?>> definitions() {
        return List.copyOf(definitions.values());
    }
}

package xiaojin.gachaaddiction.reward.parameter;

import java.util.Objects;
import java.util.Optional;

public record GachaParameterDefinition<T>(GachaParameterKey key, GachaParameterType<T> type,
                                          GachaParameterDirection direction, boolean required,
                                          Optional<T> defaultValue) {
    public GachaParameterDefinition {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(direction, "direction");
        defaultValue = Objects.requireNonNull(defaultValue, "defaultValue");
        defaultValue.ifPresent(value -> {
            if (!type.valueClass().isInstance(value)) {
                throw new IllegalArgumentException("Default value does not match parameter type: " + key);
            }
        });
        if (direction == GachaParameterDirection.OUTPUT && defaultValue.isPresent()) {
            throw new IllegalArgumentException("Output parameter cannot declare an input default: " + key);
        }
    }

    public T requireValue(GachaParameterSet parameters) {
        return parameters.get(this).or(() -> defaultValue)
                .orElseThrow(() -> new IllegalArgumentException("Missing required parameter: " + key));
    }
}

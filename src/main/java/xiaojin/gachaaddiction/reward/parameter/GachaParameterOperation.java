package xiaojin.gachaaddiction.reward.parameter;

import java.util.Objects;
import java.util.Optional;

public record GachaParameterOperation(GachaParameterKey key, Optional<Object> before, Optional<Object> after) {
    public GachaParameterOperation {
        Objects.requireNonNull(key, "key");
        before = Objects.requireNonNull(before, "before");
        after = Objects.requireNonNull(after, "after");
        if (before.equals(after)) {
            throw new IllegalArgumentException("Parameter operation must change its value: " + key);
        }
    }

    public static GachaParameterOperation set(GachaParameterKey key, Object before, Object after) {
        return new GachaParameterOperation(key, Optional.ofNullable(before), Optional.of(after));
    }

    public static GachaParameterOperation remove(GachaParameterKey key, Object before) {
        return new GachaParameterOperation(key, Optional.of(before), Optional.empty());
    }
}

package xiaojin.gachaaddiction.cardpool.parameter;

import java.util.Objects;

public record GachaPlayerChoice<T>(String labelTranslationKey,
                                   GachaPlayerChoiceConstraint<T> constraint) {
    public GachaPlayerChoice {
        if (Objects.requireNonNull(labelTranslationKey, "labelTranslationKey").isBlank()) {
            throw new IllegalArgumentException("Player choice label translation key cannot be blank");
        }
        Objects.requireNonNull(constraint, "constraint");
    }

    public T requireAllowed(T value) {
        Objects.requireNonNull(value, "value");
        if (!constraint.allows(value)) {
            throw new IllegalArgumentException("Player choice value is outside its declared constraint");
        }
        return value;
    }
}

package xiaojin.gachaaddiction.cardpool.parameter;

import xiaojin.gachaaddiction.reward.parameter.GachaParameterDefinition;

import java.util.Objects;
import java.util.Optional;

public record GachaParameterBinding<T>(GachaParameterDefinition<T> parameter,
                                       GachaParameterBindingSource source,
                                       GachaParameterOverridePolicy overridePolicy,
                                       Optional<T> boundValue,
                                       Optional<GachaPlayerChoice<T>> playerChoice) {
    public GachaParameterBinding {
        Objects.requireNonNull(parameter, "parameter");
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(overridePolicy, "overridePolicy");
        boundValue = Objects.requireNonNull(boundValue, "boundValue");
        playerChoice = Objects.requireNonNull(playerChoice, "playerChoice");
        if (source == GachaParameterBindingSource.CONSTANT && boundValue.isEmpty()) {
            throw new IllegalArgumentException("Constant parameter binding requires a value: " + parameter.key());
        }
        if (source != GachaParameterBindingSource.CONSTANT && boundValue.isPresent()) {
            throw new IllegalArgumentException("Only constant parameter bindings can contain a bound value: "
                    + parameter.key());
        }
        boundValue.ifPresent(value -> {
            if (!parameter.type().valueClass().isInstance(value)) {
                throw new IllegalArgumentException("Bound value does not match parameter type: " + parameter.key());
            }
        });
        if (overridePolicy == GachaParameterOverridePolicy.PLAYER_CHOICE && playerChoice.isEmpty()) {
            throw new IllegalArgumentException("Player choice binding requires a choice constraint: "
                    + parameter.key());
        }
        if (overridePolicy != GachaParameterOverridePolicy.PLAYER_CHOICE && playerChoice.isPresent()) {
            throw new IllegalArgumentException("Only player choice bindings can declare a choice constraint: "
                    + parameter.key());
        }
    }
}

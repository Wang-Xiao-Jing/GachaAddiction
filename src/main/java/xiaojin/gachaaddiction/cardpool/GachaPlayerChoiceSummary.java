package xiaojin.gachaaddiction.cardpool;

import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record GachaPlayerChoiceSummary(ResourceLocation owner, String name, ResourceLocation parameterType,
                                       String labelTranslationKey, GachaPlayerChoiceKind kind,
                                       Optional<String> minimum, Optional<String> maximum, Optional<String> step,
                                       List<String> values) {
    public GachaPlayerChoiceSummary {
        Objects.requireNonNull(owner, "owner");
        if (Objects.requireNonNull(name, "name").isBlank()) {
            throw new IllegalArgumentException("Player choice parameter name cannot be blank");
        }
        Objects.requireNonNull(parameterType, "parameterType");
        if (Objects.requireNonNull(labelTranslationKey, "labelTranslationKey").isBlank()) {
            throw new IllegalArgumentException("Player choice label cannot be blank");
        }
        Objects.requireNonNull(kind, "kind");
        minimum = Objects.requireNonNull(minimum, "minimum");
        maximum = Objects.requireNonNull(maximum, "maximum");
        step = Objects.requireNonNull(step, "step");
        values = List.copyOf(values);
        if (kind == GachaPlayerChoiceKind.NUMBER_RANGE
                && (minimum.isEmpty() || maximum.isEmpty() || step.isEmpty())) {
            throw new IllegalArgumentException("Number range choice requires minimum, maximum, and step");
        }
        if (kind == GachaPlayerChoiceKind.ENUMERATED && values.isEmpty()) {
            throw new IllegalArgumentException("Enumerated choice requires values");
        }
    }
}

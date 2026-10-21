package xiaojin.gachaaddiction.execution;

import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;

public record GachaExecutionStepPreview(ResourceLocation type, Optional<ResourceLocation> target,
                                        OptionalLong rounds, List<ResourceLocation> consumptionTypes) {
    public GachaExecutionStepPreview {
        Objects.requireNonNull(type, "type");
        target = Objects.requireNonNull(target, "target");
        rounds = Objects.requireNonNull(rounds, "rounds");
        consumptionTypes = List.copyOf(consumptionTypes);
    }
}

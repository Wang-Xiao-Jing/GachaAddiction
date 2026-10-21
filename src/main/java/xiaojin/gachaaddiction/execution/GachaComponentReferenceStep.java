package xiaojin.gachaaddiction.execution;

import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.Set;

public record GachaComponentReferenceStep(ResourceLocation type, ResourceLocation target,
                                          GachaParameterMapping parameters,
                                          Set<GachaExecutionChannel> reads,
                                          Set<GachaExecutionChannel> writes) implements IGachaExecutionStep {
    public GachaComponentReferenceStep {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(parameters, "parameters");
        reads = Set.copyOf(reads);
        writes = Set.copyOf(writes);
    }
}

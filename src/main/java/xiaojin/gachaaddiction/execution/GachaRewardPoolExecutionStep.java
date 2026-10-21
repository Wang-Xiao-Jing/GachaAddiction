package xiaojin.gachaaddiction.execution;

import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.reward.number.GachaLongProvider;

import java.util.Objects;
import java.util.Set;

public record GachaRewardPoolExecutionStep(ResourceLocation type, ResourceLocation target,
                                           GachaLongProvider rounds, GachaParameterMapping parameters,
                                           Set<GachaExecutionChannel> reads,
                                           Set<GachaExecutionChannel> writes) implements GachaExecutionStep {
    public GachaRewardPoolExecutionStep {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(rounds, "rounds");
        Objects.requireNonNull(parameters, "parameters");
        reads = Set.copyOf(reads);
        writes = Set.copyOf(writes);
    }
}

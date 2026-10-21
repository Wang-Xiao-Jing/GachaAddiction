package xiaojin.gachaaddiction.execution;

import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.reward.number.IGachaLongProvider;
import xiaojin.gachaaddiction.reward.quantity.GachaExecutionLimits;

import java.util.Objects;
import java.util.Set;

public record GachaCustomTableExecutionStep(ResourceLocation type, ResourceLocation target,
                                            IGachaLongProvider rounds, GachaExecutionLimits limits,
                                            Set<GachaExecutionChannel> reads,
                                            Set<GachaExecutionChannel> writes) implements IGachaExecutionStep {
    public GachaCustomTableExecutionStep {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(rounds, "rounds");
        Objects.requireNonNull(limits, "limits");
        reads = Set.copyOf(reads);
        writes = Set.copyOf(writes);
    }
}

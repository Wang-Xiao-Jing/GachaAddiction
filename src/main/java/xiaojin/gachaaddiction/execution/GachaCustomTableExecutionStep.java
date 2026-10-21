package xiaojin.gachaaddiction.execution;

import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.reward.number.GachaLongProvider;
import xiaojin.gachaaddiction.reward.quantity.GachaExecutionLimits;

import java.util.Objects;
import java.util.Set;

public record GachaCustomTableExecutionStep(ResourceLocation type, ResourceLocation target,
                                            GachaLongProvider rounds, GachaExecutionLimits limits,
                                            Set<GachaExecutionChannel> reads,
                                            Set<GachaExecutionChannel> writes) implements GachaExecutionStep {
    public GachaCustomTableExecutionStep {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(rounds, "rounds");
        Objects.requireNonNull(limits, "limits");
        reads = Set.copyOf(reads);
        writes = Set.copyOf(writes);
    }
}

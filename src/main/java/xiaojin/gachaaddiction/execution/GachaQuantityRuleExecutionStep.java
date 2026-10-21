package xiaojin.gachaaddiction.execution;

import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.reward.quantity.GachaQuantityPipeline;

import java.util.Objects;
import java.util.Set;

public record GachaQuantityRuleExecutionStep(ResourceLocation type, ResourceLocation target,
                                             GachaQuantityPipeline pipeline,
                                             Set<GachaExecutionChannel> reads,
                                             Set<GachaExecutionChannel> writes) implements IGachaExecutionStep {
    public GachaQuantityRuleExecutionStep {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(pipeline, "pipeline");
        reads = Set.copyOf(reads);
        writes = Set.copyOf(writes);
    }
}

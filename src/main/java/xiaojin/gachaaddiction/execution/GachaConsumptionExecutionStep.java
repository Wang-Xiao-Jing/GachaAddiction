package xiaojin.gachaaddiction.execution;

import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.cardpool.transaction.GachaConsumptionSpec;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public record GachaConsumptionExecutionStep(ResourceLocation type, List<GachaConsumptionSpec> consumption,
                                            Set<GachaExecutionChannel> reads,
                                            Set<GachaExecutionChannel> writes) implements GachaExecutionStep {
    public GachaConsumptionExecutionStep {
        Objects.requireNonNull(type, "type");
        consumption = List.copyOf(consumption);
        reads = Set.copyOf(reads);
        writes = Set.copyOf(writes);
    }
}

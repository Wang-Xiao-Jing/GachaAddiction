package xiaojin.gachaaddiction.execution;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;
import xiaojin.gachaaddiction.reward.number.IGachaLongProvider;
import xiaojin.gachaaddiction.reward.quantity.GachaExecutionLimits;

import java.util.Objects;
import java.util.Set;

public record GachaVanillaLootTableExecutionStep(ResourceLocation type, ResourceKey<LootTable> lootTable,
                                                 IGachaLongProvider rounds, GachaExecutionLimits limits,
                                                 Set<GachaExecutionChannel> reads,
                                                 Set<GachaExecutionChannel> writes) implements IGachaExecutionStep {
    public GachaVanillaLootTableExecutionStep {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(lootTable, "lootTable");
        Objects.requireNonNull(rounds, "rounds");
        Objects.requireNonNull(limits, "limits");
        reads = Set.copyOf(reads);
        writes = Set.copyOf(writes);
    }
}

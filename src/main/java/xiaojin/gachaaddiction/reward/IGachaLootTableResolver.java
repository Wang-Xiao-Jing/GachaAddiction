package xiaojin.gachaaddiction.reward;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.Optional;

@FunctionalInterface
public interface IGachaLootTableResolver {
    Optional<LootTable> resolve(ResourceKey<LootTable> key);
}

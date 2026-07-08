package xiaojin.gachaaddiction.mixed;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.List;

public interface IMenuProviderLootExtension {
    List<ResourceKey<LootTable>> gachaaddiction$getLootTable();
}

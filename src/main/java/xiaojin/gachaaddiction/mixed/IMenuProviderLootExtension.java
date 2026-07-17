package xiaojin.gachaaddiction.mixed;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IMenuProviderLootExtension {
    List<@Nullable ResourceKey<LootTable>> gachaaddiction$getLootTable();
}

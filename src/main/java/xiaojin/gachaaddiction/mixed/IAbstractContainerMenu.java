package xiaojin.gachaaddiction.mixed;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.Nullable;
import xiaojin.gachaaddiction.api.ItemStackEntry;

import java.util.List;

public interface IAbstractContainerMenu {
    static IAbstractContainerMenu of(AbstractContainerMenu menu) {
        return (IAbstractContainerMenu) menu;
    }

    boolean gachaaddiction$isInit();

    void gachaaddiction$setIsInit(boolean isInit);

    List<@Nullable ResourceKey<LootTable>> gachaaddiction$getLootTableKey();

    void gachaaddiction$setLootTableKey(@Nullable List<@Nullable ResourceKey<LootTable>> lootTableResourceKeys);

    List<ItemStackEntry> gachaaddiction$getDisplayEntries();

    void gachaaddiction$setDisplayEntries(@Nullable List<ItemStackEntry> entries);
}

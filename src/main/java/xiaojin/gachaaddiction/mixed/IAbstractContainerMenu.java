package xiaojin.gachaaddiction.mixed;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.storage.loot.LootTable;
import xiaojin.gachaaddiction.util.DisplayEntry;

import java.util.List;

public interface IAbstractContainerMenu {
    static IAbstractContainerMenu of(AbstractContainerMenu menu) {
        return (IAbstractContainerMenu) menu;
    }

    boolean gachaaddiction$isInit();

    void gachaaddiction$setIsInit(boolean isInit);

    List<ResourceKey<LootTable>> gachaaddiction$getLootTableKey();

    void gachaaddiction$setLootTableKey(List<ResourceKey<LootTable>> lootTableResourceKeys);

    List<DisplayEntry> gachaaddiction$getDisplayEntries();

    void gachaaddiction$setDisplayEntries(List<DisplayEntry> entries);
}

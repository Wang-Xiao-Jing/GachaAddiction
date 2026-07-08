package xiaojin.gachaaddiction.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import xiaojin.gachaaddiction.mixed.IAbstractContainerMenu;
import xiaojin.gachaaddiction.util.DisplayEntry;

import java.util.List;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin implements IAbstractContainerMenu {
    @Unique
    private boolean gachaaddiction$isInit = true;
    @Unique
    private List<ResourceKey<LootTable>> gachaaddiction$lootTableKey = List.of();
    @Unique
    private List<DisplayEntry> gachaaddiction$displayEntries = List.of();

    @Override
    public boolean gachaaddiction$isInit() {
        return gachaaddiction$isInit;
    }

    @Override
    public void gachaaddiction$setIsInit(boolean isInit) {
        this.gachaaddiction$isInit = isInit;
    }

    @Override
    public List<ResourceKey<LootTable>> gachaaddiction$getLootTableKey() {
        return gachaaddiction$lootTableKey;
    }

    @Override
    public void gachaaddiction$setLootTableKey(List<ResourceKey<LootTable>> key) {
        if (key == null) {
            this.gachaaddiction$lootTableKey = List.of();
            return;
        }
        this.gachaaddiction$lootTableKey = key;
    }

    @Override
    public List<DisplayEntry> gachaaddiction$getDisplayEntries() {
        return gachaaddiction$displayEntries;
    }

    @Override
    public void gachaaddiction$setDisplayEntries(List<DisplayEntry> entries) {
        if (entries == null) {
            this.gachaaddiction$displayEntries = List.of();
            return;
        }
        this.gachaaddiction$displayEntries = entries;
    }
}

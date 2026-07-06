package xiaojin.gachaaddiction.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import xiaojin.gachaaddiction.mixed.IAbstractContainerMenu;
import xiaojin.gachaaddiction.util.DisplayEntry;

import java.util.ArrayList;
import java.util.List;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin implements IAbstractContainerMenu {
    @Unique
    private boolean gachaaddiction$isInit = true;
    @Unique
    @Nullable
    private ResourceKey<LootTable> gachaaddiction$lootTableKey;
    @Unique
    private List<DisplayEntry> gachaaddiction$displayEntries = new ArrayList<>();

    @Override
    public boolean gachaaddiction$isInit() {
        return gachaaddiction$isInit;
    }

    @Override
    public void gachaaddiction$setIsInit(boolean isInit) {
        this.gachaaddiction$isInit = isInit;
    }

    @Override
    @Nullable
    public ResourceKey<LootTable> gachaaddiction$getLootTableKey() {
        return gachaaddiction$lootTableKey;
    }

    @Override
    public void gachaaddiction$setLootTableKey(ResourceKey<LootTable> key) {
        this.gachaaddiction$lootTableKey = key;
    }

    @Override
    public List<DisplayEntry> gachaaddiction$getDisplayEntries() {
        return gachaaddiction$displayEntries;
    }

    @Override
    public void gachaaddiction$setDisplayEntries(List<DisplayEntry> entries) {
        this.gachaaddiction$displayEntries = entries;
    }
}

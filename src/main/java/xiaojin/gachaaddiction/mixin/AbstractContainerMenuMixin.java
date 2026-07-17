package xiaojin.gachaaddiction.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import xiaojin.gachaaddiction.mixed.IAbstractContainerMenu;
import xiaojin.gachaaddiction.api.ItemStackEntry;

import java.util.ArrayList;
import java.util.List;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin implements IAbstractContainerMenu {
    @Unique
    private boolean gachaaddiction$isInit = true;
    @Unique
    private List<@Nullable ResourceKey<LootTable>> gachaaddiction$lootTableKey = new ArrayList<>();
    @Unique
    private List<ItemStackEntry> gachaaddiction$displayEntries = new ArrayList<>();

    @Override
    public boolean gachaaddiction$isInit() {
        return gachaaddiction$isInit;
    }

    @Override
    public void gachaaddiction$setIsInit(boolean isInit) {
        this.gachaaddiction$isInit = isInit;
    }

    @Override
    public List<@Nullable ResourceKey<LootTable>> gachaaddiction$getLootTableKey() {
        return gachaaddiction$lootTableKey;
    }

    @Override
    public void gachaaddiction$setLootTableKey(@Nullable List<@Nullable ResourceKey<LootTable>> key) {
        if (key == null) {
            this.gachaaddiction$lootTableKey = new ArrayList<>();
            return;
        }
        this.gachaaddiction$lootTableKey = key;
    }

    @Override
    public List<ItemStackEntry> gachaaddiction$getDisplayEntries() {
        return gachaaddiction$displayEntries;
    }

    @Override
    public void gachaaddiction$setDisplayEntries(@Nullable List<ItemStackEntry> entries) {
        if (entries == null) {
            this.gachaaddiction$displayEntries = new ArrayList<>();
            return;
        }
        this.gachaaddiction$displayEntries = entries;
    }
}

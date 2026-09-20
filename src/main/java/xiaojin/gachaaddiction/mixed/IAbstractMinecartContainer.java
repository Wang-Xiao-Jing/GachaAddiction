package xiaojin.gachaaddiction.mixed;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecartContainer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.Nullable;

public interface IAbstractMinecartContainer {
    static IAbstractMinecartContainer of(AbstractMinecartContainer menu) {
        return (IAbstractMinecartContainer) menu;
    }

    boolean gachaaddiction$isOpenGachaaUi();

    void gachaaddiction$setOpenGachaaUi(boolean isOpen);

    @Nullable
    ResourceKey<LootTable> gachaaddiction$getOldLootTable();
}

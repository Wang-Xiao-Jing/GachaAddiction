package xiaojin.gachaaddiction.util;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.storage.loot.LootTable;
import noobanidus.mods.lootr.common.api.interfaces.inventory.ILootrInventory;
import noobanidus.mods.lootr.common.data.LootrInventory;
import org.jetbrains.annotations.Nullable;
import xiaojin.gachaaddiction.mixed.IILootrInventory;

public class LootrUtil {

    public static boolean isInstanceofILootrInventory(MenuProvider provider) {
        return provider instanceof ILootrInventory;
    }

    @Nullable
    public static ResourceKey<LootTable> getInfoLootTable(MenuProvider provider) {
        return ((ILootrInventory) provider).getData().getDataLootTable();
    }

    /**
     * 本会话内打开过，或 Lootr 自己记录过已打开（跨存档保留）。
     */
    public static boolean isOpen(MenuProvider provider) {
        if (getIiLootrInventory(provider).gachaaddiction$isOpen()) {
            return true;
        }
        return ((ILootrInventory) provider).getData().hasBeenOpened();
    }

    public static void setOpen(MenuProvider provider, boolean isOpen) {
        getIiLootrInventory(provider).gachaaddiction$setOpen(isOpen);
    }

    private static IILootrInventory getIiLootrInventory(MenuProvider provider) {
        return IILootrInventory.of((LootrInventory) provider);
    }
}

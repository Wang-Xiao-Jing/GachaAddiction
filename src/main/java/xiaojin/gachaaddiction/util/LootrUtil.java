package xiaojin.gachaaddiction.util;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.storage.loot.LootTable;
import noobanidus.mods.lootr.common.api.data.inventory.ILootrInventory;
import noobanidus.mods.lootr.common.data.LootrInventory;
import org.jetbrains.annotations.Nullable;
import xiaojin.gachaaddiction.mixed.IILootrInventory;

public class LootrUtil {

    public static boolean isInstanceofILootrInventory(MenuProvider provider) {
        return provider instanceof ILootrInventory;
    }

    @Nullable
    public static ResourceKey<LootTable> getInfoLootTable(MenuProvider provider) {
        return ((ILootrInventory) provider).getInfo().getInfoLootTable();
    }

    public static boolean isOpen(MenuProvider provider) {
        return getIiLootrInventory(provider).gachaaddiction$isOpen();
    }

    public static void setOpen(MenuProvider provider, boolean isOpen) {
        getIiLootrInventory(provider).gachaaddiction$setOpen(isOpen);
    }

    private static IILootrInventory getIiLootrInventory(MenuProvider provider) {
        return IILootrInventory.of((LootrInventory) provider);
    }
}

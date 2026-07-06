package xiaojin.gachaaddiction.util;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.storage.loot.LootTable;
import noobanidus.mods.lootr.common.api.data.inventory.ILootrInventory;
import xiaojin.gachaaddiction.GachaAddiction;
import xiaojin.gachaaddiction.mixed.IILootrInventory;

public class LootrUtil {
    public static boolean isLoaded() {
        return GachaAddiction.LOOTR_LOADED;
    }

    public static boolean isInstanceofILootrInventory(MenuProvider provider) {
        return isLoaded() && provider instanceof ILootrInventory;
    }

    public static ResourceKey<LootTable> getInfoLootTable(MenuProvider provider) {
        return ((ILootrInventory) provider).getInfo().getInfoLootTable();
    }

    public static boolean isOpen(MenuProvider provider) {
        return IILootrInventory.of(((ILootrInventory) provider)).gachaaddiction$isOpen();
    }
}

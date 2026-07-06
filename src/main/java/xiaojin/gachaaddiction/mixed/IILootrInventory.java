package xiaojin.gachaaddiction.mixed;

import noobanidus.mods.lootr.common.api.data.inventory.ILootrInventory;

public interface IILootrInventory {
    default boolean gachaaddiction$isOpen() {
        return false;
    }

    default void gachaaddiction$setOpen(boolean isOpen) {
    }

    static IILootrInventory of(ILootrInventory inventory) {
        return (IILootrInventory) inventory;
    }
}

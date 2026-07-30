package xiaojin.gachaaddiction.mixed;

import noobanidus.mods.lootr.common.data.LootrInventory;

public interface IILootrInventory {
    default boolean gachaaddiction$isOpen() {
        return false;
    }

    default void gachaaddiction$setOpen(boolean isOpen) {
    }

    static IILootrInventory of(LootrInventory inventory) {
        return (IILootrInventory) inventory;
    }
}

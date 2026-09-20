package xiaojin.gachaaddiction.mixin.compat.lootr;

import noobanidus.mods.lootr.common.api.data.inventory.ILootrInventory;
import noobanidus.mods.lootr.common.data.LootrInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import xiaojin.gachaaddiction.mixed.IILootrInventory;

@Pseudo
@Mixin(LootrInventory.class)
public abstract class LootrInventoryMixin implements ILootrInventory, IILootrInventory {
    @Unique
    private boolean gachaaddiction$isOpen;

    @Override
    public boolean gachaaddiction$isOpen() {
        return gachaaddiction$isOpen;
    }

    @Override
    public void gachaaddiction$setOpen(boolean isOpen) {
        this.gachaaddiction$isOpen = isOpen;
    }
}

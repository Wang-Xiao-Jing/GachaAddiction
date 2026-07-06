package xiaojin.gachaaddiction.mixin.lootr;

import noobanidus.mods.lootr.common.api.data.inventory.ILootrInventory;
import org.spongepowered.asm.mixin.Mixin;
import xiaojin.gachaaddiction.mixed.IILootrInventory;

@Mixin(ILootrInventory.class)
public interface ILootrInventoryMixin extends IILootrInventory {
}

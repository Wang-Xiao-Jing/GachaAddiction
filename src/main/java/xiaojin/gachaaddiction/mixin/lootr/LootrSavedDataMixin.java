package xiaojin.gachaaddiction.mixin.lootr;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import noobanidus.mods.lootr.common.api.data.ILootrSavedData;
import noobanidus.mods.lootr.common.data.LootrInventory;
import noobanidus.mods.lootr.common.data.LootrSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xiaojin.gachaaddiction.mixed.IILootrInventory;

@Mixin(LootrSavedData.class)
public abstract class LootrSavedDataMixin {
    @WrapOperation(method = "load", at = @At(value = "NEW", target = "(Lnoobanidus/mods/lootr/common/api/data/ILootrSavedData;Lnet/minecraft/core/NonNullList;)Lnoobanidus/mods/lootr/common/data/LootrInventory;"))
    private static LootrInventory gachaaddiction$load(ILootrSavedData info, NonNullList<ItemStack> contents, Operation<LootrInventory> original, @Local(name = "itemTag") CompoundTag itemTag) {
        LootrInventory inventory = original.call(info, contents);
        IILootrInventory.of(inventory).gachaaddiction$setOpen(itemTag.getBoolean("gachaaddiction$isOpen"));
        return inventory;
    }
}

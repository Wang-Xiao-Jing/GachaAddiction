package xiaojin.gachaaddiction.mixin.compat.lootr;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import noobanidus.mods.lootr.common.api.data.inventory.ILootrInventory;
import noobanidus.mods.lootr.common.data.LootrInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
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

    @Inject(method = "saveToTag", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/ContainerHelper;saveAllItems(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/NonNullList;Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/nbt/CompoundTag;"))
    private void gachaaddiction$saveToTag$saveAllItems(HolderLookup.Provider provider, CallbackInfoReturnable<CompoundTag> cir, @Local(name = "result") CompoundTag result) {
        result.putBoolean("gachaaddiction$isOpen", gachaaddiction$isOpen);
    }
}

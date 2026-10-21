package xiaojin.gachaaddiction.mixin.entity;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xiaojin.gachaaddiction.presentation.lock.GachaLockedItemHandler;

@Mixin(value = ItemCapability.class, remap = false)
public abstract class GachaItemCapabilityMixin {
    @Inject(method = "getCapability", at = @At("RETURN"), cancellable = true)
    private void gachaaddiction$protectHandler(ItemStack stack, Object context, CallbackInfoReturnable<Object> cir) {
        if ((Object) this == Capabilities.ItemHandler.ITEM && cir.getReturnValue() instanceof IItemHandler handler) {
            cir.setReturnValue(GachaLockedItemHandler.wrap(handler, stack));
        }
    }
}

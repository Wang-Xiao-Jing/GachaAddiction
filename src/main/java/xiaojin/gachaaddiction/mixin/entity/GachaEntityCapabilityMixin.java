package xiaojin.gachaaddiction.mixin.entity;

import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xiaojin.gachaaddiction.presentation.lock.GachaLockedItemHandler;

@Mixin(value = EntityCapability.class, remap = false)
public abstract class GachaEntityCapabilityMixin {
    @Inject(method = "getCapability", at = @At("RETURN"), cancellable = true)
    private void gachaaddiction$protectHandler(Entity entity, Object context, CallbackInfoReturnable<Object> cir) {
        if (((Object) this == Capabilities.ItemHandler.ENTITY || (Object) this == Capabilities.ItemHandler.ENTITY_AUTOMATION)
                && cir.getReturnValue() instanceof IItemHandler handler) {
            cir.setReturnValue(GachaLockedItemHandler.wrap(handler, entity));
        }
    }
}

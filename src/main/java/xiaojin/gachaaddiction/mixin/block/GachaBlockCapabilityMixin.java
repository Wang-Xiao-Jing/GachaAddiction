package xiaojin.gachaaddiction.mixin.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xiaojin.gachaaddiction.presentation.lock.GachaLockedItemHandler;

@Mixin(value = BlockCapability.class, remap = false)
public abstract class GachaBlockCapabilityMixin {
    @Inject(method = "getCapability", at = @At("RETURN"), cancellable = true)
    private void gachaaddiction$protectHandler(Level level, BlockPos pos, @Nullable BlockState state,
                                               @Nullable BlockEntity blockEntity, Object context, CallbackInfoReturnable<Object> cir) {
        if ((Object) this == Capabilities.ItemHandler.BLOCK && cir.getReturnValue() instanceof IItemHandler handler) {
            BlockEntity holder = blockEntity == null ? level.getBlockEntity(pos) : blockEntity;
            if (holder != null) {
                cir.setReturnValue(GachaLockedItemHandler.wrap(handler, holder));
            }
        }
    }
}

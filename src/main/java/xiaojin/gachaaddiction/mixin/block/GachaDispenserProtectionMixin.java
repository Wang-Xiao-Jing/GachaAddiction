package xiaojin.gachaaddiction.mixin.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.DropperBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xiaojin.gachaaddiction.util.GachaPresentations;

@Mixin({DispenserBlock.class, DropperBlock.class})
public abstract class GachaDispenserProtectionMixin {
    @Inject(method = "dispenseFrom", at = @At("HEAD"), cancellable = true)
    private void gachaaddiction$denyDispensing(ServerLevel level, BlockState state, BlockPos pos, CallbackInfo ci) {
        var holder = level.getBlockEntity(pos);
        if (holder != null && GachaPresentations.isLocked(holder)) {
            ci.cancel();
        }
    }
}

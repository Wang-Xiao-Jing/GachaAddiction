package xiaojin.gachaaddiction.mixin.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.Hopper;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xiaojin.gachaaddiction.presentation.GachaPresentations;
import xiaojin.gachaaddiction.cardpool.delivery.GachaPendingRewardDrops;

@Mixin(HopperBlockEntity.class)
public abstract class GachaHopperProtectionMixin {
    @Inject(method = "addItem(Lnet/minecraft/world/Container;Lnet/minecraft/world/entity/item/ItemEntity;)Z",
            at = @At("HEAD"), cancellable = true)
    private static void gachaaddiction$denyPendingDrop(Container container, ItemEntity item,
                                                      CallbackInfoReturnable<Boolean> cir) {
        if (GachaPendingRewardDrops.isPending(item)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "tryTakeInItemFromSlot", at = @At("HEAD"), cancellable = true)
    private static void gachaaddiction$denyExtraction(Hopper hopper, Container container, int slot, Direction direction,
                                                      CallbackInfoReturnable<Boolean> cir) {
        if (GachaPresentations.isLocked(container)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "ejectItems", at = @At("HEAD"), cancellable = true)
    private static void gachaaddiction$denyEjection(Level level, BlockPos pos, HopperBlockEntity hopper,
                                                    CallbackInfoReturnable<Boolean> cir) {
        if (GachaPresentations.isLocked(hopper)) {
            cir.setReturnValue(false);
        }
    }
}

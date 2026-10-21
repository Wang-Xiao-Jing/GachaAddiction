package xiaojin.gachaaddiction.mixin.entity;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xiaojin.gachaaddiction.cardpool.delivery.GachaPendingRewardDrops;
import xiaojin.gachaaddiction.cardpool.GachaServerRuntimeManager;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
    @Inject(method = "playerTouch", at = @At("HEAD"), cancellable = true)
    private void gachaaddiction$protectPendingPickup(Player player, CallbackInfo ci) {
        if (GachaPendingRewardDrops.isPending((ItemEntity)(Object)this)) {
            ci.cancel();
        }
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void gachaaddiction$freezePendingDrop(CallbackInfo ci) {
        ItemEntity item = (ItemEntity) (Object) this;
        if (GachaPendingRewardDrops.isPending(item)) {
            if (!item.level().isClientSide
                    && item.level().getGameTime() % GachaPendingRewardDrops.RECONCILE_INTERVAL_TICKS == 0) {
                GachaServerRuntimeManager.current().ifPresent(runtime -> runtime.reconcilePendingDrop(item));
            }
            if (item.isRemoved() || GachaPendingRewardDrops.isPending(item)) {
                ci.cancel();
            }
        }
    }
}

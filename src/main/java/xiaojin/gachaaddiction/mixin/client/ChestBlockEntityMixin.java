package xiaojin.gachaaddiction.mixin.client;

import net.minecraft.world.level.block.entity.ChestBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xiaojin.gachaaddiction.client.render.GachaSceneRenderer;

@Mixin(ChestBlockEntity.class)
public class ChestBlockEntityMixin {
    @Inject(method = "getOpenNess(F)F", at = @At("HEAD"), cancellable = true)
    private void gachaaddiction$previewOpenNess(float partialTick, CallbackInfoReturnable<Float> callback) {
        float opening = GachaSceneRenderer.overrideOpenNess((ChestBlockEntity) (Object) this);
        if (opening >= 0.0F) {
            callback.setReturnValue(opening);
        }
    }
}


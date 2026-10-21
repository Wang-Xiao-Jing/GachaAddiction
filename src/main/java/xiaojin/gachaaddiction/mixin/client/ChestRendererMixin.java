package xiaojin.gachaaddiction.mixin.client;

import net.minecraft.client.renderer.blockentity.ChestRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xiaojin.gachaaddiction.client.render.GachaChestRenderOverride;

@Mixin(ChestRenderer.class)
public class ChestRendererMixin {
    @ModifyVariable(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/client/model/geom/ModelPart;Lnet/minecraft/client/model/geom/ModelPart;Lnet/minecraft/client/model/geom/ModelPart;FII)V",
            at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float gachaaddiction$previewOpenNess(float original) {
        float opening = GachaChestRenderOverride.overrideRenderedOpenNess();
        if (opening < 0.0F) {
            return original;
        }
        float closed = 1.0F - opening;
        return 1.0F - closed * closed * closed;
    }
}

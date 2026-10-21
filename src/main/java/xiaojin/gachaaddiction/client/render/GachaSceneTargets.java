package xiaojin.gachaaddiction.client.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

public class GachaSceneTargets implements AutoCloseable {
    private @Nullable RenderTarget chestTarget;
    private @Nullable RenderTarget reflectionTarget;

    public @Nullable RenderTarget chest() {
        return chestTarget;
    }

    public RenderTarget prepareChest(RenderTarget reference) {
        if (chestTarget == null || chestTarget.width != reference.width || chestTarget.height != reference.height) {
            if (chestTarget != null) {
                chestTarget.destroyBuffers();
            }
            chestTarget = new TextureTarget(reference.width, reference.height, true, Minecraft.ON_OSX);
            chestTarget.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        }
        return chestTarget;
    }

    public RenderTarget prepareReflection(RenderTarget reference) {
        if (reflectionTarget == null || reflectionTarget.width != reference.width || reflectionTarget.height != reference.height) {
            if (reflectionTarget != null) {
                reflectionTarget.destroyBuffers();
            }
            reflectionTarget = new TextureTarget(reference.width, reference.height, true, Minecraft.ON_OSX);
            reflectionTarget.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        }
        return reflectionTarget;
    }

    private void releaseChest() {
        if (chestTarget != null) {
            chestTarget.destroyBuffers();
            chestTarget = null;
        }
    }

    private void releaseReflection() {
        if (reflectionTarget != null) {
            reflectionTarget.destroyBuffers();
            reflectionTarget = null;
        }
    }

    @Override
    public void close() {
        if (chestTarget == null && reflectionTarget == null) {
            return;
        }
        try (GachaRenderState previousState = new GachaRenderState()) {
            try (TargetRelease chest = this::releaseChest; TargetRelease reflection = this::releaseReflection) {
            } finally {
                Minecraft.getInstance().getMainRenderTarget().bindWrite(true);
            }
        }
    }

    @FunctionalInterface
    private interface TargetRelease extends AutoCloseable {
        @Override
        void close();
    }
}


package xiaojin.gachaaddiction.client.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.GachaConstants;

import java.io.IOException;

public class GachaReelFade implements AutoCloseable {
    private static final ResourceLocation EFFECT = ResourceLocation.fromNamespaceAndPath(
            GachaConstants.MODID, "shaders/post/reel_fade.json");
    private static final String BACKGROUND_TARGET = "background";
    private static final String TOP_UNIFORM = "FadeTop";
    private static final String BOTTOM_UNIFORM = "FadeBottom";
    private static final String WARNING = "Cannot render gacha reel fade {}; using per-item opacity";
    private static final long UNLOADED_REVISION = -1;
    public static long resourceRevision;
    private long loadedRevision = UNLOADED_REVISION;
    private PostChain chain;
    private RenderTarget mainTarget;
    private int width;
    private int height;
    private boolean active;

    public boolean begin(GuiGraphics graphics) {
        active = false;
        try (GachaRenderState previousState = new GachaRenderState()) {
            try {
                graphics.flush();
                RenderTarget current = Minecraft.getInstance().getMainRenderTarget();
                if (mainTarget != current || width != current.width || height != current.height
                        || loadedRevision != resourceRevision) {
                    release();
                    mainTarget = current;
                    width = current.width;
                    height = current.height;
                    loadedRevision = resourceRevision;
                    chain = new PostChain(Minecraft.getInstance().getTextureManager(),
                            Minecraft.getInstance().getResourceManager(), current, EFFECT);
                    chain.resize(width, height);
                }
                if (chain == null) {
                    return false;
                }

                RenderTarget background = chain.getTempTarget(BACKGROUND_TARGET);
                background.clear(Minecraft.ON_OSX);
                background.bindWrite(true);
                mainTarget.blitToScreen(width, height, true);
                active = true;
                return true;
            } catch (IOException | RuntimeException failure) {
                GachaConstants.LOGGER.warn(WARNING, EFFECT, failure);
                release();
                return false;
            } finally {
                Minecraft.getInstance().getMainRenderTarget().bindWrite(true);
            }
        }
    }

    public void finish(GuiGraphics graphics, int top, int bottom) {
        if (!active || chain == null) {
            return;
        }

        active = false;
        try (GachaRenderState previousState = new GachaRenderState()) {
            try {
                graphics.flush();
                chain.setUniform(TOP_UNIFORM, top / (float) graphics.guiHeight());
                chain.setUniform(BOTTOM_UNIFORM, bottom / (float) graphics.guiHeight());
                chain.process(0.0F);
            } catch (RuntimeException failure) {
                GachaConstants.LOGGER.warn(WARNING, EFFECT, failure);
                release();
            } finally {
                Minecraft.getInstance().getMainRenderTarget().bindWrite(true);
            }
        }
    }

    private void release() {
        if (chain != null) {
            chain.close();
            chain = null;
        }
    }

    @Override
    public void close() {
        try (GachaRenderState previousState = new GachaRenderState()) {
            try {
                active = false;
                release();
                mainTarget = null;
                loadedRevision = UNLOADED_REVISION;
            } finally {
                Minecraft.getInstance().getMainRenderTarget().bindWrite(true);
            }
        }
    }
}

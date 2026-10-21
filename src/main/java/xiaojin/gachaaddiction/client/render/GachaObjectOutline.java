package xiaojin.gachaaddiction.client.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.lwjgl.opengl.*;
import xiaojin.gachaaddiction.GachaConstants;

import java.io.IOException;
import java.util.function.Consumer;

public class GachaObjectOutline implements AutoCloseable {
    private static final ResourceLocation EFFECT = ResourceLocation.fromNamespaceAndPath(
            GachaConstants.MODID, "shaders/post/object_outline.json");
    private static final String OBJECT = "object";
    private static final String OUTPUT = "swap";
    private static final float OUTLINE_DEPTH = 300.0F;
    private static final String WIDTH_UNIFORM = "OutlineWidth";
    private static final String WARNING = "Cannot render gacha object outline {}; disabled until resources change";
    private static final long UNLOADED_REVISION = -1;
    public static long resourceRevision;
    private long loadedRevision = UNLOADED_REVISION;
    private PostChain chain;
    private RenderTarget mainTarget;
    private int width;
    private int height;

    public void render(GuiGraphics graphics, Consumer<RenderTarget> draw) {
        Minecraft minecraft = Minecraft.getInstance();
        try (OutlineTargetState targetState = new OutlineTargetState(); GachaRenderState previousState = new GachaRenderState()) {
            try {
                graphics.flush();
                RenderTarget current = minecraft.getMainRenderTarget();
                if (targetState.drawFramebuffer != current.frameBufferId) {
                    return;
                }

                if (mainTarget != current || width != current.width || height != current.height
                        || loadedRevision != resourceRevision) {
                    release();
                    mainTarget = current;
                    width = current.width;
                    height = current.height;
                    loadedRevision = resourceRevision;
                    chain = new PostChain(minecraft.getTextureManager(), minecraft.getResourceManager(), current, EFFECT);
                    chain.resize(width, height);
                }
                if (chain == null) {
                    return;
                }

                try (GachaRenderState objectState = new GachaRenderState()) {
                    // 离屏目标完整清空，横向裁剪只在最终合成时应用。
                    RenderSystem.disableScissor();
                    RenderSystem.colorMask(true, true, true, true);
                    RenderSystem.depthMask(true);
                    RenderSystem.disableColorLogicOp();
                    RenderSystem.blendEquation(GL14.GL_FUNC_ADD);
                    RenderTarget object = chain.getTempTarget(OBJECT);
                    object.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
                    object.clear(Minecraft.ON_OSX);
                    object.bindWrite(true);
                    draw.accept(object);
                    graphics.flush();
                    chain.setUniform(WIDTH_UNIFORM, (float) minecraft.getWindow().getGuiScale());
                    chain.process(0.0F);
                }
                current.bindWrite(true);
                compositeOutline(graphics, chain.getTempTarget(OUTPUT));
            } catch (IOException | RuntimeException failure) {
                GachaConstants.LOGGER.warn(WARNING, EFFECT, failure);
                release();
            }
        }
    }

    private void compositeOutline(GuiGraphics graphics, RenderTarget outline) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, outline.getColorTextureId());
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendEquation(GL14.GL_FUNC_ADD);
        RenderSystem.disableColorLogicOp();
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        Matrix4f matrix = graphics.pose().last().pose();
        buffer.addVertex(matrix, 0.0F, 0.0F, OUTLINE_DEPTH).setUv(0.0F, 1.0F);
        buffer.addVertex(matrix, 0.0F, graphics.guiHeight(), OUTLINE_DEPTH).setUv(0.0F, 0.0F);
        buffer.addVertex(matrix, graphics.guiWidth(), graphics.guiHeight(), OUTLINE_DEPTH).setUv(1.0F, 0.0F);
        buffer.addVertex(matrix, graphics.guiWidth(), 0.0F, OUTLINE_DEPTH).setUv(1.0F, 1.0F);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private void release() {
        if (chain != null) {
            chain.close();
            chain = null;
        }
    }

    @Override
    public void close() {
        try (OutlineTargetState targetState = new OutlineTargetState(); GachaRenderState previousState = new GachaRenderState()) {
            release();
            mainTarget = null;
            loadedRevision = UNLOADED_REVISION;
        }
    }

    // GlStateBackup不包含帧缓冲绑定、视口、裁剪矩形、活动纹理、清空值及混合方程。
    private static class OutlineTargetState implements AutoCloseable {
        private final int drawFramebuffer = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
        private final int readFramebuffer = GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
        private final int rgbEquation = GL11.glGetInteger(GL20.GL_BLEND_EQUATION_RGB);
        private final int alphaEquation = GL11.glGetInteger(GL20.GL_BLEND_EQUATION_ALPHA);
        private final int activeTexture = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
        private final int[] viewport = new int[4];
        private final int[] scissor = new int[4];
        private final float[] clearColor = new float[4];
        private final double clearDepth = GL11.glGetDouble(GL11.GL_DEPTH_CLEAR_VALUE);

        private OutlineTargetState() {
            GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
            GL11.glGetIntegerv(GL11.GL_SCISSOR_BOX, scissor);
            GL11.glGetFloatv(GL11.GL_COLOR_CLEAR_VALUE, clearColor);
        }

        @Override
        public void close() {
            GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, drawFramebuffer);
            GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, readFramebuffer);
            RenderSystem.viewport(viewport[0], viewport[1], viewport[2], viewport[3]);
            GlStateManager._scissorBox(scissor[0], scissor[1], scissor[2], scissor[3]);
            RenderSystem.activeTexture(activeTexture);
            GL20.glBlendEquationSeparate(rgbEquation, alphaEquation);
            GlStateManager._clearColor(clearColor[0], clearColor[1], clearColor[2], clearColor[3]);
            GlStateManager._clearDepth(clearDepth);
        }
    }
}

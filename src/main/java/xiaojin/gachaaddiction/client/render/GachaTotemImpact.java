package xiaojin.gachaaddiction.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;

public class GachaTotemImpact {
    public static final long ACTIVATE_MILLIS = 250;
    private static final long DURATION_MILLIS = 600;
    private static final long FLASH_RISE_MILLIS = 70;
    private static final long FLASH_FADE_MILLIS = 180;
    private static final long RING_MILLIS = 500;
    private static final long PARTICLE_MILLIS = 550;
    private static final long NOT_STARTED = -1;
    private static final int SEGMENTS = 48;
    private static final int PARTICLES = 12;
    private static final float FULL_TURN = (float) (Math.PI * 2.0);
    private static final float DEPTH = 400.0F;
    private static final float SWELL = 0.04F;
    private static final float WHITE_MIX = 0.55F;
    private static final float CORE_RADIUS = 0.35F;
    private static final float CORE_ALPHA = 0.4F;
    private static final float RING_START_RADIUS = 0.12F;
    private static final float RING_TRAVEL = 0.42F;
    private static final float RING_WIDTH = 0.035F;
    private static final float RING_ALPHA = 0.65F;
    private static final float PARTICLE_START_RADIUS = 0.1F;
    private static final float PARTICLE_TRAVEL = 0.62F;
    private static final float PARTICLE_SIZE = 0.012F;
    private static final float PARTICLE_GRAVITY = 0.04F;
    private long startedAt = NOT_STARTED;
    private float centerX;
    private float centerY;
    private float size;
    private float red;
    private float green;
    private float blue;

    public void play(long now, float centerX, float centerY, float size, int color) {
        startedAt = now;
        this.centerX = centerX;
        this.centerY = centerY;
        this.size = size;
        red = WHITE_MIX + (1.0F - WHITE_MIX) * (color >> 16 & 255) / 255.0F;
        green = WHITE_MIX + (1.0F - WHITE_MIX) * (color >> 8 & 255) / 255.0F;
        blue = WHITE_MIX + (1.0F - WHITE_MIX) * (color & 255) / 255.0F;
    }

    public static float swell(long elapsedMillis) {
        float progress = GachaAnimationRenderer.smoothProgress((float) elapsedMillis / ACTIVATE_MILLIS);
        return 1.0F + SWELL * (float) Math.sin(Math.PI * progress);
    }

    public void render(GuiGraphics graphics, long now) {
        long elapsed = now - startedAt;
        if (startedAt == NOT_STARTED || elapsed < 0 || elapsed >= DURATION_MILLIS) {
            return;
        }

        try (GachaRenderState previousState = new GachaRenderState()) {
            graphics.flush();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.disableCull();
            BufferBuilder vertices = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
            Matrix4f matrix = graphics.pose().last().pose();
            float rise = GachaAnimationRenderer.smoothProgress((float) elapsed / FLASH_RISE_MILLIS);
            float flash = rise * (1.0F - GachaAnimationRenderer.smoothProgress((float) (elapsed - FLASH_RISE_MILLIS) / FLASH_FADE_MILLIS));
            band(vertices, matrix, centerX, centerY, 0.0F, size * CORE_RADIUS, flash * CORE_ALPHA, 0.0F);
            float travel = GachaAnimationRenderer.smoothProgress((float) elapsed / RING_MILLIS);
            float radius = size * (RING_START_RADIUS + RING_TRAVEL * travel);
            float half = size * RING_WIDTH * (1.0F - travel);
            float alpha = rise * (1.0F - travel) * RING_ALPHA;
            band(vertices, matrix, centerX, centerY, Math.max(0.0F, radius - half), radius, 0.0F, alpha);
            band(vertices, matrix, centerX, centerY, radius, radius + half, alpha, 0.0F);
            renderParticles(vertices, matrix, elapsed, rise);
            BufferUploader.drawWithShader(vertices.buildOrThrow());
        }
    }

    private void renderParticles(BufferBuilder vertices, Matrix4f matrix, long elapsed, float rise) {
        float progress = GachaAnimationRenderer.smoothProgress((float) elapsed / PARTICLE_MILLIS);
        for (int index = 0; index < PARTICLES; index++) {
            float angle = FULL_TURN * index / PARTICLES;
            float variation = (index * 0.37F) % 1.0F;
            float radius = size * (PARTICLE_START_RADIUS + PARTICLE_TRAVEL * progress * (0.6F + 0.4F * variation));
            float x = centerX + (float) Math.cos(angle) * radius;
            float y = centerY + (float) Math.sin(angle) * radius + size * PARTICLE_GRAVITY * progress * progress;
            float half = size * PARTICLE_SIZE * (1.0F - progress);
            band(vertices, matrix, x, y, 0.0F, half, rise * (1.0F - progress), 0.0F);
        }
    }

    private void band(BufferBuilder vertices, Matrix4f matrix, float x, float y, float inner, float outer,
                       float innerAlpha, float outerAlpha) {
        for (int index = 0; index < SEGMENTS; index++) {
            float first = FULL_TURN * index / SEGMENTS;
            float second = FULL_TURN * (index + 1) / SEGMENTS;
            float firstX = (float) Math.cos(first);
            float firstY = (float) Math.sin(first);
            float secondX = (float) Math.cos(second);
            float secondY = (float) Math.sin(second);
            vertex(vertices, matrix, x + firstX * inner, y + firstY * inner, innerAlpha);
            vertex(vertices, matrix, x + firstX * outer, y + firstY * outer, outerAlpha);
            vertex(vertices, matrix, x + secondX * outer, y + secondY * outer, outerAlpha);
            vertex(vertices, matrix, x + firstX * inner, y + firstY * inner, innerAlpha);
            vertex(vertices, matrix, x + secondX * outer, y + secondY * outer, outerAlpha);
            vertex(vertices, matrix, x + secondX * inner, y + secondY * inner, innerAlpha);
        }
    }

    private void vertex(BufferBuilder vertices, Matrix4f matrix, float x, float y, float alpha) {
        vertices.addVertex(matrix, x, y, DEPTH).setColor(red, green, blue, alpha);
    }
}

package xiaojin.gachaaddiction.client.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import xiaojin.gachaaddiction.GachaConstants;
import xiaojin.gachaaddiction.api.RewardData;

import java.io.IOException;
import java.util.function.Consumer;

public class ModernGachaEffect implements AutoCloseable {
    private static final ResourceLocation EFFECT =
            ResourceLocation.fromNamespaceAndPath(GachaConstants.MODID, "shaders/post/modern.json");
    private static final String ITEM_TARGET = "item";
    private static final String REVEAL_UNIFORM = "RevealProgress";
    private static final String TIME_UNIFORM = "EffectTime";
    private static final String RED_UNIFORM = "RarityRed";
    private static final String GREEN_UNIFORM = "RarityGreen";
    private static final String BLUE_UNIFORM = "RarityBlue";
    private static final String BACKDROP_OFFSET_UNIFORM = "BackdropOffset";
    private static final String ENTRANCE_OPACITY_UNIFORM = "EntranceOpacity";
    private static final String BACKDROP_PROGRESS_UNIFORM = "BackdropProgress";
    private static final String RIM_PROGRESS_UNIFORM = "RimProgress";
    private static final String LOAD_WARNING = "Cannot load modern gacha shader {}; using the basic item view";
    private static final String RENDER_WARNING = "Cannot render modern gacha shader {}; disabling it until resources change";
    private static final long UNLOADED_REVISION = -1;
    private static final float NATIVE_ITEM_SIZE = 16.0F;
    private static final float COLOR_CHANNEL_MAXIMUM = 255.0F;
    public static long resourceRevision;
    private long loadedRevision = UNLOADED_REVISION;
    private @Nullable PostChain chain;
    private @Nullable RenderTarget mainTarget;
    private int targetWidth;
    private int targetHeight;

    public boolean render(GuiGraphics graphics, RewardData reward, int left, int top, int size,
                          float seconds, float reveal) {
        return renderLayer(graphics, reward, reward.getRarityColor(), left, top, size, seconds, reveal);
    }

    public boolean render(GuiGraphics graphics, RewardData reward, int left, int top, int size,
                          float seconds, float reveal, ModernGachaEntrance.Frame entrance) {
        return renderLayer(graphics, reward, reward.getRarityColor(), left, top, size, seconds, reveal, 0.0F, null, entrance);
    }

    public boolean renderBackdrop(GuiGraphics graphics, int color, float seconds) {
        return renderLayer(graphics, null, color, 0, 0, 0, seconds, 0.0F);
    }

    public boolean renderBackdrop(GuiGraphics graphics, int color, float seconds, ModernGachaEntrance.Frame entrance) {
        return renderLayer(graphics, null, color, 0, 0, 0, seconds, 0.0F, 0.0F, null, entrance);
    }

    public boolean renderItems(GuiGraphics graphics, int color, float seconds, float reveal, Consumer<GuiGraphics> draw) {
        return renderItems(graphics, color, seconds, reveal, draw, ModernGachaEntrance.Frame.VISIBLE);
    }

    public boolean renderItems(GuiGraphics graphics, int color, float seconds, float reveal, Consumer<GuiGraphics> draw,
                               ModernGachaEntrance.Frame entrance) {
        return renderLayer(graphics, null, color, 0, 0, 0, seconds, reveal, 0.0F, draw, entrance);
    }

    public boolean renderBackdrop(GuiGraphics graphics, int color, float seconds, float verticalOffset) {
        return renderLayer(graphics, null, color, 0, 0, 0, seconds, 0.0F, verticalOffset);
    }

    private boolean renderLayer(GuiGraphics graphics, @Nullable RewardData reward, int color,
                                int left, int top, int size, float seconds, float reveal) {
        return renderLayer(graphics, reward, color, left, top, size, seconds, reveal, 0.0F);
    }

    private boolean renderLayer(GuiGraphics graphics, @Nullable RewardData reward, int color,
                                int left, int top, int size, float seconds, float reveal, float verticalOffset) {
        return renderLayer(graphics, reward, color, left, top, size, seconds, reveal, verticalOffset, null,
                ModernGachaEntrance.Frame.VISIBLE);
    }

    private boolean renderLayer(GuiGraphics graphics, @Nullable RewardData reward, int color,
                                int left, int top, int size, float seconds, float reveal, float verticalOffset,
                                @Nullable Consumer<GuiGraphics> draw, ModernGachaEntrance.Frame entrance) {
        Minecraft minecraft = Minecraft.getInstance();
        GachaRenderState previousState = new GachaRenderState();
        try {
            graphics.flush();
            if (!prepare(minecraft) || chain == null || mainTarget == null) {
                return false;
            }
            // 由 PostChain 持有和释放目标，原版物品渲染提供方块、特殊模型和附魔的实际轮廓。
            RenderTarget itemTarget = chain.getTempTarget(ITEM_TARGET);
            itemTarget.clear(Minecraft.ON_OSX);
            itemTarget.bindWrite(true);
            graphics.pose().pushPose();
            try {
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                if (reward != null) {
                    graphics.enableScissor(left, top, left + size, top + size);
                    try {
                        graphics.pose().translate(left, top, 0);
                        entrance.apply(graphics, size / 2.0F, size / 2.0F);
                        float scale = size / NATIVE_ITEM_SIZE;
                        graphics.pose().scale(scale, scale, 1.0F);
                        graphics.renderFakeItem(reward.getItemStack(), 0, 0);
                        graphics.flush();
                    } finally {
                        graphics.disableScissor();
                    }
                } else if (draw != null) {
                    draw.accept(graphics);
                }
                graphics.flush();
            } finally {
                graphics.pose().popPose();
                mainTarget.bindWrite(true);
            }

            chain.setUniform(RED_UNIFORM, (color >> 16 & 255) / COLOR_CHANNEL_MAXIMUM);
            chain.setUniform(GREEN_UNIFORM, (color >> 8 & 255) / COLOR_CHANNEL_MAXIMUM);
            chain.setUniform(BLUE_UNIFORM, (color & 255) / COLOR_CHANNEL_MAXIMUM);
            chain.setUniform(TIME_UNIFORM, seconds);
            chain.setUniform(REVEAL_UNIFORM, reveal);
            chain.setUniform(BACKDROP_OFFSET_UNIFORM, verticalOffset);
            chain.setUniform(ENTRANCE_OPACITY_UNIFORM, entrance.opacity());
            chain.setUniform(BACKDROP_PROGRESS_UNIFORM, entrance.backdrop());
            chain.setUniform(RIM_PROGRESS_UNIFORM, entrance.rim());
            chain.process(0.0F);
            return true;
        } catch (RuntimeException failure) {
            GachaConstants.LOGGER.warn(RENDER_WARNING, EFFECT, failure);
            release();
            return false;
        } finally {
            try {
                if (mainTarget != null) {
                    mainTarget.bindWrite(true);
                }
            } finally {
                previousState.close();
            }
        }
    }

    private boolean prepare(Minecraft minecraft) {
        RenderTarget currentTarget = minecraft.getMainRenderTarget();
        if (loadedRevision == resourceRevision && mainTarget == currentTarget
                && targetWidth == currentTarget.width && targetHeight == currentTarget.height) {
            return chain != null;
        }

        release();
        loadedRevision = resourceRevision;
        mainTarget = currentTarget;
        targetWidth = currentTarget.width;
        targetHeight = currentTarget.height;
        try {
            chain = new PostChain(minecraft.getTextureManager(), minecraft.getResourceManager(), currentTarget, EFFECT);
            chain.resize(targetWidth, targetHeight);
            return true;
        } catch (IOException | RuntimeException failure) {
            GachaConstants.LOGGER.warn(LOAD_WARNING, EFFECT, failure);
            release();
            return false;
        } finally {
            currentTarget.bindWrite(true);
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
                release();
            } finally {
                try {
                    Minecraft.getInstance().getMainRenderTarget().bindWrite(true);
                } finally {
                    mainTarget = null;
                    loadedRevision = UNLOADED_REVISION;
                }
            }
        }
    }
}

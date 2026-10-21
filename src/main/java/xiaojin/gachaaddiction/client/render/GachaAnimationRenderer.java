package xiaojin.gachaaddiction.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import xiaojin.gachaaddiction.GachaAddiction;
import xiaojin.gachaaddiction.api.GachaType;
import xiaojin.gachaaddiction.client.compat.colortooltips.ColorTooltipsBorderRenderer;
import xiaojin.gachaaddiction.registry.GachaTypes;
import xiaojin.gachaaddiction.registry.item.GachaDrawItems;
import xiaojin.gachaaddiction.util.RarityUtil;

import java.util.List;

public class GachaAnimationRenderer {
    private static final float PANEL_WIDTH_FRACTION = 0.8F;
    private static final float PANEL_HEIGHT_FRACTION = 0.7F;
    private static final int BACKGROUND = 0xFF090A13;
    private static final int GOLD = 0xFFF3CE76;
    private static final int SECTORS = 12;
    private static final int RING_SEGMENTS = 96;
    private static final int REEL_COUNT = 3;
    private static final int FRAME_SIZE = 18;
    private static final int ITEM_GAP = 1;
    private static final int SLOT_VISIBLE_ROWS = 5;
    private static final int MARKER_EXTENSION = 5;
    private static final int POINTER_HALF = 5;
    private static final int POINTER_HEIGHT = 8;
    private static final float MARKER_DEPTH = 500.0F;
    private static final float FRAME_DEPTH = 200.0F;
    private static final int OPAQUE_ALPHA = 0xFF000000;
    private static final String COLOR_TOOLTIP_WARNING = "Cannot render gacha item borders with ColorTooltips";
    private static final float NATIVE_ITEM_SIZE = 16.0F;
    private static final float ITEM_DEPTH = 100.0F;
    private static final float CENTER = 0.5F;
    private static final float WHEEL_RADIUS_FRACTION = 0.34F;
    private static final float CHEST_SIZE = 0.68F;
    private static final float FULL_TURN = (float) (Math.PI * 2.0);
    private static final float NEEDLE_ANGLE = (float) (-Math.PI / 2.0);
    private static boolean colorTooltipsFailed;
    private static float borderFlow;

    public static Layout layout(int width, int height) {
        int panelWidth = Math.max(1, (int) (width * PANEL_WIDTH_FRACTION));
        int panelHeight = Math.max(1, (int) (height * PANEL_HEIGHT_FRACTION));
        return new Layout((width - panelWidth) / 2, (height - panelHeight) / 2, panelWidth, panelHeight);
    }

    public static void render(GuiGraphics graphics, GachaType type, Layout layout, List<ItemStack> icons,
                              float motion, float seconds, float charge, boolean running,
                              GachaSceneRenderer scene, ItemStack trigger, GachaReelFade reelFade, int lightColor) {
        if (GachaAddiction.COLORTOOLTIPS_LOADED && !colorTooltipsFailed
                && type != GachaTypes.CHEST && type != GachaTypes.TOTEM) {
            try {
                borderFlow = ColorTooltipsBorderRenderer.updateFlow(0.0F);
            } catch (RuntimeException | LinkageError failure) {
                colorTooltipsFailed = true;
                GachaAddiction.LOGGER.warn(COLOR_TOOLTIP_WARNING, failure);
            }
        }
        if (type == GachaTypes.CHEST) {
            scene.renderChest(graphics, layout, seconds, charge, trigger, lightColor);
        } else if (type == GachaTypes.TOTEM) {
            scene.renderTotem(graphics, seconds, trigger);
        } else if (type == GachaTypes.CSGO) {
            strip(graphics, layout, icons, motion);
        } else if (type == GachaTypes.WHEEL) {
            wheel(graphics, layout, icons, motion);
        } else if (type == GachaTypes.INTERACTIVE_SLOT_MACHINE) {
            slots(graphics, layout, icons, motion, running, reelFade);
        }
    }

    public static int backgroundColor() {
        return BACKGROUND;
    }

    public static float smoothProgress(float progress) {
        float value = Math.clamp(progress, 0.0F, 1.0F);
        return value * value * value * (value * (value * 6.0F - 15.0F) + 10.0F);
    }

    public static int accentColor() {
        return GOLD;
    }

    public static boolean overChest(Layout layout, double x, double y) {
        return layout.contains(x, y, CENTER - CHEST_SIZE / 2, CENTER - CHEST_SIZE / 2,
                CENTER + CHEST_SIZE / 2, CENTER + CHEST_SIZE / 2);
    }


    public static boolean overWheel(Layout layout, double x, double y) {
        double radius = Math.min(layout.width, layout.height) * WHEEL_RADIUS_FRACTION;
        return Math.hypot(x - layout.x(CENTER), y - layout.y(CENTER)) <= radius;
    }

    public static float wheelAngle(Layout layout, double x, double y) {
        return (float) Math.atan2(y - layout.y(CENTER), x - layout.x(CENTER));
    }


    private static void strip(GuiGraphics graphics, Layout layout, List<ItemStack> icons, float motion) {
        int pitch = FRAME_SIZE + ITEM_GAP;
        int top = layout.y(CENTER) - FRAME_SIZE / 2;
        int offset = (int) (motion * pitch);
        int base = Math.floorDiv(offset, pitch);
        int remainder = Math.floorMod(offset, pitch);
        ItemStack fallback = icons.isEmpty() ? GachaDrawItems.GACHA_KEY.toStack() : ItemStack.EMPTY;
        graphics.enableScissor(layout.left, top, layout.left + layout.width, top + FRAME_SIZE);
        try {
            for (int index = -1; index <= layout.width / pitch + 1; index++) {
                int x = layout.left + index * pitch - remainder;
                framedItem(graphics, icon(icons, index + base, fallback),
                        x + FRAME_SIZE / 2, top + FRAME_SIZE / 2);
            }
        } finally {
            graphics.disableScissor();
        }
        graphics.flush();
        graphics.pose().pushPose();
        try {
            graphics.pose().translate(0, 0, MARKER_DEPTH);
            graphics.fill(layout.x(CENTER), top - MARKER_EXTENSION, layout.x(CENTER) + 1,
                    top + FRAME_SIZE + MARKER_EXTENSION, GOLD);
            graphics.flush();
        } finally {
            graphics.pose().popPose();
        }
    }

    private static void wheel(GuiGraphics graphics, Layout layout, List<ItemStack> icons, float motion) {
        int centerX = layout.x(CENTER);
        int centerY = layout.y(CENTER);
        int radius = Math.max(FRAME_SIZE, (int) (Math.min(layout.width, layout.height) * WHEEL_RADIUS_FRACTION));
        int count = Math.max(1, Math.min(SECTORS, (int) (FULL_TURN * radius / (FRAME_SIZE + ITEM_GAP))));
        ItemStack fallback = icons.isEmpty() ? GachaDrawItems.WHEEL_TICKET.toStack() : ItemStack.EMPTY;
        for (int index = 0; index < count; index++) {
            float angle = motion + index * FULL_TURN / count;
            framedItem(graphics, icon(icons, index, fallback),
                    centerX + (int) (Math.cos(angle) * radius), centerY + (int) (Math.sin(angle) * radius));
        }
        graphics.flush();
        graphics.pose().pushPose();
        try {
            graphics.pose().translate(0, 0, MARKER_DEPTH);
            ring(graphics, centerX, centerY, radius, GOLD);
            graphics.flush();
        } finally {
            graphics.pose().popPose();
        }
        try (GachaRenderState previousState = new GachaRenderState()) {
            graphics.flush();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableCull();
            BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
            Matrix4f matrix = graphics.pose().last().pose();
            int pointerY = centerY - radius - FRAME_SIZE / 2;
            buffer.addVertex(matrix, centerX - POINTER_HALF, pointerY - POINTER_HEIGHT, MARKER_DEPTH).setColor(GOLD);
            buffer.addVertex(matrix, centerX + POINTER_HALF, pointerY - POINTER_HEIGHT, MARKER_DEPTH).setColor(GOLD);
            buffer.addVertex(matrix, centerX, pointerY, MARKER_DEPTH).setColor(GOLD);
            BufferUploader.drawWithShader(buffer.buildOrThrow());
        }
    }

    private static void slots(GuiGraphics graphics, Layout layout, List<ItemStack> icons, float motion, boolean running,
                              GachaReelFade reelFade) {
        int pitch = FRAME_SIZE + ITEM_GAP;
        int centerY = layout.y(CENTER);
        int top = Math.max(layout.top, centerY - SLOT_VISIBLE_ROWS * pitch / 2);
        int bottom = Math.min(layout.top + layout.height, centerY + SLOT_VISIBLE_ROWS * pitch / 2);
        int left = layout.x(CENTER) - REEL_COUNT * pitch / 2;
        ItemStack fallback = icons.isEmpty() ? GachaDrawItems.SLOT_TOKEN.toStack() : ItemStack.EMPTY;
        boolean fading = reelFade.begin(graphics);
        try {
            for (int reel = 0; reel < REEL_COUNT; reel++) {
                int centerX = left + reel * pitch + FRAME_SIZE / 2;
                float position = running ? motion + reel : reel;
                int scroll = (int) (position * pitch);
                int base = Math.floorDiv(scroll, pitch);
                int offset = Math.floorMod(scroll, pitch);
                graphics.enableScissor(centerX - FRAME_SIZE / 2, top, centerX + FRAME_SIZE / 2, bottom);
                try {
                    for (int row = -SLOT_VISIBLE_ROWS; row <= SLOT_VISIBLE_ROWS; row++) {
                        int itemY = centerY + row * pitch - offset;
                        float opacity = fading ? 1.0F : Math.clamp(1.0F - Math.abs(itemY - centerY)
                                / (float) Math.max(1, (bottom - top) / 2), 0.0F, 1.0F);
                        framedItem(graphics, icon(icons, row + base + reel, fallback), centerX, itemY, opacity);
                    }
                } finally {
                    graphics.disableScissor();
                }
            }
        } finally {
            reelFade.finish(graphics, top, bottom);
        }
        graphics.flush();
        graphics.pose().pushPose();
        try {
            graphics.pose().translate(0, 0, MARKER_DEPTH);
            graphics.hLine(left, left + REEL_COUNT * pitch - ITEM_GAP, centerY, GOLD);
            graphics.flush();
        } finally {
            graphics.pose().popPose();
        }
    }

    private static void framedItem(GuiGraphics graphics, ItemStack stack, int centerX, int centerY) {
        framedItem(graphics, stack, centerX, centerY, 1.0F);
    }

    private static void framedItem(GuiGraphics graphics, ItemStack stack, int centerX, int centerY, float opacity) {
        if (opacity <= 0.0F) {
            return;
        }
        item(graphics, stack, centerX, centerY, (int) NATIVE_ITEM_SIZE, opacity);
        GachaRenderState previousState = new GachaRenderState();
        graphics.pose().pushPose();
        try {
            graphics.pose().translate(0, 0, FRAME_DEPTH);
            graphics.setColor(1.0F, 1.0F, 1.0F, opacity);
            boolean custom = false;
            if (GachaAddiction.COLORTOOLTIPS_LOADED && !colorTooltipsFailed) {
                try {
                    custom = ColorTooltipsBorderRenderer.render(
                            graphics, stack, centerX - FRAME_SIZE / 2, centerY - FRAME_SIZE / 2, FRAME_SIZE,
                            borderFlow);
                } catch (RuntimeException | LinkageError failure) {
                    colorTooltipsFailed = true;
                    GachaAddiction.LOGGER.warn(COLOR_TOOLTIP_WARNING, failure);
                }
            }
            if (!custom) {
                frame(graphics, centerX - FRAME_SIZE / 2, centerY - FRAME_SIZE / 2, FRAME_SIZE, FRAME_SIZE,
                        (RarityUtil.getRarityColor(stack) & ~OPAQUE_ALPHA) | Math.round(opacity * (OPAQUE_ALPHA >>> 24)) << 24);
            }
        } finally {
            try {
                graphics.flush();
            } finally {
                graphics.pose().popPose();
                previousState.close();
            }
        }
    }

    private static void ring(GuiGraphics graphics, int x, int y, int radius, int color) {
        for (int index = 0; index < RING_SEGMENTS; index++) {
            float angle = NEEDLE_ANGLE + index * FULL_TURN / RING_SEGMENTS;
            int pointX = x + (int) (Math.cos(angle) * radius);
            int pointY = y + (int) (Math.sin(angle) * radius);
            graphics.fill(pointX, pointY, pointX + 2, pointY + 2, color);
        }
    }

    private static ItemStack icon(List<ItemStack> icons, int index, ItemStack fallback) {
        return icons.isEmpty() ? fallback : icons.get(Math.floorMod(index, icons.size()));
    }

    private static void item(GuiGraphics graphics, ItemStack stack, int centerX, int centerY, int size, float opacity) {
        GachaRenderState previousState = new GachaRenderState();
        graphics.pose().pushPose();
        try {
            graphics.pose().translate(centerX - size / 2.0F, centerY - size / 2.0F, ITEM_DEPTH);
            graphics.pose().scale(size / NATIVE_ITEM_SIZE, size / NATIVE_ITEM_SIZE, 1.0F);
            GuiItemRenderer.renderFakeItem(graphics, stack, 0, 0, opacity);
        } finally {
            graphics.pose().popPose();
            previousState.close();
        }
    }

    private static void frame(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        graphics.hLine(x, x + width - 1, y, color);
        graphics.hLine(x, x + width - 1, y + height - 1, color);
        graphics.vLine(x, y, y + height - 1, color);
        graphics.vLine(x + width - 1, y, y + height - 1, color);
    }

    public record Layout(int left, int top, int width, int height) {
        public int x(float fraction) {
            return left + (int) (width * fraction);
        }

        public int y(float fraction) {
            return top + (int) (height * fraction);
        }

        public boolean contains(double x, double y, float left, float top, float right, float bottom) {
            return x >= x(left) && x < x(right) && y >= y(top) && y < y(bottom);
        }
    }
}

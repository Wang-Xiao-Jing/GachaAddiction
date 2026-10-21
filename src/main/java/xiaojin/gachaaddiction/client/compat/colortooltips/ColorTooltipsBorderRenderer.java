package xiaojin.gachaaddiction.client.compat.colortooltips;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.yanbwe.colortooltips.config.ConfigManager;
import org.yanbwe.colortooltips.config.DynamicColorResolver;
import org.yanbwe.colortooltips.util.TooltipRenderer;

public class ColorTooltipsBorderRenderer {
    private static final String DEFAULT_COLOR_TOKEN = "@rarityCore";

    public static float updateFlow(float partialTick) {
        var animator = ConfigManager.getInstance().getColorFlowAnimator();
        animator.update();
        return animator.getInterpolatedFlowFraction(partialTick);
    }

    public static boolean render(GuiGraphics graphics, ItemStack stack, int left, int top, int size, float flow) {
        var config = ConfigManager.getInstance();
        if (!config.isEnabled()) {
            return false;
        }

        var border = config.getStyleForStack(stack, false).getBorder();
        var entries = border.getFillColor();
        int[] colors;
        if (entries == null || entries.isEmpty()) {
            colors = new int[]{DynamicColorResolver.resolve(DEFAULT_COLOR_TOKEN, stack)};
        } else {
            colors = new int[entries.size()];
            for (int index = 0; index < entries.size(); index++) {
                colors[index] = DynamicColorResolver.resolve(entries.get(index), stack);
            }
        }

        graphics.flush();
        try {
            TooltipRenderer.drawGradientScrollingBorder(graphics, left, top, size, size, colors,
                    1.0F, size, size, border, flow);
            graphics.flush();
            return true;
        } finally {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
        }
    }
}

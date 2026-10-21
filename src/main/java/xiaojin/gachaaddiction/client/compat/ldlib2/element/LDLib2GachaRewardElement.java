package xiaojin.gachaaddiction.client.compat.ldlib2.element;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import xiaojin.gachaaddiction.client.compat.ldlib2.LDLib2GachaUiConstants;
import xiaojin.gachaaddiction.client.render.GuiItemRenderer;

public class LDLib2GachaRewardElement extends UIElement {
    private final ItemStack reward;

    public LDLib2GachaRewardElement(ItemStack reward) {
        this.reward = reward.copy();
        addClass(LDLib2GachaUiConstants.REWARD_CLASS);
        layout(layout -> layout.width(LDLib2GachaUiConstants.REWARD_SIZE)
                .height(LDLib2GachaUiConstants.REWARD_SIZE));
    }

    @Override
    public void drawBackgroundAdditional(@NotNull GUIContext context) {
        super.drawBackgroundAdditional(context);
        int x = (int) getPositionX() + LDLib2GachaUiConstants.REWARD_PADDING;
        int y = (int) getPositionY() + LDLib2GachaUiConstants.REWARD_PADDING;
        GuiItemRenderer.renderItem(context.graphics, reward, x, y, 1.0f);
        context.graphics.renderItemDecorations(context.mc.font, reward, x, y);
        if (isMouseOver(context.mouseX, context.mouseY)) {
            int mouseX = (int) context.localMouseX;
            int mouseY = (int) context.localMouseY;
            context.postRendering(gui -> gui.graphics.renderTooltip(gui.mc.font, reward, mouseX, mouseY));
        }
    }
}

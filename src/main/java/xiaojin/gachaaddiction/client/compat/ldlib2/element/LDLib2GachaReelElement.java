package xiaojin.gachaaddiction.client.compat.ldlib2.element;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import org.jetbrains.annotations.NotNull;
import xiaojin.gachaaddiction.client.compat.ldlib2.LDLib2GachaUiConstants;
import xiaojin.gachaaddiction.client.gui.widget.VerticalReelWidget;

public class LDLib2GachaReelElement extends UIElement {
    private final VerticalReelWidget reel;

    public LDLib2GachaReelElement(VerticalReelWidget reel, int height) {
        this.reel = reel;
        addClass(LDLib2GachaUiConstants.REWARD_CLASS);
        layout(layout -> layout.width(LDLib2GachaUiConstants.REWARD_SIZE).height(height));
    }

    @Override
    public void drawBackgroundAdditional(@NotNull GUIContext context) {
        super.drawBackgroundAdditional(context);
        reel.setX((int) (getPositionX() + getSizeWidth() / 2));
        reel.setY((int) (getPositionY() + getSizeHeight() / 2));
        reel.render(context.graphics, (int) context.localMouseX, (int) context.localMouseY, context.partialTick);
    }
}

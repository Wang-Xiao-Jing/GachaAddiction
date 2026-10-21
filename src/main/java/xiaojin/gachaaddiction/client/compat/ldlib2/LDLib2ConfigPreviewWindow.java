package xiaojin.gachaaddiction.client.compat.ldlib2;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Dialog;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.style.StylesheetManager;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import xiaojin.gachaaddiction.GachaConstants;
import xiaojin.gachaaddiction.client.compat.ldlib2.element.LDLib2GachaPreviewElement;
import xiaojin.gachaaddiction.config.GachaAddictionConfig;
import xiaojin.gachaaddiction.registry.GachaTypes;

import java.util.List;

import static xiaojin.gachaaddiction.util.TranslationKeys.*;

public class LDLib2ConfigPreviewWindow extends Dialog {
    private static final float WINDOW_WIDTH = 280;
    private static final float WINDOW_HEIGHT = 220;
    private static final float MAXIMUM_SCREEN_FRACTION = 0.8F;
    private static final float CONTENT_GAP = 4;
    private static final String EDITOR_CLASS = "__editor__";

    public LDLib2ConfigPreviewWindow(float mouseX, float mouseY) {
        addClass(EDITOR_CLASS);
        addLocalStylesheet(StylesheetManager.INSTANCE.getStylesheetSafe(StylesheetManager.ORE));
        titleBar.moveInlineAsDefault();
        contentContainer.moveInlineAsDefault();
        buttonContainer.moveInlineAsDefault();
        overlay.moveInlineAsDefault();
        titleBar.addChild(new Label().setText(Component.translatable(GUI_CONFIG_PREVIEW)));
        var preview = new LDLib2GachaPreviewElement();
        var viewport = new PreviewViewport(preview, this::close);
        var hint = new Label().setText(Component.translatable(GUI_CONFIG_PREVIEW_HINT));
        hint.layout(layout -> layout.widthPercent(100));
        hint.textStyle(style -> style.adaptiveWidth(false).adaptiveHeight(true).textWrap(TextWrap.WRAP));
        contentContainer.addChildren(hint, viewport);
        contentContainer.layout(layout -> layout.minHeight(0).gapAll(CONTENT_GAP)
                .alignItems(AlignItems.STRETCH).justifyContent(AlignContent.FLEX_START));
        addButton(new Button().setText(Component.translatable(GUI_CONFIG_PREVIEW_REPLAY))
                .setOnClick(event -> preview.replay()));
        addButton(new Button().setText(Component.translatable(GUI_ADMIN_WORKBENCH_CLOSE))
                .setOnClick(event -> close()));
        allowInteraction();
        var window = Minecraft.getInstance().getWindow();
        windowMode(mouseX, mouseY,
                Math.min(WINDOW_WIDTH, window.getGuiScaledWidth() * MAXIMUM_SCREEN_FRACTION),
                Math.min(WINDOW_HEIGHT, window.getGuiScaledHeight() * MAXIMUM_SCREEN_FRACTION));
        setAutoClose(false);
        setClickOutsideClose(false);
    }

    private static class PreviewViewport extends UIElement {
        private final LDLib2GachaPreviewElement preview;
        private final Runnable close;
        private List<?> profile = List.of();

        private PreviewViewport(LDLib2GachaPreviewElement preview, Runnable close) {
            this.preview = preview;
            this.close = close;
            layout(layout -> layout.widthPercent(100).flex(1).minHeight(0));
            refresh();
        }

        private static List<?> currentProfile() {
            var config = GachaAddictionConfig.CLIENT;
            return List.of(config.getDefaultGachaaType(), config.ldlib2GachaTemplate.get(),
                    config.ldlib2GachaStyleSheet.get());
        }

        @Override
        public void screenTick() {
            super.screenTick();
            if (!profile.equals(currentProfile())) {
                refresh();
            }
        }

        private void refresh() {
            profile = currentProfile();
            preview.removeSelf();
            clearAllChildren();
            if (GachaAddictionConfig.CLIENT.getDefaultGachaaType() == GachaTypes.LDLIB2) {
                try {
                    var root = LDLib2GachaResources.createPreviewRoot(preview, preview::replay, close);
                    root.layout(layout -> layout.widthPercent(100).heightPercent(100).minHeight(0));
                    addChild(root);
                    return;
                } catch (RuntimeException failure) {
                    GachaConstants.LOGGER.warn(LDLib2GachaUiConstants.FALLBACK_LOG, failure);
                }
            }
            addChild(preview);
        }
    }
}

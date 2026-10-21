package xiaojin.gachaaddiction.client.compat.ldlib2;

import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextField;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TreeList;
import com.lowdragmc.lowdraglib2.gui.ui.style.StylesheetManager;
import com.lowdragmc.lowdraglib2.gui.util.TreeNode;
import dev.vfyjxf.taffy.style.FlexDirection;
import net.minecraft.network.chat.Component;

public final class LDLib2EditorTheme {
    private static final float SCREEN_PERCENT = 90;
    private static final float NAVIGATION_PERCENT = 28;
    private static final float SEARCH_HEIGHT = 18;
    private static final float TOOLBAR_HEIGHT = 22;
    private static final float GAP = 4;

    private LDLib2EditorTheme() {
    }

    public static UI createUI(UIElement root) {
        return UI.of(root, StylesheetManager.ORE);
    }

    public static UIElement createAdminRoot() {
        var root = new UIElement().addClasses("__editor__", "panel_bg");
        root.layout(layout -> layout.widthPercent(SCREEN_PERCENT).heightPercent(SCREEN_PERCENT)
                .minHeight(0).gapAll(GAP)).moveInlineAsDefault();
        var navigationSearch = new TextField();
        navigationSearch.setId("navigation_search_box");
        navigationSearch.layout(layout -> layout.widthPercent(100).height(SEARCH_HEIGHT)).moveInlineAsDefault();
        var navigation = new TreeList<TreeNode<Component, Void>>();
        navigation.setId("navigation_content");
        navigation.layout(layout -> layout.widthPercent(100).flex(1).minHeight(0)).moveInlineAsDefault();
        var navigationScroller = new ScrollerView();
        navigationScroller.scrollerStyle(style -> style.adaptiveHeight(false));
        navigationScroller.layout(layout -> layout.widthPercent(100).flex(1).minHeight(0)).moveInlineAsDefault();
        navigationScroller.addScrollViewChild(navigation);
        var navigationPane = new UIElement().addClass("__view-container__");
        navigationPane.layout(layout -> layout.widthPercent(NAVIGATION_PERCENT).heightPercent(100)
                .minHeight(0).gapAll(GAP)).moveInlineAsDefault();
        navigationPane.addChildren(navigationSearch, navigationScroller);

        var optionsSearch = new TextField();
        optionsSearch.setId("options_search_box");
        optionsSearch.layout(layout -> layout.widthPercent(100).height(SEARCH_HEIGHT)).moveInlineAsDefault();
        var content = new ScrollerView();
        content.setId("config_options_container");
        content.scrollerStyle(style -> style.adaptiveHeight(false));
        content.layout(layout -> layout.widthPercent(100).flex(1).minHeight(0)).moveInlineAsDefault();
        content.viewPort.layout(layout -> layout.minHeight(0));
        content.viewContainer.layout(layout -> layout.gapAll(GAP));
        var optionsPane = new UIElement().addClass("__view-container__");
        optionsPane.layout(layout -> layout.flex(1).heightPercent(100).minWidth(0).minHeight(0)
                .gapAll(GAP)).moveInlineAsDefault();
        optionsPane.addChildren(optionsSearch, content);
        var main = new UIElement();
        main.layout(layout -> layout.widthPercent(100).flex(1).minHeight(0)
                .flexDirection(FlexDirection.ROW).gapAll(GAP)).moveInlineAsDefault();
        main.addChildren(navigationPane, optionsPane);

        var action = new Button();
        action.setId("preview_window_button");
        var close = new Button();
        close.setId("finish_button");
        var toolbar = new UIElement().addClass("__editor_top__");
        toolbar.layout(layout -> layout.widthPercent(100).height(TOOLBAR_HEIGHT)
                .flexDirection(FlexDirection.ROW).gapAll(GAP)).moveInlineAsDefault();
        toolbar.addChildren(action, close);
        return root.addChildren(main, toolbar);
    }
}

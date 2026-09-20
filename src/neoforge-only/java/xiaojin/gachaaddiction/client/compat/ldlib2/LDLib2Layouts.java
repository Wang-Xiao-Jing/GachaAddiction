package xiaojin.gachaaddiction.client.compat.ldlib2;

import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;

public final class LDLib2Layouts {
    private LDLib2Layouts() {
    }

    public static void constrainScrollerHeight(ScrollerView scrollerView) {
        constrainScrollerHeight(scrollerView, true);
    }

    public static void constrainScrollerHeight(ScrollerView scrollerView, boolean constrainParent) {
        scrollerView.layout(layout -> layout.flex(1).minHeight(0));
        scrollerView.scrollerStyle(style -> style.adaptiveHeight(false));
        scrollerView.verticalContainer.layout(layout -> layout.minHeight(0));
        scrollerView.viewPort.layout(layout -> layout.minHeight(0));
        if (constrainParent && scrollerView.getParent() != null) {
            scrollerView.getParent().layout(layout -> layout.minHeight(0));
        }
    }
}

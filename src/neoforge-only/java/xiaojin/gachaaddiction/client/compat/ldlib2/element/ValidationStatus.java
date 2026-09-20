package xiaojin.gachaaddiction.client.compat.ldlib2.element;

import com.lowdragmc.lowdraglib2.gui.ui.elements.TextElement;

import static xiaojin.gachaaddiction.util.TranslationKeys.GUI_CONFIG_FILTER_CONDITION_INVALID;

final class ValidationStatus {
    private static final String EMPTY_MARKER = "?";
    private static final String VALID_MARKER = "√";
    private static final String INVALID_MARKER = "!";
    private static final int EMPTY_COLOR = 0xFFFFFF55;
    private static final int VALID_COLOR = 0xFF55FF55;
    private static final int INVALID_COLOR = 0xFFFF5555;

    private ValidationStatus() {
    }

    static void update(TextElement element, boolean empty, boolean invalid, String emptyTooltip) {
        element.setText(empty ? EMPTY_MARKER : invalid ? INVALID_MARKER : VALID_MARKER);
        element.getTextStyle().textColor(empty ? EMPTY_COLOR : invalid ? INVALID_COLOR : VALID_COLOR);
        element.style(style -> style.tooltips(empty ? new String[]{emptyTooltip}
                : invalid ? new String[]{GUI_CONFIG_FILTER_CONDITION_INVALID} : new String[0]));
        element.setVisible(true).setDisplay(true);
    }
}

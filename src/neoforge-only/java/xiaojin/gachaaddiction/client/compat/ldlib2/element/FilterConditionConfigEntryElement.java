package xiaojin.gachaaddiction.client.compat.ldlib2.element;

import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextElement;

import static xiaojin.gachaaddiction.util.TranslationKeys.GUI_CONFIG_FILTER_CONDITION_EMPTY;

public class FilterConditionConfigEntryElement extends SearchConfigEntryElement {
    private final TextElement descriptionElement;

    public FilterConditionConfigEntryElement() {
        getTextElement().setVisible(false).setDisplay(false);
        select("#reset_button", Button.class).findFirst().orElseThrow()
                .setVisible(false)
                .setDisplay(false);
        descriptionElement = select("#describe", TextElement.class).findFirst().orElseThrow();
        refreshValidationState();
    }

    public void refreshValidationState() {
        boolean empty = getValue().isBlank();
        boolean invalid = !empty && getTextField().isError();
        ValidationStatus.update(descriptionElement, empty, invalid, GUI_CONFIG_FILTER_CONDITION_EMPTY);
    }

    @Override
    public FilterConditionConfigEntryElement setValue(String value) {
        super.setValue(value);
        refreshValidationState();
        return this;
    }
}

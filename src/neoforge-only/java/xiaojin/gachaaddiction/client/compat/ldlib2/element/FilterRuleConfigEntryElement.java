package xiaojin.gachaaddiction.client.compat.ldlib2.element;

import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextElement;

import java.util.List;
import java.util.function.Predicate;

import static xiaojin.gachaaddiction.util.TranslationKeys.GUI_CONFIG_FILTER_RULE_EMPTY;

public class FilterRuleConfigEntryElement extends ListConfigEntryElement<String> {
    private final Predicate<List<String>> validator;
    private final TextElement descriptionElement;

    public FilterRuleConfigEntryElement(Predicate<List<String>> validator) {
        this.validator = validator;
        setAllowEmpty(true);
        getTextElement().setVisible(false).setDisplay(false);
        select("#reset_button", Button.class).findFirst().orElseThrow()
                .setVisible(false)
                .setDisplay(false);
        descriptionElement = select("#describe", TextElement.class).findFirst().orElseThrow();
        refreshValidationState();
    }

    public void refreshValidationState() {
        boolean empty = getValue().isEmpty();
        boolean invalid = !empty && !validator.test(getValue());
        ValidationStatus.update(descriptionElement, empty, invalid, GUI_CONFIG_FILTER_RULE_EMPTY);
    }

    @Override
    public FilterRuleConfigEntryElement setValue(List<String> value) {
        super.setValue(value);
        refreshValidationState();
        return this;
    }

    @Override
    public FilterRuleConfigEntryElement setValue(List<String> value, boolean notify) {
        super.setValue(value, notify);
        refreshValidationState();
        return this;
    }
}

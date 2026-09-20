package xiaojin.gachaaddiction.client.compat.ldlib2.element;

import com.lowdragmc.lowdraglib2.editor.resource.IResourcePath;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextField;
import org.jetbrains.annotations.Nullable;
import xiaojin.gachaaddiction.client.compat.ldlib2.LDLib2Templates;

import java.util.function.Predicate;

public abstract class FieldConfigEntryElement<T> extends BasicConfigEntryElement<T> {
    protected final TextField valueElement;

    protected FieldConfigEntryElement() {
        this(LDLib2Templates.FIELD_CONFIG_ENTRY);
    }

    protected FieldConfigEntryElement(@Nullable IResourcePath templatePath) {
        super(templatePath);
        valueElement = select("#value", TextField.class).findFirst().orElseThrow();
    }

    public TextField getValueElement() {
        return valueElement;
    }

    public FieldConfigEntryElement<T> setCharValidator(Predicate<Character> charValidator) {
        getValueElement().setCharValidator(charValidator);
        return this;
    }
}

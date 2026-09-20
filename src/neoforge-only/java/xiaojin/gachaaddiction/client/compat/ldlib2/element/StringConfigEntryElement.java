package xiaojin.gachaaddiction.client.compat.ldlib2.element;

import com.lowdragmc.lowdraglib2.editor.resource.IResourcePath;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class StringConfigEntryElement extends FieldConfigEntryElement<String> {
    public StringConfigEntryElement() {
        getValueElement().setAnyString();
    }

    @SuppressWarnings("unused")
    public StringConfigEntryElement(Predicate<Character> charValidator) {
        getValueElement().setAnyString()
                .setCharValidator(charValidator);
    }

    @SuppressWarnings("unused")
    public StringConfigEntryElement(@Nullable IResourcePath templatePath) {
        super(templatePath);
        getValueElement().setAnyString();
    }

    @SuppressWarnings("unused")
    public StringConfigEntryElement(@Nullable IResourcePath templatePath, Predicate<Character> charValidator) {
        super(templatePath);
        getValueElement().setAnyString()
                .setCharValidator(charValidator);
    }

    @Override
    public String getValue() {
        return getValueElement().getValue();
    }

    @Override
    public StringConfigEntryElement setText(String text) {
        return (StringConfigEntryElement) super.setText(text);
    }

    @Override
    public StringConfigEntryElement setText(Component component) {
        return (StringConfigEntryElement) super.setText(component);
    }

    @Override
    public StringConfigEntryElement setCharValidator(Predicate<Character> charValidator) {
        return (StringConfigEntryElement) super.setCharValidator(charValidator);
    }

    @Override
    public StringConfigEntryElement setValue(String value) {
        getValueElement().setValue(value);
        return this;
    }
}

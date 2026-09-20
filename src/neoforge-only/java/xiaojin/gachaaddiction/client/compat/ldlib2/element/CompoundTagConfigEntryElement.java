package xiaojin.gachaaddiction.client.compat.ldlib2.element;

import com.lowdragmc.lowdraglib2.editor.resource.IResourcePath;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

@SuppressWarnings("unused")
public class CompoundTagConfigEntryElement extends FieldConfigEntryElement<CompoundTag> {
    public CompoundTagConfigEntryElement() {
        getValueElement().setCompoundTagOnly();
    }

    public CompoundTagConfigEntryElement(@Nullable IResourcePath templatePath) {
        super(templatePath);
        getValueElement().setCompoundTagOnly();
    }

    @Override
    public CompoundTag getValue() {
        try {
            return TagParser.parseTag(getValueElement().getValue());
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    @Override
    public CompoundTagConfigEntryElement setText(String text) {
        return (CompoundTagConfigEntryElement) super.setText(text);
    }

    @Override
    public CompoundTagConfigEntryElement setText(Component component) {
        return (CompoundTagConfigEntryElement) super.setText(component);
    }

    @Override
    public CompoundTagConfigEntryElement setCharValidator(Predicate<Character> charValidator) {
        return (CompoundTagConfigEntryElement) super.setCharValidator(charValidator);
    }

    @Override
    public CompoundTagConfigEntryElement setValue(CompoundTag value) {
        getValueElement().setValue(value.toString());
        return this;
    }
}

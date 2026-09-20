package xiaojin.gachaaddiction.client.compat.ldlib2.element;

import com.lowdragmc.lowdraglib2.editor.resource.IResourcePath;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

@SuppressWarnings("unused")
public class ResourceLocationConfigEntryElement extends FieldConfigEntryElement<ResourceLocation> {
    public ResourceLocationConfigEntryElement() {
        getValueElement().setResourceLocationOnly();
    }

    public ResourceLocationConfigEntryElement(@Nullable IResourcePath templatePath) {
        super(templatePath);
        getValueElement().setResourceLocationOnly();
    }

    @Override
    public ResourceLocation getValue() {
        return ResourceLocation.parse(getValueElement().getValue());
    }

    @Override
    public ResourceLocationConfigEntryElement setText(String text) {
        return (ResourceLocationConfigEntryElement) super.setText(text);
    }

    @Override
    public ResourceLocationConfigEntryElement setText(Component component) {
        return (ResourceLocationConfigEntryElement) super.setText(component);
    }

    @Override
    public ResourceLocationConfigEntryElement setCharValidator(Predicate<Character> charValidator) {
        return (ResourceLocationConfigEntryElement) super.setCharValidator(charValidator);
    }

    @Override
    public ResourceLocationConfigEntryElement setValue(ResourceLocation value) {
        getValueElement().setValue(value.toString());
        return this;
    }
}

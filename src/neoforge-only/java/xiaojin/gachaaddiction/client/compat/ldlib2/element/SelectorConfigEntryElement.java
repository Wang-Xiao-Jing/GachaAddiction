package xiaojin.gachaaddiction.client.compat.ldlib2.element;

import com.lowdragmc.lowdraglib2.editor.resource.IResourcePath;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Selector;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import xiaojin.gachaaddiction.client.compat.ldlib2.LDLib2Templates;

import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings("UnusedReturnValue")
public class SelectorConfigEntryElement<T> extends BasicConfigEntryElement<T> {
    private final Selector<T> valueElement;

    public SelectorConfigEntryElement() {
        this(LDLib2Templates.SELECTOR_CONFIG_ENTRY);
    }

    @SuppressWarnings("unchecked")
    public SelectorConfigEntryElement(@Nullable IResourcePath templatePath) {
        super(templatePath);
        valueElement = (Selector<T>) select("#value", Selector.class).findFirst().orElseThrow();
    }

    public Selector<T> getValueElement() {
        return valueElement;
    }

    public SelectorConfigEntryElement<T> setCandidates(List<T> candidates) {
        getValueElement().setCandidates(candidates);
        return this;
    }

    public SelectorConfigEntryElement<T> setOnValueChanged(Consumer<T> onValueChanged) {
        getValueElement().setOnValueChanged(onValueChanged);
        return this;
    }

    @Override
    public T getValue() {
        return getValueElement().getValue();
    }

    @Override
    public SelectorConfigEntryElement<T> setText(String text) {
        return (SelectorConfigEntryElement<T>) super.setText(text);
    }

    @Override
    public SelectorConfigEntryElement<T> setText(Component component) {
        return (SelectorConfigEntryElement<T>) super.setText(component);
    }

    @Override
    public SelectorConfigEntryElement<T> setValue(T value) {
        return setValue(value, true);
    }

    public SelectorConfigEntryElement<T> setValue(T value, boolean notify) {
        getValueElement().setValue(value, notify);
        return this;
    }
}

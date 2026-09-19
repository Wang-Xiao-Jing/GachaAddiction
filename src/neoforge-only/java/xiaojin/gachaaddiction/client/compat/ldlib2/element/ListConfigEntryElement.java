package xiaojin.gachaaddiction.client.compat.ldlib2.element;

import com.lowdragmc.lowdraglib2.editor.resource.IResourcePath;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextElement;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import xiaojin.gachaaddiction.client.compat.ldlib2.LDLib2Templates;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

@SuppressWarnings("UnusedReturnValue")
public class ListConfigEntryElement<V> extends BasicConfigEntryElement<List<V>> {
    private final Button valueElement;
    private final TextElement itemNumberElement;
    private final List<V> value = new ArrayList<>();
    private Predicate<List<V>> valueValidator = values -> true;
    private Consumer<List<V>> onValueChanged = values -> {
    };
    private boolean allowEmpty;
    @Nullable
    private Supplier<V> newValueSupplier;
    @Nullable
    private Function<V, BasicConfigEntryElement<V>> entryFactory;

    public ListConfigEntryElement() {
        this(LDLib2Templates.LIST_CONFIG_ENTRY);
    }

    public ListConfigEntryElement(@Nullable IResourcePath templatePath) {
        super(templatePath);
        valueElement = select("#value", Button.class).findFirst().orElseThrow();
        itemNumberElement = select("#item_number", TextElement.class).findFirst().orElseThrow();
        valueElement.setOnClick(event -> openEditor(event.x, event.y));
    }

    public Button getValueElement() {
        return valueElement;
    }

    public TextElement getItemNumberElement() {
        return itemNumberElement;
    }

    public ListConfigEntryElement<V> setValueValidator(Predicate<List<V>> valueValidator) {
        this.valueValidator = valueValidator;
        return this;
    }

    public ListConfigEntryElement<V> setOnValueChanged(Consumer<List<V>> onValueChanged) {
        this.onValueChanged = onValueChanged;
        return this;
    }

    public ListConfigEntryElement<V> setAllowEmpty(boolean allowEmpty) {
        this.allowEmpty = allowEmpty;
        return this;
    }

    public ListConfigEntryElement<V> setNewValueSupplier(Supplier<V> newValueSupplier) {
        this.newValueSupplier = newValueSupplier;
        return this;
    }

    public ListConfigEntryElement<V> setEntryFactory(Function<V, BasicConfigEntryElement<V>> entryFactory) {
        this.entryFactory = entryFactory;
        return this;
    }

    @Override
    public List<V> getValue() {
        return List.copyOf(value);
    }

    @Override
    public ListConfigEntryElement<V> setText(String text) {
        return (ListConfigEntryElement<V>) super.setText(text);
    }

    @Override
    public ListConfigEntryElement<V> setText(Component component) {
        return (ListConfigEntryElement<V>) super.setText(component);
    }

    @Override
    public ListConfigEntryElement<V> setValue(List<V> value) {
        return setValue(value, true);
    }

    public ListConfigEntryElement<V> setValue(List<V> value, boolean notify) {
        this.value.clear();
        this.value.addAll(value);
        getItemNumberElement().setText(Integer.toString(value.size()));
        if (notify) {
            onValueChanged.accept(getValue());
        }
        return this;
    }

    private void openEditor(float mouseX, float mouseY) {
        if (newValueSupplier == null || entryFactory == null) {
            return;
        }
        new ListConfigScreen<>(getText(), getValue(), newValueSupplier, entryFactory,
                valueValidator, allowEmpty, this::setValue, mouseX, mouseY)
                .show(getModularUI());
    }
}

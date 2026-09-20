package xiaojin.gachaaddiction.client.compat.ldlib2.element;

import com.lowdragmc.lowdraglib2.editor.resource.IResourcePath;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Switch;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import xiaojin.gachaaddiction.client.compat.ldlib2.LDLib2Templates;

@SuppressWarnings("UnusedReturnValue")
public class SwitchConfigEntryElement extends BasicConfigEntryElement<Boolean> {
    private final Switch valueElement;

    public SwitchConfigEntryElement() {
        this(LDLib2Templates.SWITCH_CONFIG_ENTRY);
    }

    public SwitchConfigEntryElement(@Nullable IResourcePath templatePath) {
        super(templatePath);
        valueElement = select("#value", Switch.class).findFirst().orElseThrow();
    }

    public Switch getValueElement() {
        return valueElement;
    }

    public SwitchConfigEntryElement setOnValueChanged(BooleanConsumer onValueChanged) {
        getValueElement().setOnSwitchChanged(onValueChanged);
        return this;
    }

    @Override
    public Boolean getValue() {
        return getValueElement().getValue();
    }

    @Override
    public SwitchConfigEntryElement setText(String text) {
        return (SwitchConfigEntryElement) super.setText(text);
    }

    @Override
    public SwitchConfigEntryElement setText(Component component) {
        return (SwitchConfigEntryElement) super.setText(component);
    }

    @Override
    public SwitchConfigEntryElement setValue(Boolean value) {
        return setValue(value, true);
    }

    public SwitchConfigEntryElement setValue(Boolean value, boolean notify) {
        getValueElement().setValue(value, notify);
        return this;
    }
}

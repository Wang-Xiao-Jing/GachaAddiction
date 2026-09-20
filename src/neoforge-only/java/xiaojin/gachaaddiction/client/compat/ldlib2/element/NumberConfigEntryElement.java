package xiaojin.gachaaddiction.client.compat.ldlib2.element;

import com.lowdragmc.lowdraglib2.editor.resource.IResourcePath;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextField;
import org.jetbrains.annotations.Nullable;
import xiaojin.gachaaddiction.client.compat.ldlib2.LDLib2Templates;

import java.util.function.Consumer;
import java.util.function.Function;

public abstract class NumberConfigEntryElement<N extends Number> extends FieldConfigEntryElement<N> {
    private final Function<String, N> parser;

    protected NumberConfigEntryElement(Consumer<TextField> fieldConfigurator, Function<String, N> parser) {
        this(LDLib2Templates.FIELD_CONFIG_ENTRY, fieldConfigurator, parser);
    }

    protected NumberConfigEntryElement(@Nullable IResourcePath templatePath,
                                       Consumer<TextField> fieldConfigurator, Function<String, N> parser) {
        super(templatePath);
        this.parser = parser;
        fieldConfigurator.accept(getValueElement());
    }

    @Override
    public N getValue() {
        return parser.apply(getValueElement().getValue());
    }

    @Override
    public NumberConfigEntryElement<N> setValue(N value) {
        getValueElement().setValue(value.toString());
        return this;
    }
}

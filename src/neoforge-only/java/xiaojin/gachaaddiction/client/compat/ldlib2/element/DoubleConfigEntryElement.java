package xiaojin.gachaaddiction.client.compat.ldlib2.element;

import com.lowdragmc.lowdraglib2.editor.resource.IResourcePath;
import org.jetbrains.annotations.Nullable;

public class DoubleConfigEntryElement extends NumberConfigEntryElement<Double> {
    @SuppressWarnings("unused")
    public DoubleConfigEntryElement() {
        this(-Double.MAX_VALUE, Double.MAX_VALUE);
    }

    public DoubleConfigEntryElement(double minValue, double maxValue) {
        super(field -> field.setNumbersOnlyDouble(minValue, maxValue), Double::parseDouble);
    }

    @SuppressWarnings("unused")
    public DoubleConfigEntryElement(@Nullable IResourcePath templatePath) {
        this(templatePath, -Double.MAX_VALUE, Double.MAX_VALUE);
    }

    public DoubleConfigEntryElement(@Nullable IResourcePath templatePath, double minValue, double maxValue) {
        super(templatePath, field -> field.setNumbersOnlyDouble(minValue, maxValue), Double::parseDouble);
    }
}

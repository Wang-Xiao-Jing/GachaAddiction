package xiaojin.gachaaddiction.client.compat.ldlib2.element;

import com.lowdragmc.lowdraglib2.editor.resource.IResourcePath;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class FloatConfigEntryElement extends NumberConfigEntryElement<Float> {
    public FloatConfigEntryElement() {
        this(-Float.MAX_VALUE, Float.MAX_VALUE);
    }

    public FloatConfigEntryElement(float minValue, float maxValue) {
        super(field -> field.setNumbersOnlyFloat(minValue, maxValue), Float::parseFloat);
    }

    public FloatConfigEntryElement(@Nullable IResourcePath templatePath) {
        this(templatePath, -Float.MAX_VALUE, Float.MAX_VALUE);
    }

    public FloatConfigEntryElement(@Nullable IResourcePath templatePath, float minValue, float maxValue) {
        super(templatePath, field -> field.setNumbersOnlyFloat(minValue, maxValue), Float::parseFloat);
    }
}

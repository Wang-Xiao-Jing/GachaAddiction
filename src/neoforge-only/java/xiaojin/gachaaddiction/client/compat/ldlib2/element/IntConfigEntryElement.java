package xiaojin.gachaaddiction.client.compat.ldlib2.element;

import com.lowdragmc.lowdraglib2.editor.resource.IResourcePath;
import org.jetbrains.annotations.Nullable;

public class IntConfigEntryElement extends NumberConfigEntryElement<Integer> {
    @SuppressWarnings("unused")
    public IntConfigEntryElement() {
        this(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public IntConfigEntryElement(int minValue, int maxValue) {
        super(field -> field.setNumbersOnlyInt(minValue, maxValue), Integer::parseInt);
    }

    @SuppressWarnings("unused")
    public IntConfigEntryElement(@Nullable IResourcePath templatePath) {
        this(templatePath, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public IntConfigEntryElement(@Nullable IResourcePath templatePath, int minValue, int maxValue) {
        super(templatePath, field -> field.setNumbersOnlyInt(minValue, maxValue), Integer::parseInt);
    }
}

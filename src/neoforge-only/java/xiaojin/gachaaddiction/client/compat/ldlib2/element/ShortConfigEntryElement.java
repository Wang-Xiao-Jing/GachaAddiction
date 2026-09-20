package xiaojin.gachaaddiction.client.compat.ldlib2.element;

import com.lowdragmc.lowdraglib2.editor.resource.IResourcePath;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class ShortConfigEntryElement extends NumberConfigEntryElement<Short> {
    public ShortConfigEntryElement() {
        this(Short.MIN_VALUE, Short.MAX_VALUE);
    }

    public ShortConfigEntryElement(short minValue, short maxValue) {
        super(field -> field.setNumbersOnlyShort(minValue, maxValue), Short::parseShort);
    }

    public ShortConfigEntryElement(@Nullable IResourcePath templatePath) {
        this(templatePath, Short.MIN_VALUE, Short.MAX_VALUE);
    }

    public ShortConfigEntryElement(@Nullable IResourcePath templatePath, short minValue, short maxValue) {
        super(templatePath, field -> field.setNumbersOnlyShort(minValue, maxValue), Short::parseShort);
    }
}

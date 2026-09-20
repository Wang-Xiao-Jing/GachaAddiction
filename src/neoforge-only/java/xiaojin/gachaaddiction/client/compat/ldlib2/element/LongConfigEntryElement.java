package xiaojin.gachaaddiction.client.compat.ldlib2.element;

import com.lowdragmc.lowdraglib2.editor.resource.IResourcePath;
import org.jetbrains.annotations.Nullable;

public class LongConfigEntryElement extends NumberConfigEntryElement<Long> {
    @SuppressWarnings("unused")
    public LongConfigEntryElement() {
        this(Long.MIN_VALUE, Long.MAX_VALUE);
    }

    public LongConfigEntryElement(long minValue, long maxValue) {
        super(field -> field.setNumbersOnlyLong(minValue, maxValue), Long::parseLong);
    }

    @SuppressWarnings("unused")
    public LongConfigEntryElement(@Nullable IResourcePath templatePath) {
        this(templatePath, Long.MIN_VALUE, Long.MAX_VALUE);
    }

    public LongConfigEntryElement(@Nullable IResourcePath templatePath, long minValue, long maxValue) {
        super(templatePath, field -> field.setNumbersOnlyLong(minValue, maxValue), Long::parseLong);
    }
}

package xiaojin.gachaaddiction.client.compat.ldlib2.element;

import com.lowdragmc.lowdraglib2.editor.resource.IResourcePath;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class ByteConfigEntryElement extends NumberConfigEntryElement<Byte> {
    public ByteConfigEntryElement() {
        this(Byte.MIN_VALUE, Byte.MAX_VALUE);
    }

    public ByteConfigEntryElement(byte minValue, byte maxValue) {
        super(field -> field.setNumbersOnlyByte(minValue, maxValue), Byte::parseByte);
    }

    public ByteConfigEntryElement(@Nullable IResourcePath templatePath) {
        this(templatePath, Byte.MIN_VALUE, Byte.MAX_VALUE);
    }

    public ByteConfigEntryElement(@Nullable IResourcePath templatePath, byte minValue, byte maxValue) {
        super(templatePath, field -> field.setNumbersOnlyByte(minValue, maxValue), Byte::parseByte);
    }
}

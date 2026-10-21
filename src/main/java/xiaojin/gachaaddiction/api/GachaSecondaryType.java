package xiaojin.gachaaddiction.api;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum GachaSecondaryType implements StringRepresentable {
    NONE("none"), SELECT_ONE("select_one"), MULTIPLE("multiple");

    public static final int DEFAULT_CHOICES = 3;
    public static final int MAXIMUM_CHOICES = 12;
    public static final int MAXIMUM_ROUNDS = 4096;
    public static final Codec<GachaSecondaryType> CODEC = StringRepresentable.fromEnum(GachaSecondaryType::values);
    public static final StreamCodec<RegistryFriendlyByteBuf, GachaSecondaryType> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public GachaSecondaryType decode(RegistryFriendlyByteBuf buffer) {
            return buffer.readEnum(GachaSecondaryType.class);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, GachaSecondaryType value) {
            buffer.writeEnum(value);
        }
    };
    private final String name;

    GachaSecondaryType(String name) {
        this.name = name;
    }

    public static GachaSecondaryType byName(String name) {
        for (GachaSecondaryType type : values()) {
            if (type.name.equals(name)) {
                return type;
            }
        }
        return NONE;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public String getTranslationKey() {
        return "gacha_secondary_type.gachaaddiction." + name;
    }
}

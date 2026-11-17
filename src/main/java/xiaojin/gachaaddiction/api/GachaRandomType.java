package xiaojin.gachaaddiction.api;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum GachaRandomType implements StringRepresentable {
    NONE("none"), RANDOM("random"), FULL_RANDOM("full_random");

    public static final Codec<GachaRandomType> CODEC = StringRepresentable.fromEnum(GachaRandomType::values);
    public static final StreamCodec<RegistryFriendlyByteBuf, GachaRandomType> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull GachaRandomType decode(RegistryFriendlyByteBuf buffer) {
            return buffer.readEnum(GachaRandomType.class);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, @NotNull GachaRandomType value) {
            buffer.writeEnum(value);
        }
    };
    private final String name;

    GachaRandomType(String name) {
        this.name = name;
    }

    public static GachaRandomType byName(String name) {
        for (GachaRandomType type : values()) {
            if (type.name.equals(name)) {
                return type;
            }
        }

        return NONE;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }
}

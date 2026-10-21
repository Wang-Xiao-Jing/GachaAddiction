package xiaojin.gachaaddiction.reward.parameter;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.function.UnaryOperator;

public record GachaParameterType<T>(ResourceLocation id, Class<T> valueClass, Codec<T> codec,
                                    UnaryOperator<T> copyFunction) {
    public GachaParameterType {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(valueClass, "valueClass");
        Objects.requireNonNull(codec, "codec");
        Objects.requireNonNull(copyFunction, "copyFunction");
    }

    public T copy(T value) {
        Objects.requireNonNull(value, "value");
        if (!valueClass.isInstance(value)) {
            throw new IllegalArgumentException("Value does not match parameter type: " + id);
        }
        return Objects.requireNonNull(copyFunction.apply(value), "copied value");
    }
}

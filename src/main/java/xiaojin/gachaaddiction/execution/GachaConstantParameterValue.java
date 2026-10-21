package xiaojin.gachaaddiction.execution;

import xiaojin.gachaaddiction.reward.number.GachaNumberContext;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterType;

import java.util.Objects;

public record GachaConstantParameterValue<T>(GachaParameterType<T> type, T value) implements GachaParameterValue {
    public GachaConstantParameterValue {
        Objects.requireNonNull(type, "type");
        value = type.copy(Objects.requireNonNull(value, "value"));
    }

    @Override
    public Object resolve(GachaNumberContext context) {
        return type.copy(value);
    }
}

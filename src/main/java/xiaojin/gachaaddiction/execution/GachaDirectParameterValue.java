package xiaojin.gachaaddiction.execution;

import xiaojin.gachaaddiction.reward.number.GachaNumberContext;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterKey;

import java.util.Objects;

public record GachaDirectParameterValue(GachaParameterKey source) implements GachaParameterValue {
    public GachaDirectParameterValue {
        Objects.requireNonNull(source, "source");
    }

    @Override
    public Object resolve(GachaNumberContext context) {
        return context.currentParameters().get(source).orElseThrow(() ->
                new IllegalArgumentException("Missing mapped source parameter: " + source));
    }
}

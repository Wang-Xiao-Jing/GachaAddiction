package xiaojin.gachaaddiction.execution;

import xiaojin.gachaaddiction.reward.number.GachaDoubleProvider;
import xiaojin.gachaaddiction.reward.number.GachaNumberContext;

import java.util.Objects;

public record GachaDoubleParameterValue(GachaDoubleProvider provider) implements GachaParameterValue {
    public GachaDoubleParameterValue {
        Objects.requireNonNull(provider, "provider");
    }

    @Override
    public Object resolve(GachaNumberContext context) {
        return provider.get(context);
    }
}

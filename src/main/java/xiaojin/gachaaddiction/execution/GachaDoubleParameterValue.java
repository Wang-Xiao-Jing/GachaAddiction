package xiaojin.gachaaddiction.execution;

import xiaojin.gachaaddiction.reward.number.IGachaDoubleProvider;
import xiaojin.gachaaddiction.reward.number.GachaNumberContext;

import java.util.Objects;

public record GachaDoubleParameterValue(IGachaDoubleProvider provider) implements IGachaParameterValue {
    public GachaDoubleParameterValue {
        Objects.requireNonNull(provider, "provider");
    }

    @Override
    public Object resolve(GachaNumberContext context) {
        return provider.get(context);
    }
}

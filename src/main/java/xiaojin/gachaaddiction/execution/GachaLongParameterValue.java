package xiaojin.gachaaddiction.execution;

import xiaojin.gachaaddiction.reward.number.IGachaLongProvider;
import xiaojin.gachaaddiction.reward.number.GachaNumberContext;

import java.util.Objects;

public record GachaLongParameterValue(IGachaLongProvider provider) implements IGachaParameterValue {
    public GachaLongParameterValue {
        Objects.requireNonNull(provider, "provider");
    }

    @Override
    public Object resolve(GachaNumberContext context) {
        return provider.get(context);
    }
}

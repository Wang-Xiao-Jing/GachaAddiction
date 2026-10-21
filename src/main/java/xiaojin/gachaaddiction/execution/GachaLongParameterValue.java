package xiaojin.gachaaddiction.execution;

import xiaojin.gachaaddiction.reward.number.GachaLongProvider;
import xiaojin.gachaaddiction.reward.number.GachaNumberContext;

import java.util.Objects;

public record GachaLongParameterValue(GachaLongProvider provider) implements GachaParameterValue {
    public GachaLongParameterValue {
        Objects.requireNonNull(provider, "provider");
    }

    @Override
    public Object resolve(GachaNumberContext context) {
        return provider.get(context);
    }
}

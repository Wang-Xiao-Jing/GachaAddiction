package xiaojin.gachaaddiction.reward.source;

import xiaojin.gachaaddiction.reward.number.IGachaDoubleProvider;

import java.util.Objects;

public record GachaWeightedSource(IGachaRewardSource source, IGachaDoubleProvider weight) {
    public GachaWeightedSource {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(weight, "weight");
    }
}

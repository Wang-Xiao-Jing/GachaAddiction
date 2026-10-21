package xiaojin.gachaaddiction.reward.source;

import xiaojin.gachaaddiction.reward.number.GachaDoubleProvider;

import java.util.Objects;

public record GachaWeightedSource(GachaRewardSource source, GachaDoubleProvider weight) {
    public GachaWeightedSource {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(weight, "weight");
    }
}

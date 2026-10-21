package xiaojin.gachaaddiction.reward.custom;

import xiaojin.gachaaddiction.reward.number.IGachaDoubleProvider;

import java.util.Objects;

public record GachaWeightedCustomEntry(IGachaCustomEntry entry, IGachaDoubleProvider weight) {
    public GachaWeightedCustomEntry {
        Objects.requireNonNull(entry, "entry");
        Objects.requireNonNull(weight, "weight");
    }
}

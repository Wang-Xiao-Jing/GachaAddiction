package xiaojin.gachaaddiction.reward.custom;

import xiaojin.gachaaddiction.reward.number.GachaDoubleProvider;

import java.util.Objects;

public record GachaWeightedCustomEntry(GachaCustomEntry entry, GachaDoubleProvider weight) {
    public GachaWeightedCustomEntry {
        Objects.requireNonNull(entry, "entry");
        Objects.requireNonNull(weight, "weight");
    }
}

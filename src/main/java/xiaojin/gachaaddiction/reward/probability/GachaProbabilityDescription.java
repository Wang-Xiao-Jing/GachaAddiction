package xiaojin.gachaaddiction.reward.probability;

import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.reward.number.GachaNumberExpression;

import java.util.List;
import java.util.Objects;

public record GachaProbabilityDescription(ResourceLocation id, GachaProbabilitySelectionMode selectionMode,
                                          GachaNumberExpression selectionCount, List<Candidate> candidates) {
    public GachaProbabilityDescription {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(selectionMode, "selectionMode");
        Objects.requireNonNull(selectionCount, "selectionCount");
        candidates = List.copyOf(Objects.requireNonNull(candidates, "candidates"));
    }

    public record Candidate(ResourceLocation id, GachaNumberExpression weight) {
        public Candidate {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(weight, "weight");
        }
    }
}

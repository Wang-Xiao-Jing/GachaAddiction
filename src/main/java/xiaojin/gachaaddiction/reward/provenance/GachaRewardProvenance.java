package xiaojin.gachaaddiction.reward.provenance;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record GachaRewardProvenance(List<GachaRewardOrigin> generationProvenance,
                                    List<GachaRewardOrigin> transformationProvenance,
                                    GachaRewardOrigin currentRewardOrigin) {
    public GachaRewardProvenance {
        generationProvenance = List.copyOf(generationProvenance);
        transformationProvenance = List.copyOf(transformationProvenance);
        Objects.requireNonNull(currentRewardOrigin, "currentRewardOrigin");
        if (generationProvenance.isEmpty()) {
            throw new IllegalArgumentException("Generation provenance cannot be empty");
        }
    }

    public static GachaRewardProvenance generated(GachaRewardOrigin origin) {
        Objects.requireNonNull(origin, "origin");
        return new GachaRewardProvenance(List.of(origin), List.of(), origin);
    }

    public GachaRewardProvenance generatedBy(GachaRewardOrigin origin) {
        List<GachaRewardOrigin> generation = new ArrayList<>(generationProvenance);
        generation.add(Objects.requireNonNull(origin, "origin"));
        return new GachaRewardProvenance(generation, transformationProvenance, origin);
    }

    public GachaRewardProvenance transformedBy(GachaRewardOrigin origin) {
        List<GachaRewardOrigin> transformations = new ArrayList<>(transformationProvenance);
        transformations.add(Objects.requireNonNull(origin, "origin"));
        return new GachaRewardProvenance(generationProvenance, transformations, origin);
    }

    public boolean matches(GachaProvenanceSelector selector, GachaRewardOrigin origin) {
        Objects.requireNonNull(selector, "selector");
        Objects.requireNonNull(origin, "origin");
        return switch (selector) {
            case ROOT_SOURCE -> generationProvenance.getFirst().equals(origin);
            case ANY_SOURCE -> generationProvenance.contains(origin);
            case LEAF_SOURCE -> generationProvenance.getLast().equals(origin);
            case CURRENT_ORIGIN -> currentRewardOrigin.equals(origin);
        };
    }
}

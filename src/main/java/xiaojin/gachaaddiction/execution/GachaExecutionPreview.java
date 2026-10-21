package xiaojin.gachaaddiction.execution;

import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterSet;
import xiaojin.gachaaddiction.reward.probability.GachaProbabilityDescription;

import java.util.List;
import java.util.Objects;

public record GachaExecutionPreview(ResourceLocation planId, GachaParameterSet normalizedParameters,
                                    List<GachaExecutionStepPreview> steps,
                                    List<GachaExecutionPreviewIssue> issues,
                                    List<GachaProbabilityDescription> probabilityDescriptions) {
    public GachaExecutionPreview(ResourceLocation planId, GachaParameterSet normalizedParameters,
                                 List<GachaExecutionStepPreview> steps,
                                 List<GachaExecutionPreviewIssue> issues) {
        this(planId, normalizedParameters, steps, issues, List.of());
    }

    public GachaExecutionPreview {
        Objects.requireNonNull(planId, "planId");
        Objects.requireNonNull(normalizedParameters, "normalizedParameters");
        steps = List.copyOf(steps);
        issues = List.copyOf(issues);
        probabilityDescriptions = List.copyOf(probabilityDescriptions);
    }
}

package xiaojin.gachaaddiction.execution;

import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterSchema;
import xiaojin.gachaaddiction.reward.quantity.GachaExecutionLimits;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record GachaExecutionPlanDefinition(ResourceLocation id, GachaParameterSchema parameterSchema,
                                           List<GachaExecutionStep> steps,
                                           Optional<GachaExecutionLimits> limits) {
    public GachaExecutionPlanDefinition(ResourceLocation id, GachaParameterSchema parameterSchema,
                                        List<GachaExecutionStep> steps) {
        this(id, parameterSchema, steps, Optional.empty());
    }

    public GachaExecutionPlanDefinition {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(parameterSchema, "parameterSchema");
        steps = List.copyOf(steps);
        limits = Objects.requireNonNull(limits, "limits");
        if (steps.isEmpty()) {
            throw new IllegalArgumentException("Execution plan must contain at least one step: " + id);
        }
    }
}

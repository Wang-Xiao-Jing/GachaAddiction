package xiaojin.gachaaddiction.execution;

import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

@FunctionalInterface
public interface IGachaExecutionPlanResolver {
    Optional<GachaExecutionPlanDefinition> resolveExecutionPlan(ResourceLocation id);
}

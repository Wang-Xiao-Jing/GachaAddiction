package xiaojin.gachaaddiction.data;

import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.cardpool.GachaCardPoolDefinition;
import xiaojin.gachaaddiction.execution.GachaExecutionPlanDefinition;

import java.util.Map;

public record GachaCardPoolCompilation(Map<ResourceLocation, GachaCardPoolDefinition> cardPools,
                                       Map<ResourceLocation, GachaExecutionPlanDefinition> generatedPlans) {
    public GachaCardPoolCompilation {
        cardPools = Map.copyOf(cardPools);
        generatedPlans = Map.copyOf(generatedPlans);
    }
}

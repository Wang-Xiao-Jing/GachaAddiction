package xiaojin.gachaaddiction.execution;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

@FunctionalInterface
public interface GachaExecutionStepType {
    GachaExecutionStep decode(ResourceLocation planId, int stepIndex, JsonObject data,
                              GachaParameterMappingDecoder parameterMappings);
}

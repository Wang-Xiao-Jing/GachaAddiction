package xiaojin.gachaaddiction.execution;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

@FunctionalInterface
public interface IGachaExecutionStepType {
    IGachaExecutionStep decode(ResourceLocation planId, int stepIndex, JsonObject data,
                               IGachaParameterMappingDecoder parameterMappings);
}

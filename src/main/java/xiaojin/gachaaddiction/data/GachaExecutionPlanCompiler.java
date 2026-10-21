package xiaojin.gachaaddiction.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.execution.GachaExecutionPlanDefinition;
import xiaojin.gachaaddiction.execution.GachaExecutionStep;
import xiaojin.gachaaddiction.execution.GachaExecutionStepType;
import xiaojin.gachaaddiction.execution.GachaExecutionStepTypeRegistry;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterDefinition;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterSchema;
import xiaojin.gachaaddiction.reward.quantity.GachaExecutionLimits;

import java.util.*;

public final class GachaExecutionPlanCompiler {
    public static final int MAXIMUM_STEPS = 256;

    private static final Set<String> ROOT_FIELDS = Set.of("format_version", "parameters", "steps", "limits");
    private static final Set<String> STEP_FIELDS = Set.of("type", "data");

    private final GachaParameterDefinitionJson parameters;
    private final GachaExecutionStepTypeRegistry stepTypes;

    public GachaExecutionPlanCompiler(GachaParameterDefinitionJson parameters,
                                      GachaExecutionStepTypeRegistry stepTypes) {
        this.parameters = Objects.requireNonNull(parameters, "parameters");
        this.stepTypes = Objects.requireNonNull(stepTypes, "stepTypes");
    }

    public Map<ResourceLocation, GachaExecutionPlanDefinition> compile(
            Map<ResourceLocation, GachaRawDefinition> definitions) {
        Map<ResourceLocation, GachaExecutionPlanDefinition> compiled = new LinkedHashMap<>();
        definitions.forEach((id, definition) -> compiled.put(id, compile(id, definition.data())));
        return Map.copyOf(compiled);
    }

    private GachaExecutionPlanDefinition compile(ResourceLocation id, JsonObject json) {
        String location = "execution plan " + id;
        GachaJson.requireOnly(json, ROOT_FIELDS, location);
        GachaParameterSchema schema = parameterSchema(id, GachaJson.array(json, "parameters", location), location);
        List<GachaExecutionStep> steps = steps(id, GachaJson.array(json, "steps", location), location);
        Optional<GachaExecutionLimits> limits = json.has("limits")
                ? Optional.of(GachaExecutionLimitsJson.decode(GachaJson.object(json, "limits", location),
                location + ".limits")) : Optional.empty();
        return new GachaExecutionPlanDefinition(id, schema, steps, limits);
    }

    private GachaParameterSchema parameterSchema(ResourceLocation id, JsonArray encoded, String location) {
        List<GachaParameterDefinition<?>> definitions = new ArrayList<>();
        for (int index = 0; index < encoded.size(); index++) {
            JsonElement value = encoded.get(index);
            if (!value.isJsonObject()) {
                throw new JsonParseException("Execution plan parameter must be an object at " + location
                        + ".parameters[" + index + "]");
            }
            definitions.add(parameters.decode(id, value.getAsJsonObject(),
                    location + ".parameters[" + index + "]"));
        }
        return new GachaParameterSchema(definitions);
    }

    private List<GachaExecutionStep> steps(ResourceLocation id, JsonArray encoded, String location) {
        if (encoded.isEmpty()) {
            throw new JsonParseException("Execution plan must contain at least one step: " + id);
        }
        if (encoded.size() > MAXIMUM_STEPS) {
            throw new JsonParseException("Execution plan contains too many steps: " + id);
        }
        List<GachaExecutionStep> steps = new ArrayList<>();
        for (int index = 0; index < encoded.size(); index++) {
            JsonElement value = encoded.get(index);
            String stepLocation = location + ".steps[" + index + "]";
            if (!value.isJsonObject()) {
                throw new JsonParseException("Execution plan step must be an object at " + stepLocation);
            }
            JsonObject step = value.getAsJsonObject();
            GachaJson.requireOnly(step, STEP_FIELDS, stepLocation);
            ResourceLocation typeId = ResourceLocation.parse(GachaJson.string(step, "type", stepLocation));
            GachaExecutionStepType type = stepTypes.resolve(typeId).orElseThrow(() ->
                    new JsonParseException("Unknown execution step type '" + typeId + "' at " + stepLocation));
            steps.add(type.decode(id, index, GachaJson.object(step, "data", stepLocation),
                    this::parameterMappings));
        }
        return List.copyOf(steps);
    }

    private xiaojin.gachaaddiction.execution.GachaParameterMapping parameterMappings(
            ResourceLocation sourceOwner, ResourceLocation targetOwner, JsonArray encoded, String location) {
        return GachaParameterMappingJson.decode(parameters, sourceOwner, targetOwner, encoded, location);
    }
}

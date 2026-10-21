package xiaojin.gachaaddiction.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.reward.GachaRewardPool;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterDefinition;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterKey;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterSchema;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterSet;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class GachaRequestPresetCompiler {
    private static final Set<String> FIELDS = Set.of("format_version", "reward_pool", "rounds", "parameters");

    private final RegistryOps<JsonElement> ops;

    public GachaRequestPresetCompiler(HolderLookup.Provider registries) {
        this.ops = RegistryOps.create(JsonOps.INSTANCE, registries);
    }

    public Map<ResourceLocation, GachaRewardPoolRequestPreset> compile(
            Map<ResourceLocation, GachaRawDefinition> definitions,
            Map<ResourceLocation, GachaRewardPool> rewardPools) {
        Map<ResourceLocation, GachaRewardPoolRequestPreset> compiled = new LinkedHashMap<>();
        definitions.forEach((id, definition) -> compiled.put(id, compilePreset(id, definition.data(), rewardPools)));
        return Map.copyOf(compiled);
    }

    public GachaParameterSet decodeExternalParameters(ResourceLocation rewardPoolId,
                                                      GachaParameterSchema schema,
                                                      JsonObject json) {
        String location = "external parameters for reward pool " + rewardPoolId;
        GachaParameterSet parameters = decodeParameters(rewardPoolId, schema, json, location);
        try {
            return schema.normalizeInputs(parameters);
        } catch (RuntimeException failure) {
            throw new JsonParseException("Invalid parameters in " + location, failure);
        }
    }

    private GachaRewardPoolRequestPreset compilePreset(ResourceLocation id, JsonObject json,
                                                       Map<ResourceLocation, GachaRewardPool> rewardPools) {
        String location = "request preset " + id;
        GachaJson.requireOnly(json, FIELDS, location);
        ResourceLocation rewardPoolId = ResourceLocation.parse(GachaJson.string(json, "reward_pool", location));
        GachaRewardPool rewardPool = rewardPools.get(rewardPoolId);
        if (rewardPool == null) {
            throw new JsonParseException("Unknown reward pool '" + rewardPoolId + "' in " + location);
        }
        GachaParameterSet parameters = decodeParameters(rewardPoolId, rewardPool.parameterSchema(),
                GachaJson.object(json, "parameters", location), location + ".parameters");
        try {
            parameters = rewardPool.parameterSchema().normalizeInputs(parameters);
        } catch (RuntimeException failure) {
            throw new JsonParseException("Invalid parameters in " + location, failure);
        }
        return new GachaRewardPoolRequestPreset(id, rewardPoolId,
                GachaJson.integer(json, "rounds", location), parameters);
    }

    private GachaParameterSet decodeParameters(ResourceLocation rewardPoolId, GachaParameterSchema schema,
                                               JsonObject json, String location) {
        GachaParameterSet.Builder values = GachaParameterSet.builder();
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            GachaParameterDefinition<?> definition;
            try {
                definition = schema.require(new GachaParameterKey(rewardPoolId, entry.getKey()));
            } catch (RuntimeException failure) {
                throw new JsonParseException("Unknown parameter in " + location + ": " + entry.getKey(), failure);
            }
            values.put(definition, decodeValue(definition, entry.getValue(), location + "." + entry.getKey()));
        }
        return values.build();
    }

    private <T> T decodeValue(GachaParameterDefinition<T> definition, JsonElement json, String location) {
        try {
            return definition.type().codec().parse(ops, json).getOrThrow(JsonParseException::new);
        } catch (RuntimeException failure) {
            throw new JsonParseException("Invalid parameter value in " + location, failure);
        }
    }
}

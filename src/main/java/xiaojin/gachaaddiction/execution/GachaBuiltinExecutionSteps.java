package xiaojin.gachaaddiction.execution;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.GachaConstants;
import xiaojin.gachaaddiction.cardpool.transaction.GachaConsumptionSpec;
import xiaojin.gachaaddiction.data.GachaJson;
import xiaojin.gachaaddiction.data.GachaNumberProviderJson;
import xiaojin.gachaaddiction.reward.quantity.GachaExecutionLimits;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class GachaBuiltinExecutionSteps {
    public static final ResourceLocation REWARD_POOL = id("reward_pool");
    public static final ResourceLocation CUSTOM_TABLE = id("custom_table");
    public static final ResourceLocation VANILLA_LOOT_TABLE = id("vanilla_loot_table");
    public static final ResourceLocation RULE_GROUP = id("rule_group");
    public static final ResourceLocation QUANTITY_RULE = id("quantity_rule");
    public static final ResourceLocation EXECUTION_PLAN = id("execution_plan");
    public static final ResourceLocation CONSUMPTION = id("consumption");

    private static final Set<GachaExecutionChannel> COMPUTE_READS = Set.of(
            GachaExecutionChannel.PARAMETERS, GachaExecutionChannel.PROVENANCE);
    private static final Set<GachaExecutionChannel> COMPUTE_WRITES = Set.of(
            GachaExecutionChannel.REWARDS, GachaExecutionChannel.PARAMETERS,
            GachaExecutionChannel.DIAGNOSTICS, GachaExecutionChannel.PROVENANCE);
    private static final Set<GachaExecutionChannel> TRANSFORM_READS = Set.of(
            GachaExecutionChannel.REWARDS, GachaExecutionChannel.PARAMETERS,
            GachaExecutionChannel.PROVENANCE);
    private static final Set<GachaExecutionChannel> TRANSFORM_WRITES = Set.of(
            GachaExecutionChannel.REWARDS, GachaExecutionChannel.PARAMETERS,
            GachaExecutionChannel.DIAGNOSTICS, GachaExecutionChannel.PROVENANCE);
    private static final Set<GachaExecutionChannel> ALL_CHANNELS = Set.of(GachaExecutionChannel.values());
    private static final Set<String> LIMIT_FIELDS = Set.of("max_logical_quantity", "max_logical_rewards",
            "max_physical_stacks", "max_rounds", "max_source_selections", "max_rule_actions",
            "max_custom_table_depth", "max_custom_entry_visits");

    private GachaBuiltinExecutionSteps() {
    }

    public static void register(GachaExecutionStepTypeRegistry registry) {
        registry.register(REWARD_POOL, (planId, stepIndex, data, parameterMappings) -> {
            ResourceLocation target = target(data, planId, stepIndex, Set.of("target", "rounds", "parameters"));
            String location = planId + ".steps[" + stepIndex + "].data";
            if (!data.has("rounds")) {
                throw new JsonParseException("Reward pool execution step requires rounds at " + location);
            }
            return new GachaRewardPoolExecutionStep(REWARD_POOL, target,
                    GachaNumberProviderJson.longProvider(data.get("rounds"), location + ".rounds"),
                    parameters(data, parameterMappings, planId, stepIndex, target),
                    COMPUTE_READS, COMPUTE_WRITES);
        });
        registry.register(CUSTOM_TABLE, GachaBuiltinExecutionSteps::customTable);
        registry.register(VANILLA_LOOT_TABLE, GachaBuiltinExecutionSteps::vanillaLootTable);
        registry.register(RULE_GROUP, reference(RULE_GROUP, TRANSFORM_READS, TRANSFORM_WRITES));
        registry.register(QUANTITY_RULE, reference(QUANTITY_RULE, TRANSFORM_READS, TRANSFORM_WRITES));
        registry.register(EXECUTION_PLAN, reference(EXECUTION_PLAN, ALL_CHANNELS, ALL_CHANNELS));
        registry.register(CONSUMPTION, (planId, stepIndex, data, parameterMappings) ->
                new GachaConsumptionExecutionStep(CONSUMPTION,
                        consumption(data, planId, stepIndex),
                        Set.of(GachaExecutionChannel.PARAMETERS),
                        Set.of(GachaExecutionChannel.CONSUMPTION, GachaExecutionChannel.DIAGNOSTICS)));
    }

    private static IGachaExecutionStep customTable(ResourceLocation planId, int stepIndex, JsonObject data,
                                                   IGachaParameterMappingDecoder parameterMappings) {
        String location = planId + ".steps[" + stepIndex + "].data";
        ResourceLocation target = target(data, planId, stepIndex, Set.of("target", "rounds", "limits"));
        return new GachaCustomTableExecutionStep(CUSTOM_TABLE, target,
                GachaNumberProviderJson.longProvider(data.get("rounds"), location + ".rounds"),
                limits(data, location), COMPUTE_READS, COMPUTE_WRITES);
    }

    private static IGachaExecutionStep vanillaLootTable(ResourceLocation planId, int stepIndex, JsonObject data,
                                                        IGachaParameterMappingDecoder parameterMappings) {
        String location = planId + ".steps[" + stepIndex + "].data";
        ResourceLocation target = target(data, planId, stepIndex, Set.of("target", "rounds", "limits"));
        return new GachaVanillaLootTableExecutionStep(VANILLA_LOOT_TABLE,
                ResourceKey.create(Registries.LOOT_TABLE, target),
                GachaNumberProviderJson.longProvider(data.get("rounds"), location + ".rounds"),
                limits(data, location), COMPUTE_READS, COMPUTE_WRITES);
    }

    private static IGachaExecutionStepType reference(ResourceLocation type, Set<GachaExecutionChannel> reads,
                                                     Set<GachaExecutionChannel> writes) {
        return (planId, stepIndex, data, parameterMappings) -> {
            ResourceLocation target = target(data, planId, stepIndex, Set.of("target", "parameters"));
            GachaParameterMapping parameters = parameters(data, parameterMappings, planId, stepIndex, target);
            return new GachaComponentReferenceStep(type, target, parameters, reads, writes);
        };
    }

    private static ResourceLocation target(JsonObject data, ResourceLocation planId, int stepIndex,
                                           Set<String> fields) {
        if (!fields.containsAll(data.keySet()) || !data.has("target") || !data.get("target").isJsonPrimitive()
                || data.has("parameters") && !data.get("parameters").isJsonArray()) {
            throw new JsonParseException("Execution plan step must contain a target and optional parameters at "
                    + planId + ".steps[" + stepIndex + "]");
        }
        return ResourceLocation.parse(data.get("target").getAsString());
    }

    private static GachaExecutionLimits limits(JsonObject data, String location) {
        JsonObject encoded = GachaJson.object(data, "limits", location);
        String limitsLocation = location + ".limits";
        GachaJson.requireOnly(encoded, LIMIT_FIELDS, limitsLocation);
        return new GachaExecutionLimits(longValue(encoded, limitsLocation),
                GachaJson.integer(encoded, "max_logical_rewards", limitsLocation),
                GachaJson.integer(encoded, "max_physical_stacks", limitsLocation),
                GachaJson.integer(encoded, "max_rounds", limitsLocation),
                GachaJson.integer(encoded, "max_source_selections", limitsLocation),
                GachaJson.integer(encoded, "max_rule_actions", limitsLocation),
                GachaJson.integer(encoded, "max_custom_table_depth", limitsLocation),
                GachaJson.integer(encoded, "max_custom_entry_visits", limitsLocation));
    }

    private static long longValue(JsonObject json, String location) {
        String field = "max_logical_quantity";
        JsonElement value = json.get(field);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
            throw new JsonParseException("Field '" + field + "' must be an integer in " + location);
        }
        try {
            return GachaJson.longNumber(value, location + "." + field);
        } catch (RuntimeException failure) {
            throw new JsonParseException("Invalid integer in " + location + "." + field, failure);
        }
    }

    private static GachaParameterMapping parameters(JsonObject data,
                                                    IGachaParameterMappingDecoder parameterMappings,
                                                    ResourceLocation planId, int stepIndex,
                                                    ResourceLocation target) {
        String location = planId + ".steps[" + stepIndex + "].data.parameters";
        return data.has("parameters")
                ? parameterMappings.decode(planId, target, data.getAsJsonArray("parameters"), location)
                : GachaParameterMapping.EMPTY;
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(GachaConstants.MODID, path);
    }

    private static List<GachaConsumptionSpec> consumption(JsonObject data, ResourceLocation planId,
                                                          int stepIndex) {
        if (data.size() != 1 || !data.has("entries") || !data.get("entries").isJsonArray()) {
            throw new JsonParseException("Consumption step must contain only entries at "
                    + planId + ".steps[" + stepIndex + "]");
        }
        JsonArray entries = data.getAsJsonArray("entries");
        List<GachaConsumptionSpec> decoded = new ArrayList<>();
        for (int index = 0; index < entries.size(); index++) {
            JsonElement value = entries.get(index);
            String location = planId + ".steps[" + stepIndex + "].data.entries[" + index + "]";
            if (!value.isJsonObject()) {
                throw new JsonParseException("Consumption entry must be an object at " + location);
            }
            JsonObject entry = value.getAsJsonObject();
            if (entry.size() != 2 || !entry.has("type") || !entry.has("data")
                    || !entry.get("data").isJsonObject()) {
                throw new JsonParseException("Consumption entry must contain type and data at " + location);
            }
            Tag converted = JsonOps.INSTANCE.convertTo(NbtOps.INSTANCE, entry.get("data"));
            if (!(converted instanceof CompoundTag encoded)) {
                throw new JsonParseException("Consumption data must be a compound at " + location);
            }
            decoded.add(new GachaConsumptionSpec(
                    ResourceLocation.parse(entry.get("type").getAsString()), encoded));
        }
        return List.copyOf(decoded);
    }
}

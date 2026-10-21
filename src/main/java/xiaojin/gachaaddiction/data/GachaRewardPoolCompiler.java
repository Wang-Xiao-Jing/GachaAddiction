package xiaojin.gachaaddiction.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.reward.GachaHybridRewardPool;
import xiaojin.gachaaddiction.reward.IGachaRewardPool;
import xiaojin.gachaaddiction.reward.custom.IGachaCustomTableResolver;
import xiaojin.gachaaddiction.reward.number.IGachaLongProvider;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterDefinition;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterSchema;
import xiaojin.gachaaddiction.reward.quantity.GachaExecutionLimits;
import xiaojin.gachaaddiction.reward.rule.GachaRuleDefinition;
import xiaojin.gachaaddiction.reward.source.*;

import java.util.*;

public final class GachaRewardPoolCompiler {
    private static final Set<String> ROOT_FIELDS = Set.of("format_version", "parameters", "source_strategy",
            "source_count", "sources", "source_groups", "limits", "rules", "rule_groups");
    private static final Set<String> SOURCE_FIELDS = Set.of("id", "type", "table", "weight");
    private static final Set<String> LIMIT_FIELDS = Set.of("max_logical_quantity", "max_logical_rewards",
            "max_physical_stacks", "max_rounds", "max_source_selections", "max_rule_actions",
            "max_custom_table_depth", "max_custom_entry_visits");

    private final GachaParameterDefinitionJson parameters;
    private final IGachaCustomTableResolver customTables;
    private final IGachaRuleDefinitionCompiler rules;
    private final GachaSharedDefinitionExpander sharedDefinitions;

    public GachaRewardPoolCompiler(GachaParameterDefinitionJson parameters,
                                   IGachaCustomTableResolver customTables,
                                   IGachaRuleDefinitionCompiler rules,
                                   GachaSharedDefinitionExpander sharedDefinitions) {
        this.parameters = Objects.requireNonNull(parameters, "parameters");
        this.customTables = Objects.requireNonNull(customTables, "customTables");
        this.rules = Objects.requireNonNull(rules, "rules");
        this.sharedDefinitions = Objects.requireNonNull(sharedDefinitions, "sharedDefinitions");
    }

    private static GachaExecutionLimits compileLimits(JsonObject json, String location) {
        GachaJson.requireOnly(json, LIMIT_FIELDS, location);
        return new GachaExecutionLimits(longValue(json, location),
                GachaJson.integer(json, "max_logical_rewards", location),
                GachaJson.integer(json, "max_physical_stacks", location),
                GachaJson.integer(json, "max_rounds", location),
                GachaJson.integer(json, "max_source_selections", location),
                GachaJson.integer(json, "max_rule_actions", location),
                GachaJson.integer(json, "max_custom_table_depth", location),
                GachaJson.integer(json, "max_custom_entry_visits", location));
    }

    private static long longValue(JsonObject json, String location) {
        String field = "max_logical_quantity";
        JsonElement value = json.get(field);
        return GachaJson.longNumber(value, location + "." + field);
    }

    private static JsonArray optionalArray(JsonObject json, String field, String location) {
        return json.has(field) ? GachaJson.array(json, field, location) : new JsonArray();
    }

    public Map<ResourceLocation, IGachaRewardPool> compile(Map<ResourceLocation, GachaRawDefinition> definitions) {
        Map<ResourceLocation, IGachaRewardPool> compiled = new LinkedHashMap<>();
        definitions.forEach((id, definition) -> compiled.put(id, compilePool(id, definition.data())));
        return Map.copyOf(compiled);
    }

    private IGachaRewardPool compilePool(ResourceLocation id, JsonObject json) {
        String location = "reward pool " + id;
        GachaJson.requireOnly(json, ROOT_FIELDS, location);
        GachaParameterSchema schema = compileParameters(id, GachaJson.array(json, "parameters", location), location);
        JsonArray encodedSources = sharedDefinitions.sources(optionalArray(json, "sources", location),
                optionalArray(json, "source_groups", location), location + ".source_groups");
        List<GachaWeightedSource> sources = compileSources(encodedSources, location);
        IGachaLongProvider sourceCount = GachaNumberProviderJson.longProvider(json.get("source_count"),
                location + ".source_count");
        GachaExecutionLimits limits = compileLimits(GachaJson.object(json, "limits", location), location + ".limits");
        JsonArray encodedRules = sharedDefinitions.rules(optionalArray(json, "rules", location),
                optionalArray(json, "rule_groups", location), location + ".rule_groups");
        List<GachaRuleDefinition> compiledRules = rules.compile(id, encodedRules, schema);
        return new GachaHybridRewardPool(id,
                sources, GachaSourceSelectionStrategy.valueOf(GachaJson.string(json, "source_strategy", location)),
                sourceCount, limits, schema, compiledRules);
    }

    private GachaParameterSchema compileParameters(ResourceLocation owner, JsonArray encoded, String location) {
        List<GachaParameterDefinition<?>> definitions = new ArrayList<>();
        for (int index = 0; index < encoded.size(); index++) {
            JsonElement value = encoded.get(index);
            if (value == null || !value.isJsonObject()) {
                throw new JsonParseException("Parameter definition must be an object in " + location);
            }
            definitions.add(parameters.decode(owner, value.getAsJsonObject(),
                    location + ".parameters[" + index + "]"));
        }
        return new GachaParameterSchema(definitions);
    }

    private List<GachaWeightedSource> compileSources(JsonArray encoded, String location) {
        List<GachaWeightedSource> sources = new ArrayList<>();
        for (int index = 0; index < encoded.size(); index++) {
            String sourceLocation = location + ".sources[" + index + "]";
            JsonElement value = encoded.get(index);
            if (value == null || !value.isJsonObject()) {
                throw new JsonParseException("Reward source must be an object in " + sourceLocation);
            }
            JsonObject source = value.getAsJsonObject();
            GachaJson.requireOnly(source, SOURCE_FIELDS, sourceLocation);
            ResourceLocation sourceId = ResourceLocation.parse(GachaJson.string(source, "id", sourceLocation));
            ResourceLocation table = ResourceLocation.parse(GachaJson.string(source, "table", sourceLocation));
            IGachaRewardSource compiledSource = switch (GachaJson.string(source, "type", sourceLocation)) {
                case "vanilla_loot_table" -> new GachaVanillaLootTableSource(sourceId,
                        ResourceKey.create(Registries.LOOT_TABLE, table));
                case "custom_table" -> {
                    if (customTables.resolveTable(table).isEmpty()) {
                        throw new JsonParseException("Unknown custom loot table '" + table + "' in " + sourceLocation);
                    }
                    yield new GachaCustomLootTableSource(sourceId, table, customTables);
                }
                default -> throw new JsonParseException("Unknown reward source type in " + sourceLocation);
            };
            sources.add(new GachaWeightedSource(compiledSource,
                    GachaNumberProviderJson.doubleProvider(source.get("weight"), sourceLocation + ".weight")));
        }
        return List.copyOf(sources);
    }
}

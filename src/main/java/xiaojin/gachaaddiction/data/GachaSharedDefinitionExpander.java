package xiaojin.gachaaddiction.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

public final class GachaSharedDefinitionExpander {
    private final GachaRawDefinitionSnapshot definitions;
    private final Map<GachaDefinitionKind, Map<ResourceLocation, JsonArray>> cache =
            new EnumMap<>(GachaDefinitionKind.class);

    public GachaSharedDefinitionExpander(GachaRawDefinitionSnapshot definitions) {
        this.definitions = Objects.requireNonNull(definitions, "definitions");
    }

    private static void append(JsonArray target, JsonArray source) {
        source.forEach(element -> target.add(element.deepCopy()));
    }

    public JsonArray sources(JsonArray local, JsonArray groups, String location) {
        return combine(local, groups, GachaDefinitionKind.SOURCE_GROUP, "sources", location);
    }

    public JsonArray rules(JsonArray local, JsonArray groups, String location) {
        return combine(local, groups, GachaDefinitionKind.RULE_GROUP, "rules", location);
    }

    public JsonArray ruleGroup(ResourceLocation id, String location) {
        return expandGroup(GachaDefinitionKind.RULE_GROUP, Objects.requireNonNull(id, "id"), "rules",
                new ArrayDeque<>());
    }

    public JsonArray expandConditionEntries(JsonArray entries, String location) {
        return expandInline(entries, GachaDefinitionKind.CONDITION_GROUP, "conditions", "group", location);
    }

    public JsonArray expandQuantityEntries(JsonArray entries, String location) {
        return expandInline(entries, GachaDefinitionKind.QUANTITY_RULE, "operations", "rule", location);
    }

    public JsonArray quantityRule(ResourceLocation id, String location) {
        return expandGroup(GachaDefinitionKind.QUANTITY_RULE, Objects.requireNonNull(id, "id"), "operations",
                new ArrayDeque<>());
    }

    private JsonArray combine(JsonArray local, JsonArray groups, GachaDefinitionKind kind,
                              String field, String location) {
        JsonArray result = new JsonArray();
        for (int index = 0; index < groups.size(); index++) {
            JsonElement encodedId = groups.get(index);
            if (!encodedId.isJsonPrimitive() || !encodedId.getAsJsonPrimitive().isString()) {
                throw new JsonParseException("Shared definition reference must be a resource location in "
                        + location + "[" + index + "]");
            }
            append(result, expandGroup(kind, ResourceLocation.parse(encodedId.getAsString()), field,
                    new ArrayDeque<>()));
        }
        append(result, local);
        return result;
    }

    private JsonArray expandInline(JsonArray entries, GachaDefinitionKind kind, String field,
                                   String referenceField, String location) {
        JsonArray result = new JsonArray();
        for (int index = 0; index < entries.size(); index++) {
            JsonElement entry = entries.get(index);
            if (entry.isJsonObject() && entry.getAsJsonObject().has(referenceField)) {
                JsonObject reference = entry.getAsJsonObject();
                GachaJson.requireOnly(reference, Set.of(referenceField), location + "[" + index + "]");
                append(result, expandGroup(kind, ResourceLocation.parse(
                                GachaJson.string(reference, referenceField, location + "[" + index + "]")), field,
                        new ArrayDeque<>()));
            } else {
                result.add(entry.deepCopy());
            }
        }
        return result;
    }

    private JsonArray expandGroup(GachaDefinitionKind kind, ResourceLocation id, String field,
                                  Deque<ResourceLocation> path) {
        JsonArray cached = cache.computeIfAbsent(kind, ignored -> new LinkedHashMap<>()).get(id);
        if (cached != null) {
            return cached.deepCopy();
        }
        if (path.contains(id)) {
            throw new JsonParseException("Circular " + kind.directory() + " reference: " + path + " -> " + id);
        }
        GachaRawDefinition definition = definitions.find(kind, id).orElseThrow(() ->
                new JsonParseException("Unknown " + kind.directory() + " definition: " + id));
        JsonObject root = definition.data();
        GachaJson.requireOnly(root, Set.of("format_version", "includes", "entries", field),
                kind.directory() + " " + id);
        path.addLast(id);
        JsonArray expanded = new JsonArray();
        if (root.has("includes")) {
            JsonArray includes = GachaJson.array(root, "includes", kind.directory() + " " + id);
            for (int index = 0; index < includes.size(); index++) {
                JsonElement encodedId = includes.get(index);
                if (!encodedId.isJsonPrimitive() || !encodedId.getAsJsonPrimitive().isString()) {
                    throw new JsonParseException("Shared definition include must be a resource location in "
                            + kind.directory() + " " + id + ".includes[" + index + "]");
                }
                append(expanded, expandGroup(kind, ResourceLocation.parse(encodedId.getAsString()), field, path));
            }
        }
        if (root.has(field) && root.has("entries")) {
            throw new JsonParseException("Shared definition cannot contain both '" + field
                    + "' and 'entries': " + id);
        }
        if (root.has(field) || root.has("entries")) {
            String actualField = root.has(field) ? field : "entries";
            append(expanded, GachaJson.array(root, actualField, kind.directory() + " " + id));
        } else if (!root.has("includes")) {
            throw new JsonParseException("Shared definition must contain entries or includes: " + id);
        }
        path.removeLast();
        cache.get(kind).put(id, expanded.deepCopy());
        return expanded;
    }
}

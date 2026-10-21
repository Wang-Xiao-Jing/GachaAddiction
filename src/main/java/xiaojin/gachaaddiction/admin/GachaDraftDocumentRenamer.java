package xiaojin.gachaaddiction.admin;

import com.google.gson.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.cardpool.transaction.GachaPlayerInventoryConsumptionHandler;
import xiaojin.gachaaddiction.data.GachaDefinitionKind;
import xiaojin.gachaaddiction.execution.GachaBuiltinExecutionSteps;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterTypes;
import xiaojin.gachaaddiction.reward.provenance.GachaRewardOriginTypes;
import xiaojin.gachaaddiction.util.TranslationKeys;

import java.util.*;
import java.util.function.Consumer;

public class GachaDraftDocumentRenamer {
    private static final int MAXIMUM_JSON_DEPTH = 256;
    private static final String GENERATED_CARD_POOL_PREFIX = "generated/card_pool/";
    private static final Set<ResourceLocation> BUILTIN_PARAMETER_TYPES = Set.of(
            GachaParameterTypes.BOOLEAN.id(), GachaParameterTypes.INTEGER.id(), GachaParameterTypes.LONG.id(),
            GachaParameterTypes.DOUBLE.id(), GachaParameterTypes.STRING.id(),
            GachaParameterTypes.RESOURCE_LOCATION.id(), GachaParameterTypes.COMPOUND_TAG.id());

    private final GachaDefinitionDocumentKey oldKey;
    private final ResourceLocation newId;
    private final Map<GachaDefinitionDocumentKey, String> documents;
    private final Map<GachaDefinitionKind, Map<ResourceLocation, ResourceLocation>> renamedReferences =
            new EnumMap<>(GachaDefinitionKind.class);
    private final Set<ResourceLocation> generatedPlanIds = new HashSet<>();
    private final Map<JsonElement, String> replacements = new IdentityHashMap<>();
    private String location;

    private GachaDraftDocumentRenamer(GachaDefinitionDocumentKey oldKey, ResourceLocation newId,
                                      Map<GachaDefinitionDocumentKey, String> documents) {
        this.oldKey = oldKey;
        this.newId = newId;
        this.documents = documents;
        renamedReferences.put(oldKey.kind(), Map.of(oldKey.id(), newId));
        documents.forEach((key, content) -> {
            if (key.kind() != GachaDefinitionKind.CARD_POOL) return;
            withObject(JsonParser.parseString(content), cardPool -> {
                if (!cardPool.has("reward_pool")) return;
                eachObject(cardPool.get("packages"), drawPackage -> {
                    ResourceLocation packageId = id(drawPackage.get("id"));
                    if (packageId != null) generatedPlanIds.add(generatedPlanId(key.id(), packageId));
                });
            });
        });
        if (oldKey.kind() == GachaDefinitionKind.CARD_POOL) {
            JsonObject oldCardPool = JsonParser.parseString(documents.get(oldKey)).getAsJsonObject();
            if (oldCardPool.has("reward_pool")) {
                Map<ResourceLocation, ResourceLocation> plans = new LinkedHashMap<>();
                eachObject(oldCardPool.get("packages"), drawPackage -> {
                    ResourceLocation packageId = id(drawPackage.get("id"));
                    if (packageId != null) plans.put(generatedPlanId(oldKey.id(), packageId),
                            generatedPlanId(newId, packageId));
                });
                renamedReferences.put(GachaDefinitionKind.EXECUTION_PLAN, plans);
            }
        }
    }

    public static Map<GachaDefinitionDocumentKey, String> rename(
            Map<GachaDefinitionDocumentKey, String> documents,
            GachaDefinitionDocumentKey oldKey, ResourceLocation newId) {
        return new GachaDraftDocumentRenamer(oldKey, newId, documents).rename();
    }

    private static ResourceLocation id(JsonElement value) {
        return value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()
                ? ResourceLocation.tryParse(value.getAsString()) : null;
    }

    private static String type(JsonObject value) {
        return text(value.get("type"));
    }

    private static String text(JsonElement value) {
        return value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()
                ? value.getAsString() : "";
    }

    private static JsonElement entries(JsonObject value, String field) {
        return value.has(field) ? value.get(field) : value.get("entries");
    }

    private static void withObject(JsonElement value, Consumer<JsonObject> consumer) {
        if (value != null && value.isJsonObject()) consumer.accept(value.getAsJsonObject());
    }

    private static void eachObject(JsonElement value, Consumer<JsonObject> consumer) {
        if (value != null && value.isJsonArray()) value.getAsJsonArray().forEach(entry -> withObject(entry, consumer));
    }

    private static ResourceLocation generatedPlanId(ResourceLocation cardPoolId, ResourceLocation packageId) {
        return ResourceLocation.fromNamespaceAndPath(cardPoolId.getNamespace(),
                GENERATED_CARD_POOL_PREFIX + cardPoolId.getPath() + "/" + packageId.getNamespace()
                        + "/" + packageId.getPath());
    }

    private Map<GachaDefinitionDocumentKey, String> rename() {
        Map<GachaDefinitionDocumentKey, String> renamed = new LinkedHashMap<>();
        var newKey = new GachaDefinitionDocumentKey(oldKey.kind(), newId);
        documents.forEach((key, content) -> {
            location = key.kind().directory() + "/" + key.id();
            replacements.clear();
            JsonElement root = JsonParser.parseString(content);
            if (!root.isJsonObject()) {
                throw new JsonParseException(Component.translatable(
                        TranslationKeys.GUI_ADMIN_JSON_INVALID_VALUE).getString());
            }
            requireDepth(root, 0);
            document(key.kind(), root.getAsJsonObject());
            String updated = replacements.isEmpty() ? content : new SourceEditor(content, root).rewrite();
            renamed.put(key.equals(oldKey) ? newKey : key, updated);
        });
        return renamed;
    }

    private void document(GachaDefinitionKind kind, JsonObject root) {
        switch (kind) {
            case REWARD_POOL -> {
                eachObject(root.get("parameters"), this::parameterDefinition);
                number(root.get("source_count"));
                eachObject(root.get("sources"), this::source);
                references(root.get("source_groups"), GachaDefinitionKind.SOURCE_GROUP);
                eachObject(root.get("rules"), this::rule);
                references(root.get("rule_groups"), GachaDefinitionKind.RULE_GROUP);
            }
            case TABLE -> {
                number(root.get("selection_count"));
                eachObject(root.get("entries"), this::entry);
            }
            case SOURCE_GROUP -> {
                references(root.get("includes"), kind);
                eachObject(entries(root, "sources"), this::source);
            }
            case RULE_GROUP -> {
                references(root.get("includes"), kind);
                eachObject(entries(root, "rules"), this::rule);
            }
            case CONDITION_GROUP -> {
                references(root.get("includes"), kind);
                eachObject(entries(root, "conditions"), this::condition);
            }
            case QUANTITY_RULE -> {
                references(root.get("includes"), kind);
                eachObject(entries(root, "operations"), this::quantity);
            }
            case EXECUTION_PLAN -> {
                eachObject(root.get("parameters"), this::parameterDefinition);
                eachObject(root.get("steps"), this::step);
            }
            case CARD_POOL -> {
                reference(root.get("reward_pool"), GachaDefinitionKind.REWARD_POOL);
                reference(root.get("execution_plan"), GachaDefinitionKind.EXECUTION_PLAN);
                Map<String, ResourceLocation> types = parameterTypes(root);
                eachObject(root.get("parameter_bindings"), binding -> {
                    ResourceLocation type = types.get(text(binding.get("parameter")));
                    extensionParameterValue(type, binding.get("value"));
                    withObject(binding.get("player_choice"), choice -> extensionParameterValue(type, choice.get("values")));
                });
                eachObject(root.get("packages"), value -> {
                    eachObject(value.get("consumption"), this::consumption);
                    parameterValues(value.get("parameters"), types);
                });
            }
            case REQUEST_PRESET -> {
                reference(root.get("reward_pool"), GachaDefinitionKind.REWARD_POOL);
                parameterValues(root.get("parameters"), parameterTypes(root));
            }
        }
    }

    private void source(JsonObject value) {
        if (type(value).equals("custom_table")) {
            reference(value.get("table"), GachaDefinitionKind.TABLE);
        } else if (!type(value).equals("vanilla_loot_table")) {
            rejectUnknownReference(value);
        }
        number(value.get("weight"));
    }

    private void entry(JsonObject value) {
        String type = type(value);
        if (type.equals("custom_table")) {
            reference(value.get("table"), GachaDefinitionKind.TABLE);
        }
        if (type.equals("group") || type.equals("sequence") || type.equals("alternatives")) {
            eachObject(value.get("children"), this::entry);
        }
        if (type.equals("item") || type.equals("item_tag")) number(value.get("quantity"));
        number(value.get("weight"));
        eachObject(value.get("conditions"), this::condition);
    }

    private void rule(JsonObject value) {
        eachObject(value.get("conditions"), this::condition);
        eachObject(value.get("actions"), this::action);
        eachObject(value.get("fallback_actions"), this::action);
    }

    private void condition(JsonObject value) {
        if (value.has("condition")) {
            vanillaExtensions(value);
            return;
        }
        if (value.has("group")) {
            reference(value.get("group"), GachaDefinitionKind.CONDITION_GROUP);
            return;
        }
        switch (type(value)) {
            case "chance" -> number(value.get("chance"));
            case "number_compare" -> {
                number(value.get("left"));
                number(value.get("right"));
            }
            case "not", "any_reward" -> withObject(value.get("value"), this::condition);
            case "all" -> eachObject(value.get("values"), this::condition);
            case "provenance" -> {
                if (GachaRewardOriginTypes.CUSTOM_TABLE.equals(id(value.get("origin_type")))) {
                    reference(value.get("origin"), GachaDefinitionKind.TABLE);
                }
            }
            case "reward_item", "reward_item_tag" -> {
            }
            default -> rejectUnknownReference(value);
        }
    }

    private void action(JsonObject value) {
        switch (type(value)) {
            case "set_double_parameter", "set_long_parameter" -> number(value.get("value"));
            case "append_item" -> number(value.get("quantity"));
            case "quantity" -> eachObject(value.get("operations"), this::quantity);
            case "pity" -> {
                number(value.get("hard_pity_draw"));
                withObject(value.get("hit_condition"), this::condition);
                withObject(value.get("hard_action"), this::action);
            }
            case "vanilla_function" -> vanillaExtensions(value.get("value"));
            case "remove" -> {
            }
            default -> rejectUnknownReference(value);
        }
    }

    private void quantity(JsonObject value) {
        if (value.has("rule")) {
            reference(value.get("rule"), GachaDefinitionKind.QUANTITY_RULE);
            return;
        }
        switch (type(value)) {
            case "set", "add", "min", "max" -> number(value.get("value"));
            case "multiply" -> number(value.get("factor"));
            case "clamp", "roll" -> {
                number(value.get("minimum"));
                number(value.get("maximum"));
            }
            case "keep" -> {
            }
            default -> rejectUnknownReference(value);
        }
    }

    private void number(JsonElement value) {
        withObject(value, object -> {
            switch (type(object)) {
                case "parameter" -> parameterOwner(object.get("owner"));
                case "binary" -> {
                    number(object.get("left"));
                    number(object.get("right"));
                }
                case "clamp" -> {
                    number(object.get("value"));
                    number(object.get("minimum"));
                    number(object.get("maximum"));
                }
                case "constant" -> {
                }
                default -> rejectUnknownReference(object);
            }
        });
    }

    private void step(JsonObject value) {
        ResourceLocation type = id(value.get("type"));
        withObject(value.get("data"), data -> {
            if (GachaBuiltinExecutionSteps.REWARD_POOL.equals(type)) {
                reference(data.get("target"), GachaDefinitionKind.REWARD_POOL);
                number(data.get("rounds"));
            } else if (GachaBuiltinExecutionSteps.CUSTOM_TABLE.equals(type)) {
                reference(data.get("target"), GachaDefinitionKind.TABLE);
                number(data.get("rounds"));
            } else if (GachaBuiltinExecutionSteps.VANILLA_LOOT_TABLE.equals(type)) {
                number(data.get("rounds"));
            } else if (GachaBuiltinExecutionSteps.EXECUTION_PLAN.equals(type)) {
                reference(data.get("target"), GachaDefinitionKind.EXECUTION_PLAN);
            } else if (GachaBuiltinExecutionSteps.RULE_GROUP.equals(type)) {
                reference(data.get("target"), GachaDefinitionKind.RULE_GROUP);
            } else if (GachaBuiltinExecutionSteps.QUANTITY_RULE.equals(type)) {
                reference(data.get("target"), GachaDefinitionKind.QUANTITY_RULE);
            } else if (GachaBuiltinExecutionSteps.CONSUMPTION.equals(type)) {
                eachObject(data.get("entries"), this::consumption);
            } else {
                rejectUnknownReference(data);
                return;
            }
            eachObject(data.get("parameters"), mapping -> {
                withObject(mapping.get("number"), numeric -> number(numeric.get("value")));
                withObject(mapping.get("constant"), constant ->
                        extensionParameterValue(id(constant.get("type")), constant.get("value")));
            });
        });
    }

    private void parameterOwner(JsonElement value) {
        ResourceLocation owner = id(value);
        if (owner == null) return;
        ResourceLocation poolOwner = renamedReferences.getOrDefault(GachaDefinitionKind.REWARD_POOL, Map.of()).get(owner);
        ResourceLocation planOwner = renamedReferences.getOrDefault(GachaDefinitionKind.EXECUTION_PLAN, Map.of()).get(owner);
        if (poolOwner != null && planOwner != null) throw unsafeReference();
        ResourceLocation replacement = poolOwner == null ? planOwner : poolOwner;
        if (replacement == null) return;
        GachaDefinitionKind otherKind = poolOwner == null
                ? GachaDefinitionKind.REWARD_POOL : GachaDefinitionKind.EXECUTION_PLAN;
        if (hasParameterOwner(otherKind, owner) || hasParameterOwner(otherKind, replacement)) throw unsafeReference();
        replacements.put(value, replacement.toString());
    }

    private boolean hasParameterOwner(GachaDefinitionKind kind, ResourceLocation id) {
        return documents.containsKey(new GachaDefinitionDocumentKey(kind, id))
                || kind == GachaDefinitionKind.EXECUTION_PLAN && generatedPlanIds.contains(id);
    }

    private void consumption(JsonObject value) {
        if (!GachaPlayerInventoryConsumptionHandler.TYPE.equals(id(value.get("type")))) {
            rejectUnknownReference(value.get("data"));
        }
    }

    private void vanillaExtensions(JsonElement value) {
        if (value == null) return;
        if (value.isJsonObject()) {
            JsonObject object = value.getAsJsonObject();
            for (String field : List.of("condition", "function", "type")) {
                ResourceLocation type = id(object.get(field));
                if (type != null && !type.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE)) {
                    rejectUnknownReference(object);
                }
            }
            object.entrySet().forEach(entry -> vanillaExtensions(entry.getValue()));
        } else if (value.isJsonArray()) {
            value.getAsJsonArray().forEach(this::vanillaExtensions);
        }
    }

    private void parameterDefinition(JsonObject definition) {
        extensionParameterValue(id(definition.get("type")), definition.get("default"));
    }

    private void extensionParameterValue(ResourceLocation type, JsonElement value) {
        if (type != null && !BUILTIN_PARAMETER_TYPES.contains(type)) rejectUnknownReference(value);
    }

    private void parameterValues(JsonElement value, Map<String, ResourceLocation> types) {
        withObject(value, values -> values.entrySet().forEach(entry ->
                extensionParameterValue(types.get(entry.getKey()), entry.getValue())));
    }

    private Map<String, ResourceLocation> parameterTypes(JsonObject root) {
        GachaDefinitionKind kind = root.has("execution_plan")
                ? GachaDefinitionKind.EXECUTION_PLAN : GachaDefinitionKind.REWARD_POOL;
        ResourceLocation owner = id(root.get(root.has("execution_plan") ? "execution_plan" : "reward_pool"));
        if (owner == null) return Map.of();
        String content = documents.get(new GachaDefinitionDocumentKey(kind, owner));
        if (content == null) return Map.of();
        JsonElement schema = JsonParser.parseString(content);
        Map<String, ResourceLocation> types = new LinkedHashMap<>();
        withObject(schema, definition -> eachObject(definition.get("parameters"), parameter -> {
            ResourceLocation type = id(parameter.get("type"));
            if (type != null) types.put(text(parameter.get("name")), type);
        }));
        return types;
    }

    private void reference(JsonElement value, GachaDefinitionKind kind) {
        ResourceLocation referencedId = id(value);
        if (referencedId == null) return;
        ResourceLocation replacement = renamedReferences.getOrDefault(kind, Map.of()).get(referencedId);
        if (replacement != null) replacements.put(value, replacement.toString());
    }

    private void references(JsonElement value, GachaDefinitionKind kind) {
        if (value != null && value.isJsonArray()) {
            value.getAsJsonArray().forEach(entry -> reference(entry, kind));
        }
    }

    private boolean matches(JsonElement value) {
        return oldKey.id().equals(id(value));
    }

    private void rejectUnknownReference(JsonElement value) {
        if (value == null) return;
        if (value.isJsonObject()) {
            value.getAsJsonObject().entrySet().forEach(entry -> {
                if (isRenamedId(ResourceLocation.tryParse(entry.getKey()))) throw unsafeReference();
                rejectUnknownReference(entry.getValue());
            });
        } else if (value.isJsonArray()) {
            value.getAsJsonArray().forEach(this::rejectUnknownReference);
        } else if (isRenamedId(id(value))) {
            throw unsafeReference();
        }
    }

    private boolean isRenamedId(ResourceLocation id) {
        return id != null && renamedReferences.values().stream().anyMatch(references -> references.containsKey(id));
    }

    private void requireDepth(JsonElement value, int depth) {
        if (depth > MAXIMUM_JSON_DEPTH) throw unsafeReference();
        if (value.isJsonObject()) {
            value.getAsJsonObject().entrySet().forEach(entry -> requireDepth(entry.getValue(), depth + 1));
        } else if (value.isJsonArray()) {
            value.getAsJsonArray().forEach(entry -> requireDepth(entry, depth + 1));
        }
    }

    private JsonParseException unsafeReference() {
        return new JsonParseException(Component.translatable(
                TranslationKeys.GUI_ADMIN_WORKBENCH_RENAME_UNSAFE_REFERENCE, location).getString());
    }

    private record Replacement(int start, int end, String value) {
    }

    private class SourceEditor {
        private final String content;
        private final JsonElement root;
        private final List<Replacement> edits = new ArrayList<>();
        private int cursor;

        private SourceEditor(String content, JsonElement root) {
            this.content = content;
            this.root = root;
        }

        private String rewrite() {
            value(root);
            whitespace();
            if (cursor != content.length() || edits.size() != replacements.size()) throw unsafeReference();
            StringBuilder updated = new StringBuilder(content);
            for (int index = edits.size() - 1; index >= 0; index--) {
                Replacement edit = edits.get(index);
                updated.replace(edit.start(), edit.end(), edit.value());
            }
            return updated.toString();
        }

        private void value(JsonElement expected) {
            whitespace();
            if (expected == null || cursor >= content.length()) throw unsafeReference();
            char token = content.charAt(cursor);
            if (token == '{' && expected.isJsonObject()) {
                cursor++;
                whitespace();
                if (take('}')) return;
                Set<String> keys = new HashSet<>();
                do {
                    whitespace();
                    int start = cursor;
                    string();
                    String key = JsonParser.parseString(content.substring(start, cursor)).getAsString();
                    if (!keys.add(key)) throw unsafeReference();
                    whitespace();
                    require(':');
                    value(expected.getAsJsonObject().get(key));
                    whitespace();
                    if (take('}')) return;
                    require(',');
                } while (true);
            }
            if (token == '[' && expected.isJsonArray()) {
                cursor++;
                whitespace();
                int index = 0;
                if (!take(']')) {
                    do {
                        if (index >= expected.getAsJsonArray().size()) throw unsafeReference();
                        value(expected.getAsJsonArray().get(index++));
                        whitespace();
                        if (take(']')) break;
                        require(',');
                    } while (true);
                }
                if (index != expected.getAsJsonArray().size()) throw unsafeReference();
                return;
            }
            int start = cursor;
            if (token == '"') {
                string();
            } else {
                while (cursor < content.length() && ",]} \t\r\n".indexOf(content.charAt(cursor)) < 0) cursor++;
            }
            if (start == cursor || !expected.equals(JsonParser.parseString(content.substring(start, cursor)))) {
                throw unsafeReference();
            }
            String replacement = replacements.get(expected);
            if (replacement != null) {
                edits.add(new Replacement(start, cursor, new JsonPrimitive(replacement).toString()));
            }
        }

        private void string() {
            require('"');
            while (cursor < content.length()) {
                char current = content.charAt(cursor++);
                if (current == '"') return;
                if (current == '\\') cursor++;
            }
            throw unsafeReference();
        }

        private void whitespace() {
            while (cursor < content.length() && Character.isWhitespace(content.charAt(cursor))) cursor++;
        }

        private boolean take(char expected) {
            if (cursor < content.length() && content.charAt(cursor) == expected) {
                cursor++;
                return true;
            }
            return false;
        }

        private void require(char expected) {
            if (!take(expected)) throw unsafeReference();
        }
    }
}

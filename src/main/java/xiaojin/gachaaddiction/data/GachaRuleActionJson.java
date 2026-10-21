package xiaojin.gachaaddiction.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterDefinition;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterKey;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterSchema;
import xiaojin.gachaaddiction.reward.pity.GachaPityOutputs;
import xiaojin.gachaaddiction.reward.pity.GachaPityRuleAction;
import xiaojin.gachaaddiction.reward.quantity.*;
import xiaojin.gachaaddiction.reward.rule.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class GachaRuleActionJson {
    private final RegistryOps<JsonElement> ops;
    private final HolderLookup.RegistryLookup<Item> itemRegistry;
    private final GachaRuleConditionJson conditions;
    private final GachaSharedDefinitionExpander sharedDefinitions;

    public GachaRuleActionJson(HolderLookup.Provider registries, GachaRuleConditionJson conditions,
                               GachaSharedDefinitionExpander sharedDefinitions) {
        this.ops = RegistryOps.create(JsonOps.INSTANCE, registries);
        this.itemRegistry = registries.lookupOrThrow(Registries.ITEM);
        this.conditions = conditions;
        this.sharedDefinitions = sharedDefinitions;
    }

    private static <T> Optional<GachaParameterDefinition<T>> optionalParameter(ResourceLocation owner,
                                                                               GachaParameterSchema schema,
                                                                               JsonObject json, String field,
                                                                               Class<T> valueClass) {
        return json.has(field) ? Optional.of(requireParameter(owner, schema,
                GachaJson.string(json, field, "pity outputs"), valueClass)) : Optional.empty();
    }

    private static <T> GachaParameterDefinition<T> requireParameter(ResourceLocation owner,
                                                                    GachaParameterSchema schema,
                                                                    String name, Class<T> valueClass) {
        GachaParameterDefinition<?> definition = schema.require(new GachaParameterKey(owner, name));
        if (definition.type().valueClass() != valueClass) {
            throw new JsonParseException("Parameter has unexpected type: " + definition.key());
        }
        return castDefinition(definition);
    }

    @SuppressWarnings("unchecked")
    private static <T> GachaParameterDefinition<T> castDefinition(GachaParameterDefinition<?> definition) {
        return (GachaParameterDefinition<T>) definition;
    }

    private static JsonObject requireObject(JsonElement json, String location) {
        if (json == null || !json.isJsonObject()) {
            throw new JsonParseException("Rule action must be an object in " + location);
        }
        return json.getAsJsonObject();
    }

    public List<IGachaRuleAction> array(ResourceLocation owner, JsonArray actions,
                                        GachaParameterSchema schema, String location) {
        List<IGachaRuleAction> decoded = new ArrayList<>();
        for (int index = 0; index < actions.size(); index++) {
            decoded.add(action(owner, requireObject(actions.get(index), location + "[" + index + "]"), schema,
                    location + "[" + index + "]"));
        }
        return List.copyOf(decoded);
    }

    public IGachaRuleAction action(ResourceLocation owner, JsonObject json,
                                   GachaParameterSchema schema, String location) {
        String type = GachaJson.string(json, "type", location);
        return switch (type) {
            case "remove" -> {
                GachaJson.requireOnly(json, Set.of("type"), location);
                yield new GachaRemoveRewardAction();
            }
            case "set_double_parameter" -> {
                GachaJson.requireOnly(json, Set.of("type", "parameter", "value"), location);
                yield new GachaSetDoubleParameterAction(requireParameter(owner, schema,
                        GachaJson.string(json, "parameter", location), Double.class),
                        GachaNumberProviderJson.doubleProvider(json.get("value"), location + ".value"));
            }
            case "set_long_parameter" -> {
                GachaJson.requireOnly(json, Set.of("type", "parameter", "value"), location);
                yield new GachaSetLongParameterAction(requireParameter(owner, schema,
                        GachaJson.string(json, "parameter", location), Long.class),
                        GachaNumberProviderJson.longProvider(json.get("value"), location + ".value"));
            }
            case "append_item" -> appendItem(json, location);
            case "vanilla_function" -> vanillaFunction(json, location);
            case "quantity" -> quantity(json, location);
            case "pity" -> pity(owner, json, schema, location);
            default -> throw new JsonParseException("Unknown rule action type '" + type + "' in " + location);
        };
    }

    private IGachaRuleAction appendItem(JsonObject json, String location) {
        GachaJson.requireOnly(json, Set.of("type", "id", "item", "stack", "quantity"), location);
        return new GachaAppendItemAction(ResourceLocation.parse(GachaJson.string(json, "id", location)),
                stack(json, location), GachaNumberProviderJson.longProvider(json.get("quantity"),
                location + ".quantity"));
    }

    private IGachaRuleAction vanillaFunction(JsonObject json, String location) {
        GachaJson.requireOnly(json, Set.of("type", "id", "value"), location);
        LootItemFunction function = LootItemFunctions.ROOT_CODEC.parse(ops, json.get("value"))
                .getOrThrow(JsonParseException::new);
        return new GachaVanillaLootFunctionAction(
                ResourceLocation.parse(GachaJson.string(json, "id", location)), function);
    }

    private IGachaRuleAction quantity(JsonObject json, String location) {
        GachaJson.requireOnly(json, Set.of("type", "operations"), location);
        return new GachaQuantityRuleAction(quantityPipeline(
                GachaJson.array(json, "operations", location), location + ".operations"));
    }

    public GachaQuantityPipeline quantityPipeline(JsonArray encodedOperations, String location) {
        JsonArray expandedOperations = sharedDefinitions.expandQuantityEntries(encodedOperations, location);
        List<IGachaQuantityOperation> decodedOperations = new ArrayList<>();
        for (int index = 0; index < expandedOperations.size(); index++) {
            String operationLocation = location + "[" + index + "]";
            JsonObject operation = requireObject(expandedOperations.get(index), operationLocation);
            decodedOperations.add(quantityOperation(operation, operationLocation));
        }
        return new GachaQuantityPipeline(decodedOperations);
    }

    private IGachaQuantityOperation quantityOperation(JsonObject json, String location) {
        String type = GachaJson.string(json, "type", location);
        boolean stop = GachaJson.bool(json, "stop", false);
        return switch (type) {
            case "keep" -> {
                GachaJson.requireOnly(json, Set.of("type", "stop"), location);
                yield new GachaQuantityOperations.Keep(stop);
            }
            case "set" -> {
                GachaJson.requireOnly(json, Set.of("type", "value", "stop"), location);
                yield new GachaQuantityOperations.Set(GachaNumberProviderJson.longProvider(json.get("value"),
                        location + ".value"), stop);
            }
            case "add" -> {
                GachaJson.requireOnly(json, Set.of("type", "value", "stop"), location);
                yield new GachaQuantityOperations.Add(GachaNumberProviderJson.longProvider(json.get("value"),
                        location + ".value"), stop);
            }
            case "multiply" -> {
                GachaJson.requireOnly(json, Set.of("type", "factor", "rounding", "stop"), location);
                yield new GachaQuantityOperations.Multiply(
                        GachaNumberProviderJson.doubleProvider(json.get("factor"), location + ".factor"),
                        GachaQuantityRounding.valueOf(GachaJson.string(json, "rounding", location)), stop);
            }
            case "min" -> {
                GachaJson.requireOnly(json, Set.of("type", "value", "stop"), location);
                yield new GachaQuantityOperations.Min(GachaNumberProviderJson.longProvider(json.get("value"),
                        location + ".value"), stop);
            }
            case "max" -> {
                GachaJson.requireOnly(json, Set.of("type", "value", "stop"), location);
                yield new GachaQuantityOperations.Max(GachaNumberProviderJson.longProvider(json.get("value"),
                        location + ".value"), stop);
            }
            case "clamp" -> {
                GachaJson.requireOnly(json, Set.of("type", "minimum", "maximum", "stop"), location);
                yield new GachaQuantityOperations.Clamp(
                        GachaNumberProviderJson.longProvider(json.get("minimum"), location + ".minimum"),
                        GachaNumberProviderJson.longProvider(json.get("maximum"), location + ".maximum"), stop);
            }
            case "roll" -> {
                GachaJson.requireOnly(json, Set.of("type", "minimum", "maximum", "stop"), location);
                yield new GachaQuantityOperations.Roll(
                        GachaNumberProviderJson.longProvider(json.get("minimum"), location + ".minimum"),
                        GachaNumberProviderJson.longProvider(json.get("maximum"), location + ".maximum"), stop);
            }
            default -> throw new JsonParseException("Unknown quantity operation type '" + type + "' in " + location);
        };
    }

    private IGachaRuleAction pity(ResourceLocation owner, JsonObject json,
                                  GachaParameterSchema schema, String location) {
        GachaJson.requireOnly(json, Set.of("type", "miss_counter", "hard_pity_draw", "hit_condition",
                "hard_action", "outputs"), location);
        JsonObject outputs = GachaJson.object(json, "outputs", location);
        GachaJson.requireOnly(outputs, Set.of("natural_hit", "pity_triggered", "final_hit"),
                location + ".outputs");
        return new GachaPityRuleAction(requireParameter(owner, schema,
                GachaJson.string(json, "miss_counter", location), Long.class),
                GachaNumberProviderJson.longProvider(json.get("hard_pity_draw"), location + ".hard_pity_draw"),
                conditions.condition(json.get("hit_condition"), location + ".hit_condition"),
                action(owner, GachaJson.object(json, "hard_action", location), schema, location + ".hard_action"),
                new GachaPityOutputs(optionalParameter(owner, schema, outputs, "natural_hit", Boolean.class),
                        optionalParameter(owner, schema, outputs, "pity_triggered", Boolean.class),
                        optionalParameter(owner, schema, outputs, "final_hit", Boolean.class)));
    }

    private ItemStack stack(JsonObject json, String location) {
        boolean hasItem = json.has("item");
        boolean hasStack = json.has("stack");
        if (hasItem == hasStack) {
            throw new JsonParseException("Item action requires exactly one of item or stack in " + location);
        }
        if (hasStack) {
            return ItemStack.CODEC.parse(ops, json.get("stack")).getOrThrow(JsonParseException::new);
        }
        ResourceLocation itemId = ResourceLocation.parse(GachaJson.string(json, "item", location));
        Item item = itemRegistry.get(ResourceKey.create(Registries.ITEM, itemId)).map(holder -> holder.value())
                .orElseThrow(() -> new JsonParseException("Unknown item '" + itemId + "' in " + location));
        return new ItemStack(item);
    }
}

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
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import xiaojin.gachaaddiction.reward.provenance.GachaProvenanceSelector;
import xiaojin.gachaaddiction.reward.provenance.GachaRewardOrigin;
import xiaojin.gachaaddiction.reward.rule.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class GachaRuleConditionJson {
    private final RegistryOps<JsonElement> ops;
    private final HolderLookup.RegistryLookup<Item> itemRegistry;
    private final GachaSharedDefinitionExpander sharedDefinitions;

    public GachaRuleConditionJson(HolderLookup.Provider registries,
                                  GachaSharedDefinitionExpander sharedDefinitions) {
        this.ops = RegistryOps.create(JsonOps.INSTANCE, registries);
        this.itemRegistry = registries.lookupOrThrow(Registries.ITEM);
        this.sharedDefinitions = sharedDefinitions;
    }

    public List<GachaRuleCondition> array(JsonArray conditions, String location) {
        JsonArray expanded = sharedDefinitions.expandConditionEntries(conditions, location);
        List<GachaRuleCondition> decoded = new ArrayList<>();
        for (int index = 0; index < expanded.size(); index++) {
            decoded.add(condition(expanded.get(index), location + "[" + index + "]"));
        }
        return List.copyOf(decoded);
    }

    public List<GachaRuleCondition> customEntryArray(JsonArray conditions, String location) {
        JsonArray expanded = sharedDefinitions.expandConditionEntries(conditions, location);
        List<GachaRuleCondition> decoded = new ArrayList<>();
        for (int index = 0; index < expanded.size(); index++) {
            String conditionLocation = location + "[" + index + "]";
            validateCustomEntryCondition(expanded.get(index), conditionLocation);
            decoded.add(condition(expanded.get(index), conditionLocation));
        }
        return List.copyOf(decoded);
    }

    private void validateCustomEntryCondition(JsonElement json, String location) {
        if (json == null || !json.isJsonObject()) {
            throw new JsonParseException("Condition must be an object in " + location);
        }
        JsonObject object = json.getAsJsonObject();
        if (object.has("condition")) {
            return;
        }
        String type = GachaJson.string(object, "type", location);
        switch (type) {
            case "any_reward", "reward_item", "reward_item_tag", "provenance" ->
                    throw new JsonParseException("Condition type '" + type
                            + "' requires generated rewards and is not valid for custom entry conditions in "
                            + location);
            case "not" -> validateCustomEntryCondition(object.get("value"), location + ".value");
            case "all" -> validateCustomEntryArray(GachaJson.array(object, "values", location),
                    location + ".values");
            default -> {
            }
        }
    }

    private void validateCustomEntryArray(JsonArray conditions, String location) {
        JsonArray expanded = sharedDefinitions.expandConditionEntries(conditions, location);
        for (int index = 0; index < expanded.size(); index++) {
            validateCustomEntryCondition(expanded.get(index), location + "[" + index + "]");
        }
    }

    public GachaRuleCondition condition(JsonElement json, String location) {
        if (json == null || !json.isJsonObject()) {
            throw new JsonParseException("Condition must be an object in " + location);
        }
        JsonObject object = json.getAsJsonObject();
        if (object.has("condition")) {
            LootItemCondition condition = LootItemCondition.DIRECT_CODEC.parse(ops, object)
                    .getOrThrow(JsonParseException::new);
            return new GachaVanillaLootCondition(condition);
        }
        String type = GachaJson.string(object, "type", location);
        return switch (type) {
            case "chance" -> {
                GachaJson.requireOnly(object, Set.of("type", "chance"), location);
                yield new GachaChanceCondition(GachaNumberProviderJson.doubleProvider(object.get("chance"),
                        location + ".chance"));
            }
            case "number_compare" -> {
                GachaJson.requireOnly(object, Set.of("type", "left", "operator", "right"), location);
                yield new GachaNumberComparisonCondition(
                        GachaNumberProviderJson.doubleProvider(object.get("left"), location + ".left"),
                        GachaComparisonOperator.valueOf(GachaJson.string(object, "operator", location)),
                        GachaNumberProviderJson.doubleProvider(object.get("right"), location + ".right"));
            }
            case "not" -> {
                GachaJson.requireOnly(object, Set.of("type", "value"), location);
                yield new GachaNotCondition(condition(object.get("value"), location + ".value"));
            }
            case "all" -> {
                GachaJson.requireOnly(object, Set.of("type", "values"), location);
                yield new GachaAllConditions(array(GachaJson.array(object, "values", location),
                        location + ".values"));
            }
            case "any_reward" -> {
                GachaJson.requireOnly(object, Set.of("type", "value"), location);
                yield new GachaAnyRewardCondition(condition(object.get("value"), location + ".value"));
            }
            case "reward_item" -> {
                GachaJson.requireOnly(object, Set.of("type", "item"), location);
                ResourceLocation itemId = ResourceLocation.parse(GachaJson.string(object, "item", location));
                Item item = itemRegistry.get(ResourceKey.create(Registries.ITEM, itemId))
                        .map(holder -> holder.value())
                        .orElseThrow(() -> new JsonParseException("Unknown item '" + itemId + "' in " + location));
                yield new GachaRewardItemCondition(item);
            }
            case "reward_item_tag" -> {
                GachaJson.requireOnly(object, Set.of("type", "tag"), location);
                ResourceLocation tag = ResourceLocation.parse(GachaJson.string(object, "tag", location));
                yield new GachaRewardItemTagCondition(TagKey.create(Registries.ITEM, tag));
            }
            case "provenance" -> {
                GachaJson.requireOnly(object, Set.of("type", "selector", "origin_type", "origin"), location);
                yield new GachaRewardProvenanceCondition(
                        GachaProvenanceSelector.valueOf(GachaJson.string(object, "selector", location)),
                        new GachaRewardOrigin(
                                ResourceLocation.parse(GachaJson.string(object, "origin_type", location)),
                                ResourceLocation.parse(GachaJson.string(object, "origin", location))));
            }
            default -> throw new JsonParseException("Unknown custom condition type '" + type + "' in " + location);
        };
    }
}

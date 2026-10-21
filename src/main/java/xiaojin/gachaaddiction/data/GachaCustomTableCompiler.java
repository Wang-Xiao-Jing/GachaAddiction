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
import net.minecraft.world.item.ItemStack;
import xiaojin.gachaaddiction.reward.custom.*;
import xiaojin.gachaaddiction.reward.number.GachaConstantDoubleProvider;
import xiaojin.gachaaddiction.reward.number.GachaConstantLongProvider;
import xiaojin.gachaaddiction.reward.number.GachaLongProvider;
import xiaojin.gachaaddiction.reward.rule.GachaRuleCondition;
import xiaojin.gachaaddiction.reward.source.GachaTableSelectionStrategy;

import java.util.*;

public final class GachaCustomTableCompiler {
    private static final Set<String> TABLE_FIELDS = Set.of("format_version", "strategy", "selection_count",
            "entries");
    private static final Set<String> COMMON_ENTRY_FIELDS = Set.of("id", "type", "weight", "conditions");

    private final RegistryOps<JsonElement> ops;
    private final HolderLookup.RegistryLookup<Item> itemRegistry;
    private final GachaRuleConditionJson conditions;
    private final Map<ResourceLocation, GachaCustomLootTable> compiled = new LinkedHashMap<>();
    private final Set<ResourceLocation> references = new LinkedHashSet<>();
    private final GachaCustomTableResolver resolver = id -> Optional.ofNullable(compiled.get(id));

    public GachaCustomTableCompiler(HolderLookup.Provider registries,
                                    GachaSharedDefinitionExpander sharedDefinitions) {
        this.ops = RegistryOps.create(JsonOps.INSTANCE, registries);
        this.itemRegistry = registries.lookupOrThrow(Registries.ITEM);
        this.conditions = new GachaRuleConditionJson(registries, sharedDefinitions);
    }

    private static void requireEntryFields(JsonObject json, String location, Set<String> specificFields) {
        Set<String> allowed = new LinkedHashSet<>(COMMON_ENTRY_FIELDS);
        allowed.addAll(specificFields);
        GachaJson.requireOnly(json, allowed, location);
    }

    private static JsonObject requireObject(JsonElement json, String location) {
        if (json == null || !json.isJsonObject()) {
            throw new JsonParseException("Custom entry must be an object in " + location);
        }
        return json.getAsJsonObject();
    }

    public Map<ResourceLocation, GachaCustomLootTable> compile(
            Map<ResourceLocation, GachaRawDefinition> definitions) {
        compiled.clear();
        references.clear();
        definitions.forEach((id, definition) -> compiled.put(id, compileTable(id, definition.data())));
        for (ResourceLocation reference : references) {
            if (!compiled.containsKey(reference)) {
                throw new JsonParseException("Unknown referenced custom loot table: " + reference);
            }
        }
        return Map.copyOf(compiled);
    }

    private GachaCustomLootTable compileTable(ResourceLocation id, JsonObject json) {
        String location = "custom loot table " + id;
        GachaJson.requireOnly(json, TABLE_FIELDS, location);
        GachaTableSelectionStrategy strategy = GachaTableSelectionStrategy.valueOf(
                GachaJson.string(json, "strategy", location));
        GachaLongProvider selectionCount = json.has("selection_count")
                ? GachaNumberProviderJson.longProvider(json.get("selection_count"), location + ".selection_count")
                : new GachaConstantLongProvider(1);
        JsonArray entries = GachaJson.array(json, "entries", location);
        List<GachaWeightedCustomEntry> compiledEntries = new ArrayList<>();
        for (int index = 0; index < entries.size(); index++) {
            JsonObject entry = requireObject(entries.get(index), location + ".entries[" + index + "]");
            String entryLocation = location + ".entries[" + index + "]";
            GachaCustomEntry compiledEntry = compileEntry(entry, entryLocation);
            compiledEntries.add(new GachaWeightedCustomEntry(compiledEntry, entry.has("weight")
                    ? GachaNumberProviderJson.doubleProvider(entry.get("weight"), entryLocation + ".weight")
                    : new GachaConstantDoubleProvider(1)));
        }
        return new GachaCustomLootTable(id, strategy, selectionCount, compiledEntries);
    }

    private GachaCustomEntry compileEntry(JsonObject json, String location) {
        ResourceLocation id = ResourceLocation.parse(GachaJson.string(json, "id", location));
        String type = GachaJson.string(json, "type", location);
        GachaCustomEntry entry = switch (type) {
            case "item" -> itemEntry(id, json, location);
            case "item_tag" -> itemTagEntry(id, json, location);
            case "vanilla_loot_table" -> vanillaLootTableEntry(id, json, location);
            case "custom_table" -> customTableEntry(id, json, location);
            case "empty" -> {
                requireEntryFields(json, location, Set.of());
                yield new GachaEmptyCustomEntry(id);
            }
            case "group" -> compositeEntry(id, GachaCompositeEntryMode.GROUP, json, location);
            case "sequence" -> compositeEntry(id, GachaCompositeEntryMode.SEQUENCE, json, location);
            case "alternatives" -> compositeEntry(id, GachaCompositeEntryMode.ALTERNATIVES, json, location);
            default -> throw new JsonParseException("Unknown custom entry type '" + type + "' in " + location);
        };
        if (json.has("conditions")) {
            List<GachaRuleCondition> entryConditions = conditions.customEntryArray(
                    GachaJson.array(json, "conditions", location), location + ".conditions");
            entry = new GachaConditionalCustomEntry(entry, entryConditions);
        }
        return entry;
    }

    private GachaCustomEntry itemEntry(ResourceLocation id, JsonObject json, String location) {
        requireEntryFields(json, location, Set.of("item", "stack", "quantity"));
        boolean hasItem = json.has("item");
        boolean hasStack = json.has("stack");
        if (hasItem == hasStack) {
            throw new JsonParseException("Item entry requires exactly one of item or stack in " + location);
        }
        ItemStack stack;
        if (hasStack) {
            stack = ItemStack.CODEC.parse(ops, json.get("stack")).getOrThrow(JsonParseException::new);
        } else {
            ResourceLocation itemId = ResourceLocation.parse(GachaJson.string(json, "item", location));
            Item item = itemRegistry.get(ResourceKey.create(Registries.ITEM, itemId)).map(holder -> holder.value())
                    .orElseThrow(() -> new JsonParseException("Unknown item '" + itemId + "' in " + location));
            stack = new ItemStack(item);
        }
        GachaLongProvider quantity = json.has("quantity")
                ? GachaNumberProviderJson.longProvider(json.get("quantity"), location + ".quantity")
                : new GachaConstantLongProvider(stack.getCount());
        return new GachaItemCustomEntry(id, stack, quantity);
    }

    private GachaCustomEntry itemTagEntry(ResourceLocation id, JsonObject json, String location) {
        requireEntryFields(json, location, Set.of("tag", "quantity"));
        TagKey<Item> tag = TagKey.create(Registries.ITEM,
                ResourceLocation.parse(GachaJson.string(json, "tag", location)));
        GachaLongProvider quantity = json.has("quantity")
                ? GachaNumberProviderJson.longProvider(json.get("quantity"), location + ".quantity")
                : new GachaConstantLongProvider(1);
        return new GachaItemTagCustomEntry(id, tag, quantity, requested -> {
            return itemRegistry.get(requested)
                    .map(holders -> holders.stream().map(holder -> holder.value()).toList())
                    .orElseGet(List::of);
        });
    }

    private GachaCustomEntry vanillaLootTableEntry(ResourceLocation id, JsonObject json, String location) {
        requireEntryFields(json, location, Set.of("table"));
        ResourceLocation table = ResourceLocation.parse(GachaJson.string(json, "table", location));
        return new GachaVanillaLootTableCustomEntry(id, ResourceKey.create(Registries.LOOT_TABLE, table));
    }

    private GachaCustomEntry customTableEntry(ResourceLocation id, JsonObject json, String location) {
        requireEntryFields(json, location, Set.of("table"));
        ResourceLocation table = ResourceLocation.parse(GachaJson.string(json, "table", location));
        references.add(table);
        return new GachaCustomTableReferenceEntry(id, table, resolver);
    }

    private GachaCustomEntry compositeEntry(ResourceLocation id, GachaCompositeEntryMode mode,
                                            JsonObject json, String location) {
        requireEntryFields(json, location, Set.of("children"));
        JsonArray children = GachaJson.array(json, "children", location);
        List<GachaCustomEntry> compiledChildren = new ArrayList<>();
        for (int index = 0; index < children.size(); index++) {
            compiledChildren.add(compileEntry(requireObject(children.get(index), location + ".children[" + index + "]"),
                    location + ".children[" + index + "]"));
        }
        return new GachaCompositeCustomEntry(id, mode, compiledChildren);
    }
}

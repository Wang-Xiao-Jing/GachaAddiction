package xiaojin.gachaaddiction.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackLinkedSet;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import xiaojin.gachaaddiction.execution.GachaBuiltinExecutionSteps;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GachaPossibleRewardItems {
    private final ServerLevel level;
    private final GachaRawDefinitionSnapshot definitions;
    private final GachaSharedDefinitionExpander shared;
    private final RegistryOps<JsonElement> ops;
    private final Set<String> visited = new HashSet<>();
    private final Set<ItemStack> items = ItemStackLinkedSet.createTypeAndComponentsSet();
    private boolean smelting;
    private boolean enchanting;
    private boolean explorationMap;

    public GachaPossibleRewardItems(ServerLevel level, GachaRawDefinitionSnapshot definitions) {
        this.level = level;
        this.definitions = definitions;
        shared = new GachaSharedDefinitionExpander(definitions);
        ops = RegistryOps.create(JsonOps.INSTANCE, level.getServer().reloadableRegistries().get());
    }

    public List<ItemStack> collect(ResourceLocation executionPlan) {
        definition(GachaDefinitionKind.EXECUTION_PLAN, executionPlan);
        transformedItems();
        return items.stream().map(ItemStack::copy).toList();
    }

    private void definition(GachaDefinitionKind kind, ResourceLocation id) {
        if (!visited.add(kind.name() + ":" + id)) {
            return;
        }

        JsonObject root = definitions.find(kind, id).orElseThrow(() ->
                new IllegalArgumentException("Missing possible reward definition: " + kind + " " + id)).data();
        switch (kind) {
            case EXECUTION_PLAN -> array(root, "steps").forEach(value -> step(value.getAsJsonObject()));
            case REWARD_POOL -> {
                shared.sources(array(root, "sources"), array(root, "source_groups"), id.toString())
                        .forEach(value -> source(value.getAsJsonObject()));
                rules(shared.rules(array(root, "rules"), array(root, "rule_groups"), id.toString()));
            }
            case TABLE -> array(root, "entries").forEach(value -> entry(value.getAsJsonObject()));
            case RULE_GROUP -> rules(shared.ruleGroup(id, id.toString()));
            default -> throw new IllegalArgumentException("Unsupported possible reward definition: " + kind);
        }
    }

    private void step(JsonObject step) {
        ResourceLocation type = id(step, "type");
        JsonObject data = step.getAsJsonObject("data");
        if (type.equals(GachaBuiltinExecutionSteps.CONSUMPTION)
                || type.equals(GachaBuiltinExecutionSteps.QUANTITY_RULE)) {
            return;
        }

        ResourceLocation target = id(data, "target");
        if (type.equals(GachaBuiltinExecutionSteps.REWARD_POOL)) {
            definition(GachaDefinitionKind.REWARD_POOL, target);
        } else if (type.equals(GachaBuiltinExecutionSteps.CUSTOM_TABLE)) {
            definition(GachaDefinitionKind.TABLE, target);
        } else if (type.equals(GachaBuiltinExecutionSteps.VANILLA_LOOT_TABLE)) {
            vanillaTable(target);
        } else if (type.equals(GachaBuiltinExecutionSteps.RULE_GROUP)) {
            definition(GachaDefinitionKind.RULE_GROUP, target);
        } else if (type.equals(GachaBuiltinExecutionSteps.EXECUTION_PLAN)) {
            definition(GachaDefinitionKind.EXECUTION_PLAN, target);
        } else {
            throw new IllegalArgumentException("Cannot enumerate possible items for execution step: " + type);
        }
    }

    private void source(JsonObject source) {
        if (isZero(source, "weight")) {
            return;
        }

        switch (source.get("type").getAsString()) {
            case "custom_table" -> definition(GachaDefinitionKind.TABLE, id(source, "table"));
            case "vanilla_loot_table" -> vanillaTable(id(source, "table"));
            default -> throw new IllegalArgumentException("Cannot enumerate possible reward source: " + source);
        }
    }

    private void entry(JsonObject entry) {
        if (isZero(entry, "weight") || isZero(entry, "quantity")) {
            return;
        }

        switch (entry.get("type").getAsString()) {
            case "item" -> stack(entry);
            case "item_tag" -> tag(id(entry, "tag"));
            case "custom_table" -> definition(GachaDefinitionKind.TABLE, id(entry, "table"));
            case "vanilla_loot_table" -> vanillaTable(id(entry, "table"));
            case "group", "sequence", "alternatives" -> array(entry, "children")
                    .forEach(value -> entry(value.getAsJsonObject()));
            case "empty" -> {
            }
            default -> throw new IllegalArgumentException("Cannot enumerate possible reward entry: " + entry);
        }
    }

    private void rules(JsonArray rules) {
        for (JsonElement value : rules) {
            JsonObject rule = value.getAsJsonObject();
            array(rule, "actions").forEach(action -> action(action.getAsJsonObject()));
            array(rule, "fallback_actions").forEach(action -> action(action.getAsJsonObject()));
        }
    }

    private void action(JsonObject action) {
        switch (action.get("type").getAsString()) {
            case "append_item" -> {
                if (!isZero(action, "quantity")) {
                    stack(action);
                }
            }
            case "pity" -> action(action.getAsJsonObject("hard_action"));
            case "vanilla_function" -> functions(action.get("value"));
            default -> {
            }
        }
    }

    private void vanillaTable(ResourceLocation id) {
        if (!visited.add("vanilla:" + id)) {
            return;
        }

        LootTable table = level.getServer().reloadableRegistries()
                .getLootTable(ResourceKey.create(Registries.LOOT_TABLE, id));
        vanillaTable(LootTable.DIRECT_CODEC.encodeStart(ops, table).getOrThrow().getAsJsonObject());
    }

    private void vanillaTable(JsonObject table) {
        for (JsonElement value : array(table, "pools")) {
            JsonObject pool = value.getAsJsonObject();
            array(pool, "entries").forEach(entry -> vanillaEntry(entry.getAsJsonObject()));
            functions(pool.get("functions"));
        }

        functions(table.get("functions"));
    }

    private void vanillaEntry(JsonObject entry) {
        if (isZero(entry, "weight")) {
            return;
        }

        switch (id(entry, "type").toString()) {
            case "minecraft:item" -> item(id(entry, "name"));
            case "minecraft:tag" -> tag(id(entry, "name"));
            case "minecraft:loot_table" -> {
                JsonElement value = entry.get("value");
                if (value.isJsonObject()) {
                    vanillaTable(value.getAsJsonObject());
                } else {
                    vanillaTable(ResourceLocation.parse(value.getAsString()));
                }
            }
            case "minecraft:group", "minecraft:sequence", "minecraft:alternatives" -> array(entry, "children")
                    .forEach(value -> vanillaEntry(value.getAsJsonObject()));
            case "minecraft:empty" -> {
            }
            default -> throw new IllegalArgumentException("Cannot enumerate possible vanilla loot entry: " + entry);
        }

        functions(entry.get("functions"));
    }

    private void functions(JsonElement encoded) {
        if (encoded == null || encoded.isJsonNull()) {
            return;
        }
        if (encoded.isJsonArray()) {
            encoded.getAsJsonArray().forEach(this::functions);
            return;
        }

        if (encoded.isJsonPrimitive()) {
            modifier(ResourceLocation.parse(encoded.getAsString()));
            return;
        }

        JsonObject function = encoded.getAsJsonObject();
        if (function.has("function")) {
            switch (id(function, "function").toString()) {
                case "minecraft:set_item" -> item(id(function, "item"));
                case "minecraft:reference" -> modifier(id(function, "name"));
                case "minecraft:furnace_smelt" -> smelting = true;
                case "minecraft:enchant_randomly", "minecraft:enchant_with_levels" -> enchanting = true;
                case "minecraft:exploration_map" -> explorationMap = true;
                default -> {
                }
            }
        }

        functions(function.get("functions"));
    }

    private void modifier(ResourceLocation id) {
        if (!visited.add("modifier:" + id)) {
            return;
        }

        level.getServer().reloadableRegistries().get().registryOrThrow(Registries.ITEM_MODIFIER)
                .getOptional(id).ifPresent(function -> functions(
                        LootItemFunctions.ROOT_CODEC.encodeStart(ops, function).getOrThrow()));
    }

    private void transformedItems() {
        if (enchanting && items.stream().anyMatch(stack -> stack.is(Items.BOOK))) {
            add(Items.ENCHANTED_BOOK.getDefaultInstance());
        }
        if (explorationMap && items.stream().anyMatch(stack -> stack.is(Items.MAP))) {
            add(Items.FILLED_MAP.getDefaultInstance());
        }
        if (!smelting) {
            return;
        }

        boolean changed;
        do {
            int previousSize = items.size();
            for (var recipe : level.getRecipeManager().getAllRecipesFor(RecipeType.SMELTING)) {
                if (items.stream().anyMatch(stack -> recipe.value().matches(new SingleRecipeInput(stack), level))) {
                    add(recipe.value().getResultItem(level.registryAccess()));
                }
            }

            changed = items.size() != previousSize;
        } while (changed);
    }

    private void stack(JsonObject entry) {
        if (entry.has("stack")) {
            add(ItemStack.CODEC.parse(ops, entry.get("stack")).getOrThrow());
        } else {
            item(id(entry, "item"));
        }
    }

    private void item(ResourceLocation id) {
        level.registryAccess().registryOrThrow(Registries.ITEM).getOptional(id)
                .ifPresent(item -> add(item.getDefaultInstance()));
    }

    private void tag(ResourceLocation id) {
        level.registryAccess().registryOrThrow(Registries.ITEM).getTag(TagKey.create(Registries.ITEM, id))
                .ifPresent(holders -> holders.forEach(holder -> add(new ItemStack(holder))));
    }

    private void add(ItemStack stack) {
        if (!stack.isEmpty()) {
            items.add(stack.copyWithCount(1));
        }
    }

    private static JsonArray array(JsonObject root, String field) {
        return root.has(field) ? root.getAsJsonArray(field) : new JsonArray();
    }

    private static ResourceLocation id(JsonObject root, String field) {
        return ResourceLocation.parse(root.get(field).getAsString());
    }

    private static boolean isZero(JsonObject root, String field) {
        JsonElement value = root.get(field);
        return value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber()
                && value.getAsDouble() <= 0;
    }
}

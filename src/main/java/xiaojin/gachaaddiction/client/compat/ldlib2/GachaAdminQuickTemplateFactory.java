package xiaojin.gachaaddiction.client.compat.ldlib2;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.data.GachaDefinitionReloadListener;
import xiaojin.gachaaddiction.execution.GachaBuiltinExecutionSteps;

import java.util.Objects;

public final class GachaAdminQuickTemplateFactory {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CUSTOM_TABLE_STRATEGY = "SELECT_ONE";
    private static final String SOURCE_STRATEGY = "SELECT_ONE_SOURCE";
    private static final String VANILLA_LOOT_TABLE_SOURCE = "vanilla_loot_table";
    private static final String CUSTOM_LOOT_TABLE_SOURCE = "custom_table";
    private static final String ITEM_ENTRY_TYPE = "item";
    private static final String CARD_POOL_PACKAGE_ID = "default";
    private static final String CARD_POOL_TITLE_KEY = "gui.gachaaddiction.card_pool.title";
    private static final String CARD_POOL_MINIMUM_DURABILITY = "SYNC";
    private static final String CARD_POOL_RESULT_STYLE = "SUMMARY";
    private static final String CARD_POOL_DELIVERY_MODE = "INVENTORY";
    private static final int SINGLE_SELECTION = 1;
    private static final int MAX_LOGICAL_QUANTITY = 64;
    private static final int MAX_LOGICAL_REWARDS = 64;
    private static final int MAX_PHYSICAL_STACKS = 64;
    private static final int MAX_ROUNDS = 64;
    private static final int MAX_SOURCE_SELECTIONS = 64;
    private static final int MAX_RULE_ACTIONS = 128;
    private static final int MAX_CUSTOM_TABLE_DEPTH = 16;
    private static final int MAX_CUSTOM_ENTRY_VISITS = 1024;
    private static final ResourceLocation DEFAULT_ITEM = ResourceLocation.withDefaultNamespace("stone");
    private static final ResourceLocation DEFAULT_LOOT_TABLE = ResourceLocation.fromNamespaceAndPath(
            "minecraft", "chests/simple_dungeon");
    private static final String TEST_TARGET = "@s";
    private static final String EMPTY_PARAMETERS = "{}";
    private static final String PROBABILITY_KIND_REWARD_POOL = "reward_pool";
    private static final String PROBABILITY_KIND_TABLE = "table";

    private GachaAdminQuickTemplateFactory() {
    }

    public static String customLootTable(ResourceLocation id) {
        Objects.requireNonNull(id, "id");
        JsonObject root = base();
        root.addProperty("strategy", CUSTOM_TABLE_STRATEGY);
        root.addProperty("selection_count", SINGLE_SELECTION);
        JsonArray entries = new JsonArray();
        JsonObject entry = new JsonObject();
        entry.addProperty("id", id.toString());
        entry.addProperty("type", ITEM_ENTRY_TYPE);
        entry.addProperty("item", DEFAULT_ITEM.toString());
        entry.addProperty("quantity", SINGLE_SELECTION);
        entries.add(entry);
        root.add("entries", entries);
        return GSON.toJson(root);
    }

    public static String rewardPool(ResourceLocation id, ResourceLocation customTableId) {
        Objects.requireNonNull(id, "id");
        JsonObject root = base();
        root.add("parameters", new JsonArray());
        root.addProperty("source_strategy", SOURCE_STRATEGY);
        root.addProperty("source_count", SINGLE_SELECTION);
        JsonArray sources = new JsonArray();
        JsonObject source = new JsonObject();
        source.addProperty("id", id.toString());
        source.addProperty("type", VANILLA_LOOT_TABLE_SOURCE);
        source.addProperty("table", DEFAULT_LOOT_TABLE.toString());
        source.addProperty("weight", SINGLE_SELECTION);
        sources.add(source);
        if (customTableId != null) {
            JsonObject customSource = new JsonObject();
            customSource.addProperty("id", customSourceId(id).toString());
            customSource.addProperty("type", CUSTOM_LOOT_TABLE_SOURCE);
            customSource.addProperty("table", customTableId.toString());
            customSource.addProperty("weight", SINGLE_SELECTION);
            sources.add(customSource);
        }
        root.add("sources", sources);
        root.add("limits", limits());
        return GSON.toJson(root);
    }

    private static ResourceLocation customSourceId(ResourceLocation rewardPoolId) {
        return ResourceLocation.fromNamespaceAndPath(rewardPoolId.getNamespace(), rewardPoolId.getPath() + "_custom");
    }

    public static String executionPlan(ResourceLocation tableId) {
        Objects.requireNonNull(tableId, "tableId");
        JsonObject root = base();
        root.add("parameters", new JsonArray());
        JsonArray steps = new JsonArray();
        JsonObject step = new JsonObject();
        step.addProperty("type", GachaBuiltinExecutionSteps.CUSTOM_TABLE.toString());
        JsonObject data = new JsonObject();
        data.addProperty("target", tableId.toString());
        data.addProperty("rounds", SINGLE_SELECTION);
        data.add("limits", limits());
        step.add("data", data);
        steps.add(step);
        root.add("steps", steps);
        root.add("limits", limits());
        return GSON.toJson(root);
    }

    public static String cardPool(ResourceLocation id, ResourceLocation executionPlanId) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(executionPlanId, "executionPlanId");
        JsonObject root = base();
        root.addProperty("execution_plan", executionPlanId.toString());
        root.add("parameter_bindings", new JsonArray());
        JsonArray packages = new JsonArray();
        JsonObject drawPackage = new JsonObject();
        drawPackage.addProperty("id", CARD_POOL_PACKAGE_ID);
        drawPackage.add("parameters", new JsonObject());
        drawPackage.addProperty("minimum_durability", CARD_POOL_MINIMUM_DURABILITY);
        packages.add(drawPackage);
        root.add("packages", packages);
        JsonObject presentation = new JsonObject();
        presentation.addProperty("title", CARD_POOL_TITLE_KEY);
        JsonObject icon = new JsonObject();
        icon.addProperty("id", DEFAULT_ITEM.toString());
        icon.addProperty("count", SINGLE_SELECTION);
        presentation.add("icon", icon);
        presentation.addProperty("result_style", CARD_POOL_RESULT_STYLE);
        root.add("presentation", presentation);
        JsonObject availability = new JsonObject();
        availability.addProperty("enabled", true);
        availability.addProperty("minimum_permission_level", 0);
        root.add("availability", availability);
        JsonObject delivery = new JsonObject();
        delivery.addProperty("mode", CARD_POOL_DELIVERY_MODE);
        root.add("delivery", delivery);
        return GSON.toJson(root);
    }

    public static String executionPlanTestCommand(ResourceLocation planId, int rounds) {
        Objects.requireNonNull(planId, "planId");
        if (rounds <= 0) {
            throw new IllegalArgumentException("rounds must be positive");
        }
        return "/gacha test execution_plan_rounds " + TEST_TARGET + " " + planId + " " + rounds
                + " " + EMPTY_PARAMETERS;
    }

    public static String probabilityDescriptionCommand(String kind, ResourceLocation id) {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(id, "id");
        if (!kind.equals(PROBABILITY_KIND_REWARD_POOL) && !kind.equals(PROBABILITY_KIND_TABLE)) {
            throw new IllegalArgumentException("Unsupported probability kind: " + kind);
        }
        return "/gacha admin describe probability " + kind + " " + id;
    }

    private static JsonObject base() {
        JsonObject root = new JsonObject();
        root.addProperty(GachaDefinitionReloadListener.FORMAT_VERSION_KEY,
                GachaDefinitionReloadListener.CURRENT_FORMAT_VERSION);
        return root;
    }

    private static JsonObject limits() {
        JsonObject limits = new JsonObject();
        limits.addProperty("max_logical_quantity", MAX_LOGICAL_QUANTITY);
        limits.addProperty("max_logical_rewards", MAX_LOGICAL_REWARDS);
        limits.addProperty("max_physical_stacks", MAX_PHYSICAL_STACKS);
        limits.addProperty("max_rounds", MAX_ROUNDS);
        limits.addProperty("max_source_selections", MAX_SOURCE_SELECTIONS);
        limits.addProperty("max_rule_actions", MAX_RULE_ACTIONS);
        limits.addProperty("max_custom_table_depth", MAX_CUSTOM_TABLE_DEPTH);
        limits.addProperty("max_custom_entry_visits", MAX_CUSTOM_ENTRY_VISITS);
        return limits;
    }
}

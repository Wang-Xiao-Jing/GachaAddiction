package xiaojin.gachaaddiction.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import xiaojin.gachaaddiction.reward.quantity.GachaExecutionLimits;

import java.util.Set;

public final class GachaExecutionLimitsJson {
    private static final Set<String> FIELDS = Set.of("max_logical_quantity", "max_logical_rewards",
            "max_physical_stacks", "max_rounds", "max_source_selections", "max_rule_actions",
            "max_custom_table_depth", "max_custom_entry_visits");

    private GachaExecutionLimitsJson() {
    }

    public static GachaExecutionLimits decode(JsonObject json, String location) {
        GachaJson.requireOnly(json, FIELDS, location);
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
}

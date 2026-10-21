package xiaojin.gachaaddiction.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterSchema;
import xiaojin.gachaaddiction.reward.rule.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class GachaBuiltinRuleDefinitionCompiler implements GachaRuleDefinitionCompiler {
    private static final Set<String> FIELDS = Set.of("id", "phase", "scope", "conditions", "actions",
            "failure_policy", "fallback_actions");

    private final GachaRuleConditionJson conditions;
    private final GachaRuleActionJson actions;

    public GachaBuiltinRuleDefinitionCompiler(HolderLookup.Provider registries,
                                              GachaSharedDefinitionExpander sharedDefinitions) {
        this.conditions = new GachaRuleConditionJson(registries, sharedDefinitions);
        this.actions = new GachaRuleActionJson(registries, conditions, sharedDefinitions);
    }

    @Override
    public List<GachaRuleDefinition> compile(ResourceLocation rewardPoolId, JsonArray rules,
                                             GachaParameterSchema parameterSchema) {
        List<GachaRuleDefinition> compiled = new ArrayList<>();
        for (int index = 0; index < rules.size(); index++) {
            String location = "reward pool " + rewardPoolId + ".rules[" + index + "]";
            JsonElement value = rules.get(index);
            if (value == null || !value.isJsonObject()) {
                throw new JsonParseException("Rule must be an object in " + location);
            }
            JsonObject rule = value.getAsJsonObject();
            GachaJson.requireOnly(rule, FIELDS, location);
            GachaRuleFailurePolicy failurePolicy = GachaRuleFailurePolicy.valueOf(
                    GachaJson.string(rule, "failure_policy", location));
            List<GachaRuleCondition> compiledConditions = rule.has("conditions")
                    ? conditions.array(GachaJson.array(rule, "conditions", location), location + ".conditions")
                    : List.of();
            List<GachaRuleAction> compiledActions = actions.array(rewardPoolId,
                    GachaJson.array(rule, "actions", location), parameterSchema, location + ".actions");
            List<GachaRuleAction> fallbackActions = rule.has("fallback_actions")
                    ? actions.array(rewardPoolId, GachaJson.array(rule, "fallback_actions", location),
                    parameterSchema, location + ".fallback_actions") : List.of();
            compiled.add(new GachaRuleDefinition(
                    ResourceLocation.parse(GachaJson.string(rule, "id", location)),
                    GachaRulePhase.valueOf(GachaJson.string(rule, "phase", location)),
                    GachaRuleScope.valueOf(GachaJson.string(rule, "scope", location)),
                    compiledConditions, compiledActions, failurePolicy, fallbackActions));
        }
        return List.copyOf(compiled);
    }
}

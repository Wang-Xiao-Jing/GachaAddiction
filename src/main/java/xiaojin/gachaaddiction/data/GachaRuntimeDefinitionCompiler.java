package xiaojin.gachaaddiction.data;

import com.google.gson.JsonParseException;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.cardpool.GachaCardPoolDefinition;
import xiaojin.gachaaddiction.execution.*;
import xiaojin.gachaaddiction.registry.runtime.GachaRuntimeRegistries;
import xiaojin.gachaaddiction.reward.IGachaRewardPool;
import xiaojin.gachaaddiction.reward.custom.GachaCustomLootTable;
import xiaojin.gachaaddiction.reward.custom.IGachaCustomTableResolver;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterTypeRegistry;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class GachaRuntimeDefinitionCompiler {
    private final HolderLookup.Provider registries;
    private final GachaParameterTypeRegistry parameterTypes;

    public GachaRuntimeDefinitionCompiler(HolderLookup.Provider registries,
                                          GachaParameterTypeRegistry parameterTypes) {
        this.registries = Objects.requireNonNull(registries, "registries");
        this.parameterTypes = Objects.requireNonNull(parameterTypes, "parameterTypes");
    }

    public GachaCompiledDefinitionSnapshot compile(GachaRawDefinitionSnapshot raw) {
        Objects.requireNonNull(raw, "raw");
        GachaSharedDefinitionExpander sharedDefinitions = new GachaSharedDefinitionExpander(raw);
        GachaCustomTableCompiler customTableCompiler = new GachaCustomTableCompiler(registries, sharedDefinitions);
        Map<ResourceLocation, GachaCustomLootTable> customTables = customTableCompiler.compile(
                raw.definitions(GachaDefinitionKind.TABLE));
        IGachaCustomTableResolver customTableResolver = id -> Optional.ofNullable(customTables.get(id));
        GachaParameterDefinitionJson parameters = new GachaParameterDefinitionJson(parameterTypes, registries);
        GachaRewardPoolCompiler rewardPoolCompiler = new GachaRewardPoolCompiler(parameters, customTableResolver,
                new GachaBuiltinRuleDefinitionCompiler(registries, sharedDefinitions), sharedDefinitions);
        Map<ResourceLocation, IGachaRewardPool> rewardPools = rewardPoolCompiler.compile(
                raw.definitions(GachaDefinitionKind.REWARD_POOL));
        Map<ResourceLocation, GachaExecutionPlanDefinition> declaredExecutionPlans = new GachaExecutionPlanCompiler(
                parameters, GachaRuntimeRegistries.EXECUTION_STEP_TYPES).compile(
                raw.definitions(GachaDefinitionKind.EXECUTION_PLAN));
        Map<ResourceLocation, GachaCardPoolDefinition> cardPools = new GachaCardPoolCompiler(registries).compile(
                raw.definitions(GachaDefinitionKind.CARD_POOL), declaredExecutionPlans);
        Map<ResourceLocation, GachaExecutionPlanDefinition> executionPlans = hydrateExecutionPlans(
                declaredExecutionPlans, sharedDefinitions);
        GachaExecutionPlanValidator.validate(executionPlans, rewardPools, customTables,
                raw.definitions(GachaDefinitionKind.RULE_GROUP).keySet(),
                raw.definitions(GachaDefinitionKind.QUANTITY_RULE).keySet());
        Map<ResourceLocation, GachaRewardPoolRequestPreset> requestPresets =
                new GachaRequestPresetCompiler(registries).compile(
                        raw.definitions(GachaDefinitionKind.REQUEST_PRESET), rewardPools);
        return new GachaCompiledDefinitionSnapshot(customTables, rewardPools, cardPools, executionPlans,
                requestPresets);
    }

    private Map<ResourceLocation, GachaExecutionPlanDefinition> hydrateExecutionPlans(
            Map<ResourceLocation, GachaExecutionPlanDefinition> plans,
            GachaSharedDefinitionExpander sharedDefinitions) {
        GachaRuleConditionJson conditionJson = new GachaRuleConditionJson(registries, sharedDefinitions);
        GachaRuleActionJson actionJson = new GachaRuleActionJson(registries, conditionJson, sharedDefinitions);
        GachaBuiltinRuleDefinitionCompiler ruleCompiler = new GachaBuiltinRuleDefinitionCompiler(registries,
                sharedDefinitions);
        Map<ResourceLocation, GachaExecutionPlanDefinition> hydrated = new LinkedHashMap<>();
        plans.forEach((id, plan) -> hydrated.put(id, new GachaExecutionPlanDefinition(plan.id(),
                plan.parameterSchema(), plan.steps().stream()
                .map(step -> hydrateStep(plan, step, actionJson, ruleCompiler, sharedDefinitions))
                .toList(), plan.limits())));
        return Map.copyOf(hydrated);
    }

    private IGachaExecutionStep hydrateStep(GachaExecutionPlanDefinition plan, IGachaExecutionStep step,
                                            GachaRuleActionJson actionJson,
                                            GachaBuiltinRuleDefinitionCompiler ruleCompiler,
                                            GachaSharedDefinitionExpander sharedDefinitions) {
        if (!(step instanceof GachaComponentReferenceStep(
                ResourceLocation type, ResourceLocation target, GachaParameterMapping parameters,
                java.util.Set<GachaExecutionChannel> reads, java.util.Set<GachaExecutionChannel> writes
        ))) {
            return step;
        }
        if (!parameters.entries().isEmpty()
                && (type.equals(GachaBuiltinExecutionSteps.RULE_GROUP)
                || type.equals(GachaBuiltinExecutionSteps.QUANTITY_RULE))) {
            throw new JsonParseException("Standalone rule components do not support parameter mappings: "
                    + plan.id() + " -> " + target);
        }
        if (type.equals(GachaBuiltinExecutionSteps.RULE_GROUP)) {
            return new GachaRuleGroupExecutionStep(type, target,
                    ruleCompiler.compile(plan.id(), sharedDefinitions.ruleGroup(target,
                            "rule group " + target), plan.parameterSchema()),
                    reads, writes);
        }
        if (type.equals(GachaBuiltinExecutionSteps.QUANTITY_RULE)) {
            return new GachaQuantityRuleExecutionStep(type, target,
                    actionJson.quantityPipeline(sharedDefinitions.quantityRule(target,
                            "quantity rule " + target), "quantity rule " + target),
                    reads, writes);
        }
        return step;
    }
}

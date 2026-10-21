package xiaojin.gachaaddiction.execution;

import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.reward.IGachaRewardPool;
import xiaojin.gachaaddiction.reward.custom.GachaCustomLootTable;

import java.util.*;

public final class GachaExecutionPlanValidator {
    public static final int MAXIMUM_NESTING_DEPTH = 16;
    public static final int MAXIMUM_EXPANDED_STEPS = 1024;

    private GachaExecutionPlanValidator() {
    }

    public static void validate(Map<ResourceLocation, GachaExecutionPlanDefinition> plans,
                                Map<ResourceLocation, IGachaRewardPool> rewardPools,
                                Map<ResourceLocation, GachaCustomLootTable> customTables,
                                Set<ResourceLocation> ruleGroups,
                                Set<ResourceLocation> quantityRules) {
        Objects.requireNonNull(plans, "plans");
        Objects.requireNonNull(rewardPools, "rewardPools");
        Objects.requireNonNull(customTables, "customTables");
        Objects.requireNonNull(ruleGroups, "ruleGroups");
        Objects.requireNonNull(quantityRules, "quantityRules");
        for (GachaExecutionPlanDefinition plan : plans.values()) {
            validatePlan(plan, plans, rewardPools, customTables, ruleGroups, quantityRules,
                    new ArrayDeque<>(), new StepBudget());
        }
    }

    private static void validatePlan(GachaExecutionPlanDefinition plan,
                                     Map<ResourceLocation, GachaExecutionPlanDefinition> plans,
                                     Map<ResourceLocation, IGachaRewardPool> rewardPools,
                                     Map<ResourceLocation, GachaCustomLootTable> customTables,
                                     Set<ResourceLocation> ruleGroups,
                                     Set<ResourceLocation> quantityRules,
                                     Deque<ResourceLocation> path, StepBudget budget) {
        if (path.contains(plan.id())) {
            throw new IllegalArgumentException("Execution plan reference cycle: " + cycle(path, plan.id()));
        }
        if (path.size() >= MAXIMUM_NESTING_DEPTH) {
            throw new IllegalArgumentException("Execution plan nesting depth exceeds limit: " + plan.id());
        }
        path.addLast(plan.id());
        for (IGachaExecutionStep step : plan.steps()) {
            budget.consume(plan.id());
            if (step instanceof GachaRewardPoolExecutionStep rewardPoolStep) {
                IGachaRewardPool rewardPool = rewardPools.get(rewardPoolStep.target());
                if (rewardPool == null) {
                    throw missing(plan.id(), rewardPoolStep.type(), rewardPoolStep.target());
                }
                rewardPoolStep.parameters().validate(plan.parameterSchema(), rewardPool.parameterSchema());
                continue;
            }
            if (step instanceof GachaCustomTableExecutionStep customTableStep) {
                if (!customTables.containsKey(customTableStep.target())) {
                    throw missing(plan.id(), customTableStep.type(), customTableStep.target());
                }
                continue;
            }
            if (step instanceof GachaVanillaLootTableExecutionStep) {
                continue;
            }
            if (!(step instanceof GachaComponentReferenceStep reference)) {
                continue;
            }
            ResourceLocation type = reference.type();
            ResourceLocation target = reference.target();
            if (type.equals(GachaBuiltinExecutionSteps.CUSTOM_TABLE) && !customTables.containsKey(target)) {
                throw missing(plan.id(), type, target);
            }
            if (type.equals(GachaBuiltinExecutionSteps.RULE_GROUP) && !ruleGroups.contains(target)) {
                throw missing(plan.id(), type, target);
            }
            if (type.equals(GachaBuiltinExecutionSteps.QUANTITY_RULE) && !quantityRules.contains(target)) {
                throw missing(plan.id(), type, target);
            }
            if (type.equals(GachaBuiltinExecutionSteps.EXECUTION_PLAN)) {
                GachaExecutionPlanDefinition nested = plans.get(target);
                if (nested == null) {
                    throw missing(plan.id(), type, target);
                }
                reference.parameters().validate(plan.parameterSchema(), nested.parameterSchema());
                validatePlan(nested, plans, rewardPools, customTables, ruleGroups, quantityRules, path, budget);
            }
        }
        path.removeLast();
    }

    private static IllegalArgumentException missing(ResourceLocation planId, ResourceLocation type,
                                                    ResourceLocation target) {
        return new IllegalArgumentException("Unknown execution component '" + target + "' for step type '"
                + type + "' in execution plan " + planId);
    }

    private static String cycle(Deque<ResourceLocation> path, ResourceLocation repeated) {
        StringBuilder value = new StringBuilder();
        boolean include = false;
        for (ResourceLocation id : path) {
            if (id.equals(repeated)) {
                include = true;
            }
            if (include) {
                if (!value.isEmpty()) {
                    value.append(" -> ");
                }
                value.append(id);
            }
        }
        return value.append(" -> ").append(repeated).toString();
    }

    private static final class StepBudget {
        private int consumed;

        private void consume(ResourceLocation planId) {
            consumed = Math.incrementExact(consumed);
            if (consumed > MAXIMUM_EXPANDED_STEPS) {
                throw new IllegalArgumentException("Expanded execution plan exceeds step limit: " + planId);
            }
        }
    }
}

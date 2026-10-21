package xiaojin.gachaaddiction.data;

import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.cardpool.GachaCardPoolDefinition;
import xiaojin.gachaaddiction.cardpool.GachaCardPoolDefinitionResolver;
import xiaojin.gachaaddiction.execution.GachaExecutionComponentResolver;
import xiaojin.gachaaddiction.execution.GachaExecutionPlanDefinition;
import xiaojin.gachaaddiction.execution.GachaExecutionPlanResolver;
import xiaojin.gachaaddiction.reward.GachaRewardPool;
import xiaojin.gachaaddiction.reward.GachaRewardPoolResolver;
import xiaojin.gachaaddiction.reward.custom.GachaCustomLootTable;
import xiaojin.gachaaddiction.reward.custom.GachaCustomTableResolver;
import xiaojin.gachaaddiction.reward.probability.GachaProbabilityDescription;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public record GachaCompiledDefinitionSnapshot(Map<ResourceLocation, GachaCustomLootTable> customTables,
                                              Map<ResourceLocation, GachaRewardPool> rewardPools,
                                              Map<ResourceLocation, GachaCardPoolDefinition> cardPools,
                                              Map<ResourceLocation, GachaExecutionPlanDefinition> executionPlans,
                                              Map<ResourceLocation, GachaRewardPoolRequestPreset> requestPresets) implements GachaCustomTableResolver,
        GachaRewardPoolResolver, GachaCardPoolDefinitionResolver, GachaExecutionPlanResolver,
        GachaExecutionComponentResolver {
    public static final GachaCompiledDefinitionSnapshot EMPTY = new GachaCompiledDefinitionSnapshot(Map.of(),
            Map.of(), Map.of(), Map.of(), Map.of());

    public GachaCompiledDefinitionSnapshot(Map<ResourceLocation, GachaCustomLootTable> customTables,
                                           Map<ResourceLocation, GachaRewardPool> rewardPools,
                                           Map<ResourceLocation, GachaCardPoolDefinition> cardPools,
                                           Map<ResourceLocation, GachaExecutionPlanDefinition> executionPlans,
                                           Map<ResourceLocation, GachaRewardPoolRequestPreset> requestPresets) {
        this.customTables = immutableCopy(customTables);
        this.rewardPools = immutableCopy(rewardPools);
        this.cardPools = immutableCopy(cardPools);
        this.executionPlans = immutableCopy(executionPlans);
        this.requestPresets = immutableCopy(requestPresets);
    }

    private static <T> Map<ResourceLocation, T> immutableCopy(Map<ResourceLocation, T> values) {
        return Collections.unmodifiableMap(new LinkedHashMap<>(values));
    }

    @Override
    public Optional<GachaCustomLootTable> resolveTable(ResourceLocation id) {
        return Optional.ofNullable(customTables.get(id));
    }

    @Override
    public Optional<GachaRewardPool> resolveRewardPool(ResourceLocation id) {
        return Optional.ofNullable(rewardPools.get(id));
    }

    @Override
    public Optional<GachaProbabilityDescription> describeProbability(ResourceLocation id) {
        return GachaExecutionComponentResolver.super.describeProbability(id);
    }

    @Override
    public Optional<GachaCardPoolDefinition> resolveCardPool(ResourceLocation id) {
        return Optional.ofNullable(cardPools.get(id));
    }

    @Override
    public Optional<GachaExecutionPlanDefinition> resolveExecutionPlan(ResourceLocation id) {
        return Optional.ofNullable(executionPlans.get(id));
    }

    public Optional<GachaRewardPoolRequestPreset> resolveRequestPreset(ResourceLocation id) {
        return Optional.ofNullable(requestPresets.get(id));
    }
}

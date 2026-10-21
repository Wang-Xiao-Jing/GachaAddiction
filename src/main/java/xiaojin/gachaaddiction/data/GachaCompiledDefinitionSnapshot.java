package xiaojin.gachaaddiction.data;

import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.cardpool.GachaCardPoolDefinition;
import xiaojin.gachaaddiction.cardpool.IGachaCardPoolDefinitionResolver;
import xiaojin.gachaaddiction.execution.IGachaExecutionComponentResolver;
import xiaojin.gachaaddiction.execution.GachaExecutionPlanDefinition;
import xiaojin.gachaaddiction.execution.IGachaExecutionPlanResolver;
import xiaojin.gachaaddiction.reward.IGachaRewardPool;
import xiaojin.gachaaddiction.reward.IGachaRewardPoolResolver;
import xiaojin.gachaaddiction.reward.custom.GachaCustomLootTable;
import xiaojin.gachaaddiction.reward.custom.IGachaCustomTableResolver;
import xiaojin.gachaaddiction.reward.probability.GachaProbabilityDescription;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public record GachaCompiledDefinitionSnapshot(Map<ResourceLocation, GachaCustomLootTable> customTables,
                                              Map<ResourceLocation, IGachaRewardPool> rewardPools,
                                              Map<ResourceLocation, GachaCardPoolDefinition> cardPools,
                                              Map<ResourceLocation, GachaExecutionPlanDefinition> executionPlans,
                                              Map<ResourceLocation, GachaRewardPoolRequestPreset> requestPresets) implements IGachaCustomTableResolver,
        IGachaRewardPoolResolver, IGachaCardPoolDefinitionResolver, IGachaExecutionPlanResolver,
        IGachaExecutionComponentResolver {
    public static final GachaCompiledDefinitionSnapshot EMPTY = new GachaCompiledDefinitionSnapshot(Map.of(),
            Map.of(), Map.of(), Map.of(), Map.of());

    public GachaCompiledDefinitionSnapshot(Map<ResourceLocation, GachaCustomLootTable> customTables,
                                           Map<ResourceLocation, IGachaRewardPool> rewardPools,
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
    public Optional<IGachaRewardPool> resolveRewardPool(ResourceLocation id) {
        return Optional.ofNullable(rewardPools.get(id));
    }

    @Override
    public Optional<GachaProbabilityDescription> describeProbability(ResourceLocation id) {
        return IGachaExecutionComponentResolver.super.describeProbability(id);
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

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

import java.util.Objects;
import java.util.Optional;

public final class GachaDefinitionRepository implements GachaCustomTableResolver,
        GachaRewardPoolResolver, GachaCardPoolDefinitionResolver, GachaExecutionPlanResolver,
        GachaExecutionComponentResolver {
    public static final GachaDefinitionRepository INSTANCE = new GachaDefinitionRepository();

    private volatile State current = new State(GachaRawDefinitionSnapshot.EMPTY,
            GachaCompiledDefinitionSnapshot.EMPTY);

    private GachaDefinitionRepository() {
    }

    public void publish(GachaRawDefinitionSnapshot raw, GachaCompiledDefinitionSnapshot compiled) {
        current = new State(Objects.requireNonNull(raw, "raw"), Objects.requireNonNull(compiled, "compiled"));
    }

    public GachaRawDefinitionSnapshot raw() {
        return current.raw();
    }

    public GachaCompiledDefinitionSnapshot compiled() {
        return current.compiled();
    }

    @Override
    public Optional<GachaCustomLootTable> resolveTable(ResourceLocation id) {
        return current.compiled().resolveTable(id);
    }

    @Override
    public Optional<GachaRewardPool> resolveRewardPool(ResourceLocation id) {
        return current.compiled().resolveRewardPool(id);
    }

    @Override
    public Optional<GachaProbabilityDescription> describeProbability(ResourceLocation id) {
        return GachaExecutionComponentResolver.super.describeProbability(id);
    }

    @Override
    public Optional<GachaCardPoolDefinition> resolveCardPool(ResourceLocation id) {
        return current.compiled().resolveCardPool(id);
    }

    @Override
    public Optional<GachaExecutionPlanDefinition> resolveExecutionPlan(ResourceLocation id) {
        return current.compiled().resolveExecutionPlan(id);
    }

    public Optional<GachaRewardPoolRequestPreset> resolveRequestPreset(ResourceLocation id) {
        return current.compiled().resolveRequestPreset(id);
    }

    private record State(GachaRawDefinitionSnapshot raw, GachaCompiledDefinitionSnapshot compiled) {
    }
}

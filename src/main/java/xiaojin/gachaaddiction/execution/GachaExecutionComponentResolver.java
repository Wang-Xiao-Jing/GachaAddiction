package xiaojin.gachaaddiction.execution;

import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.reward.GachaRewardPoolResolver;
import xiaojin.gachaaddiction.reward.custom.GachaCustomTableResolver;
import xiaojin.gachaaddiction.reward.probability.GachaProbabilityDescription;

import java.util.Optional;

public interface GachaExecutionComponentResolver extends GachaExecutionPlanResolver, GachaRewardPoolResolver,
        GachaCustomTableResolver {
    @Override
    default Optional<GachaProbabilityDescription> describeProbability(ResourceLocation id) {
        return GachaRewardPoolResolver.super.describeProbability(id)
                .or(() -> GachaCustomTableResolver.super.describeProbability(id));
    }
}

package xiaojin.gachaaddiction.execution;

import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.reward.IGachaRewardPoolResolver;
import xiaojin.gachaaddiction.reward.custom.IGachaCustomTableResolver;
import xiaojin.gachaaddiction.reward.probability.GachaProbabilityDescription;

import java.util.Optional;

public interface IGachaExecutionComponentResolver extends IGachaExecutionPlanResolver, IGachaRewardPoolResolver,
        IGachaCustomTableResolver {
    @Override
    default Optional<GachaProbabilityDescription> describeProbability(ResourceLocation id) {
        return IGachaRewardPoolResolver.super.describeProbability(id)
                .or(() -> IGachaCustomTableResolver.super.describeProbability(id));
    }
}

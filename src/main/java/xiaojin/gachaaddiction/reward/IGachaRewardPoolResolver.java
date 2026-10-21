package xiaojin.gachaaddiction.reward;

import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.reward.probability.IGachaProbabilityDescribable;
import xiaojin.gachaaddiction.reward.probability.GachaProbabilityDescription;

import java.util.Optional;

@FunctionalInterface
public interface IGachaRewardPoolResolver {
    Optional<IGachaRewardPool> resolveRewardPool(ResourceLocation id);

    default Optional<GachaProbabilityDescription> describeProbability(ResourceLocation id) {
        return resolveRewardPool(id)
                .filter(IGachaProbabilityDescribable.class::isInstance)
                .map(IGachaProbabilityDescribable.class::cast)
                .map(IGachaProbabilityDescribable::describeProbability);
    }
}

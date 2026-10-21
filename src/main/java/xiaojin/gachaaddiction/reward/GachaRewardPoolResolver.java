package xiaojin.gachaaddiction.reward;

import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.reward.probability.GachaProbabilityDescribable;
import xiaojin.gachaaddiction.reward.probability.GachaProbabilityDescription;

import java.util.Optional;

@FunctionalInterface
public interface GachaRewardPoolResolver {
    Optional<GachaRewardPool> resolveRewardPool(ResourceLocation id);

    default Optional<GachaProbabilityDescription> describeProbability(ResourceLocation id) {
        return resolveRewardPool(id)
                .filter(GachaProbabilityDescribable.class::isInstance)
                .map(GachaProbabilityDescribable.class::cast)
                .map(GachaProbabilityDescribable::describeProbability);
    }
}

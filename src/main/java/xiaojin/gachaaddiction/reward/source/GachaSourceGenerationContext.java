package xiaojin.gachaaddiction.reward.source;

import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.reward.GachaRewardPoolRequest;
import xiaojin.gachaaddiction.reward.model.GachaRewardFactory;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterSet;

import java.util.Objects;

public record GachaSourceGenerationContext(GachaRewardPoolRequest request, int roundIndex,
                                           ResourceLocation sourceId,
                                           GachaParameterSet currentParameters,
                                           GachaRewardFactory rewardFactory,
                                           GachaGenerationBudget budget) {
    public GachaSourceGenerationContext {
        Objects.requireNonNull(request, "request");
        if (roundIndex < 0) {
            throw new IllegalArgumentException("Round index cannot be negative");
        }
        Objects.requireNonNull(sourceId, "sourceId");
        Objects.requireNonNull(currentParameters, "currentParameters");
        Objects.requireNonNull(rewardFactory, "rewardFactory");
        Objects.requireNonNull(budget, "budget");
    }
}

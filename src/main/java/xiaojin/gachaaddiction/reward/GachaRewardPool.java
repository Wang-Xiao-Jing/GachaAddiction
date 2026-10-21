package xiaojin.gachaaddiction.reward;

import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.reward.model.GachaLogicalReward;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterSchema;
import xiaojin.gachaaddiction.reward.provenance.GachaRewardOrigin;
import xiaojin.gachaaddiction.reward.provenance.GachaRewardOriginTypes;
import xiaojin.gachaaddiction.reward.provenance.GachaRewardProvenance;
import xiaojin.gachaaddiction.reward.quantity.GachaExecutionLimits;

import java.util.Map;
import java.util.Optional;

public interface GachaRewardPool {
    ResourceLocation id();

    GachaParameterSchema parameterSchema();

    GachaRewardPoolResult draw(GachaRewardPoolRequest request);

    default Optional<GachaExecutionLimits> executionLimits() {
        return Optional.empty();
    }

    default GachaLogicalRewardPoolResult drawLogical(GachaRewardPoolRequest request) {
        GachaRewardPoolResult result = draw(request);
        GachaRewardOrigin origin = new GachaRewardOrigin(GachaRewardOriginTypes.SOURCE, id());
        return new GachaLogicalRewardPoolResult(
                result.rewards().stream()
                        .map(stack -> new GachaLogicalReward(stack, stack.getCount(),
                                GachaRewardProvenance.generated(origin), Map.of()))
                        .toList(),
                result.parameterChanges(), result.problems(), Optional.empty());
    }
}

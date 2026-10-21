package xiaojin.gachaaddiction.data;

import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterSet;

import java.util.Objects;

public record GachaRewardPoolRequestPreset(ResourceLocation id, ResourceLocation rewardPool, int rounds,
                                           GachaParameterSet parameters) {
    public GachaRewardPoolRequestPreset {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(rewardPool, "rewardPool");
        if (rounds <= 0) {
            throw new IllegalArgumentException("Request preset rounds must be positive");
        }
        Objects.requireNonNull(parameters, "parameters");
    }
}

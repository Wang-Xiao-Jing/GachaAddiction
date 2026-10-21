package xiaojin.gachaaddiction.reward.rule;

import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.reward.GachaRewardPoolRequest;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

public record GachaRuleContext(GachaRewardPoolRequest request, OptionalInt roundIndex,
                               Optional<ResourceLocation> sourceId, OptionalInt rewardIndex) {
    public GachaRuleContext {
        Objects.requireNonNull(request, "request");
        roundIndex = Objects.requireNonNull(roundIndex, "roundIndex");
        if (roundIndex.isPresent() && roundIndex.getAsInt() < 0) {
            throw new IllegalArgumentException("Round index cannot be negative");
        }
        sourceId = Objects.requireNonNull(sourceId, "sourceId");
        rewardIndex = Objects.requireNonNull(rewardIndex, "rewardIndex");
        if (rewardIndex.isPresent() && rewardIndex.getAsInt() < 0) {
            throw new IllegalArgumentException("Reward index cannot be negative");
        }
    }

    public GachaRuleContext withRewardIndex(int rewardIndex) {
        return new GachaRuleContext(request, roundIndex, sourceId, OptionalInt.of(rewardIndex));
    }
}

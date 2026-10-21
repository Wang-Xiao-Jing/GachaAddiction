package xiaojin.gachaaddiction.reward.rule;

import net.minecraft.world.item.Item;
import xiaojin.gachaaddiction.reward.model.GachaLogicalReward;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterSet;

import java.util.List;
import java.util.Objects;

public record GachaRewardItemCondition(Item item) implements IGachaRuleCondition {
    public GachaRewardItemCondition {
        Objects.requireNonNull(item, "item");
    }

    @Override
    public boolean test(GachaRuleContext context, List<GachaLogicalReward> results,
                        GachaParameterSet parameters) {
        int rewardIndex = context.rewardIndex().orElseThrow(() ->
                new IllegalArgumentException("Reward item condition requires EACH_RESULT scope"));
        return results.get(rewardIndex).template().is(item);
    }
}

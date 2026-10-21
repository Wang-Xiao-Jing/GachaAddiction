package xiaojin.gachaaddiction.reward.rule;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import xiaojin.gachaaddiction.reward.model.GachaLogicalReward;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterSet;

import java.util.List;
import java.util.Objects;

public record GachaRewardItemTagCondition(TagKey<Item> tag) implements IGachaRuleCondition {
    public GachaRewardItemTagCondition {
        Objects.requireNonNull(tag, "tag");
    }

    @Override
    public boolean test(GachaRuleContext context, List<GachaLogicalReward> results,
                        GachaParameterSet parameters) {
        int rewardIndex = context.rewardIndex().orElseThrow(() ->
                new IllegalArgumentException("Reward item tag condition requires EACH_RESULT scope"));
        return results.get(rewardIndex).template().is(tag);
    }
}

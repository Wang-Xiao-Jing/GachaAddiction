package xiaojin.gachaaddiction.reward.rule;

import xiaojin.gachaaddiction.reward.model.GachaLogicalReward;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterSet;
import xiaojin.gachaaddiction.reward.provenance.GachaProvenanceSelector;
import xiaojin.gachaaddiction.reward.provenance.GachaRewardOrigin;

import java.util.List;
import java.util.Objects;

public record GachaRewardProvenanceCondition(GachaProvenanceSelector selector,
                                             GachaRewardOrigin origin) implements IGachaRuleCondition {
    public GachaRewardProvenanceCondition {
        Objects.requireNonNull(selector, "selector");
        Objects.requireNonNull(origin, "origin");
    }

    @Override
    public boolean test(GachaRuleContext context, List<GachaLogicalReward> results,
                        GachaParameterSet parameters) {
        int rewardIndex = context.rewardIndex().orElseThrow(() ->
                new IllegalArgumentException("Provenance condition requires EACH_RESULT scope"));
        return results.get(rewardIndex).provenance().matches(selector, origin);
    }
}

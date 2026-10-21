package xiaojin.gachaaddiction.reward.rule;

import xiaojin.gachaaddiction.reward.model.GachaResultEditor;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterEditor;

public final class GachaRemoveRewardAction implements GachaRuleAction {
    @Override
    public void apply(GachaRuleContext context, GachaResultEditor results, GachaParameterEditor parameters) {
        int rewardIndex = context.rewardIndex().orElseThrow(() ->
                new IllegalArgumentException("Remove reward action requires EACH_RESULT scope"));
        results.remove(rewardIndex);
    }
}

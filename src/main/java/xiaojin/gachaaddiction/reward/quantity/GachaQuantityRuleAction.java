package xiaojin.gachaaddiction.reward.quantity;

import xiaojin.gachaaddiction.reward.model.GachaLogicalReward;
import xiaojin.gachaaddiction.reward.model.GachaResultEditor;
import xiaojin.gachaaddiction.reward.number.GachaNumberContext;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterEditor;
import xiaojin.gachaaddiction.reward.rule.IGachaRuleAction;
import xiaojin.gachaaddiction.reward.rule.GachaRuleContext;

import java.util.Objects;

public record GachaQuantityRuleAction(GachaQuantityPipeline pipeline) implements IGachaRuleAction {
    public GachaQuantityRuleAction {
        Objects.requireNonNull(pipeline, "pipeline");
    }

    @Override
    public void apply(GachaRuleContext context, GachaResultEditor results, GachaParameterEditor parameters) {
        int rewardIndex = context.rewardIndex().orElseThrow(() ->
                new IllegalArgumentException("Quantity rule action requires EACH_RESULT scope"));
        GachaLogicalReward reward = results.get(rewardIndex);
        GachaNumberContext numbers = new GachaNumberContext(context.request().parameters(),
                parameters.currentValues(), context.request().random());
        long quantity = pipeline.apply(reward.quantity(), new GachaQuantityContext(numbers));
        if (quantity == 0) {
            results.remove(rewardIndex);
        } else {
            results.replace(rewardIndex, reward.withQuantity(quantity));
        }
    }
}

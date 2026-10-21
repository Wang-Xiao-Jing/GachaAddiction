package xiaojin.gachaaddiction.reward.rule;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import xiaojin.gachaaddiction.reward.model.GachaLogicalReward;
import xiaojin.gachaaddiction.reward.model.GachaResultEditor;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterEditor;
import xiaojin.gachaaddiction.reward.provenance.GachaRewardOrigin;
import xiaojin.gachaaddiction.reward.provenance.GachaRewardOriginTypes;

import java.util.Objects;

public record GachaVanillaLootFunctionAction(ResourceLocation id,
                                             LootItemFunction function) implements IGachaRuleAction {
    public GachaVanillaLootFunctionAction {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(function, "function");
    }

    @Override
    public void apply(GachaRuleContext context, GachaResultEditor results, GachaParameterEditor parameters) {
        int rewardIndex = context.rewardIndex().orElseThrow(() ->
                new IllegalArgumentException("Vanilla loot function requires EACH_RESULT scope"));
        GachaLogicalReward reward = results.get(rewardIndex);
        ItemStack input = reward.template().copyWithCount(Math.toIntExact(reward.quantity()));
        ItemStack output = function.apply(input, context.request().createLootContext());
        if (output == null || output.isEmpty()) {
            results.remove(rewardIndex);
            return;
        }
        GachaRewardOrigin transformation = new GachaRewardOrigin(GachaRewardOriginTypes.RULE_ACTION, id);
        results.replace(rewardIndex, new GachaLogicalReward(output, output.getCount(),
                reward.provenance().transformedBy(transformation), reward.metadata()));
    }
}

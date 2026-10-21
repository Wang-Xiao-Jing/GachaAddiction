package xiaojin.gachaaddiction.reward.rule;

import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import xiaojin.gachaaddiction.reward.model.GachaLogicalReward;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterSet;

import java.util.List;
import java.util.Objects;

public record GachaVanillaLootCondition(LootItemCondition condition) implements IGachaRuleCondition {
    public GachaVanillaLootCondition {
        Objects.requireNonNull(condition, "condition");
    }

    @Override
    public boolean test(GachaRuleContext context, List<GachaLogicalReward> results,
                        GachaParameterSet parameters) {
        return condition.test(context.request().createLootContext());
    }
}

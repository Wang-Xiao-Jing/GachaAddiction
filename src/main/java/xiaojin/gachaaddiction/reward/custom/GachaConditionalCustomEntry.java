package xiaojin.gachaaddiction.reward.custom;

import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.reward.model.GachaLogicalReward;
import xiaojin.gachaaddiction.reward.rule.GachaRuleCondition;
import xiaojin.gachaaddiction.reward.rule.GachaRuleContext;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

public record GachaConditionalCustomEntry(GachaCustomEntry delegate,
                                          List<GachaRuleCondition> conditions) implements GachaCustomEntry {
    public GachaConditionalCustomEntry {
        Objects.requireNonNull(delegate, "delegate");
        conditions = List.copyOf(conditions);
    }

    @Override
    public ResourceLocation id() {
        return delegate.id();
    }

    @Override
    public boolean isEligible(GachaCustomEntryContext context) {
        GachaRuleContext ruleContext = new GachaRuleContext(context.sourceContext().request(),
                OptionalInt.of(context.sourceContext().roundIndex()),
                Optional.of(context.sourceContext().sourceId()), OptionalInt.empty());
        return conditions.stream().allMatch(condition -> condition.test(ruleContext, List.of(),
                context.sourceContext().currentParameters()));
    }

    @Override
    public List<GachaLogicalReward> generate(GachaCustomEntryContext context) {
        return isEligible(context) ? generateSelected(context) : List.of();
    }

    @Override
    public List<GachaLogicalReward> generateSelected(GachaCustomEntryContext context) {
        return delegate.generate(context);
    }
}

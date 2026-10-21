package xiaojin.gachaaddiction.reward.rule;

import xiaojin.gachaaddiction.reward.model.GachaLogicalReward;
import xiaojin.gachaaddiction.reward.number.GachaDoubleProvider;
import xiaojin.gachaaddiction.reward.number.GachaNumberContext;
import xiaojin.gachaaddiction.reward.number.GachaNumberExpression;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterSet;

import java.util.List;
import java.util.Objects;

public record GachaChanceCondition(GachaDoubleProvider chance) implements GachaProbabilityCondition {
    public GachaChanceCondition {
        Objects.requireNonNull(chance, "chance");
    }

    @Override
    public boolean test(GachaRuleContext context, List<GachaLogicalReward> results,
                        GachaParameterSet parameters) {
        double probability = chance.get(new GachaNumberContext(context.request().parameters(), parameters,
                context.request().random()));
        if (!Double.isFinite(probability) || probability < 0 || probability > 1) {
            throw new IllegalArgumentException("Rule chance must be between zero and one");
        }
        return context.request().random().nextDouble() < probability;
    }

    @Override
    public GachaNumberExpression probabilityExpression() {
        return chance.expression();
    }
}

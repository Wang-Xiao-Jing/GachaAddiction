package xiaojin.gachaaddiction.reward.rule;

import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Objects;

public record GachaRuleDefinition(ResourceLocation id, GachaRulePhase phase, GachaRuleScope scope,
                                  List<GachaRuleCondition> conditions, List<GachaRuleAction> actions,
                                  GachaRuleFailurePolicy failurePolicy, List<GachaRuleAction> fallbackActions) {
    public GachaRuleDefinition {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(phase, "phase");
        Objects.requireNonNull(scope, "scope");
        conditions = List.copyOf(conditions);
        actions = List.copyOf(actions);
        Objects.requireNonNull(failurePolicy, "failurePolicy");
        fallbackActions = List.copyOf(fallbackActions);
        if (actions.isEmpty()) {
            throw new IllegalArgumentException("Rule must contain at least one action: " + id);
        }
        if (failurePolicy == GachaRuleFailurePolicy.FALLBACK && fallbackActions.isEmpty()) {
            throw new IllegalArgumentException("Fallback rule must contain fallback actions: " + id);
        }
        if (failurePolicy != GachaRuleFailurePolicy.FALLBACK && !fallbackActions.isEmpty()) {
            throw new IllegalArgumentException("Only fallback rules can contain fallback actions: " + id);
        }
    }
}

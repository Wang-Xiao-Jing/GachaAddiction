package xiaojin.gachaaddiction.reward.rule;

import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public final class GachaRuleExecutionException extends RuntimeException {
    private final ResourceLocation ruleId;
    private final GachaRuleFailurePolicy failurePolicy;

    public GachaRuleExecutionException(ResourceLocation ruleId, GachaRuleFailurePolicy failurePolicy,
                                       Throwable cause) {
        super("Rule execution failed: " + ruleId, cause);
        this.ruleId = Objects.requireNonNull(ruleId, "ruleId");
        this.failurePolicy = Objects.requireNonNull(failurePolicy, "failurePolicy");
    }

    public ResourceLocation ruleId() {
        return ruleId;
    }

    public GachaRuleFailurePolicy failurePolicy() {
        return failurePolicy;
    }
}

package xiaojin.gachaaddiction.execution;

import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.reward.rule.GachaRuleDefinition;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public record GachaRuleGroupExecutionStep(ResourceLocation type, ResourceLocation target,
                                          List<GachaRuleDefinition> rules,
                                          Set<GachaExecutionChannel> reads,
                                          Set<GachaExecutionChannel> writes) implements GachaExecutionStep {
    public GachaRuleGroupExecutionStep {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(target, "target");
        rules = List.copyOf(Objects.requireNonNull(rules, "rules"));
        reads = Set.copyOf(reads);
        writes = Set.copyOf(writes);
    }
}

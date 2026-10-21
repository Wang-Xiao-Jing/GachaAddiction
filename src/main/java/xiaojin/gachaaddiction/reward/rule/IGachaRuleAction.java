package xiaojin.gachaaddiction.reward.rule;

import xiaojin.gachaaddiction.reward.model.GachaResultEditor;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterEditor;

@FunctionalInterface
public interface IGachaRuleAction {
    void apply(GachaRuleContext context, GachaResultEditor results, GachaParameterEditor parameters);
}

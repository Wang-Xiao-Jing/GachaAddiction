package xiaojin.gachaaddiction.reward.rule;

import xiaojin.gachaaddiction.reward.model.GachaResultEditor;
import xiaojin.gachaaddiction.reward.number.IGachaLongProvider;
import xiaojin.gachaaddiction.reward.number.GachaNumberContext;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterDefinition;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterEditor;

import java.util.Objects;

public record GachaSetLongParameterAction(GachaParameterDefinition<Long> parameter,
                                          IGachaLongProvider value) implements IGachaRuleAction {
    public GachaSetLongParameterAction {
        Objects.requireNonNull(parameter, "parameter");
        Objects.requireNonNull(value, "value");
    }

    @Override
    public void apply(GachaRuleContext context, GachaResultEditor results, GachaParameterEditor parameters) {
        GachaNumberContext numbers = new GachaNumberContext(context.request().parameters(),
                parameters.currentValues(), context.request().random());
        parameters.set(parameter, value.get(numbers));
    }
}

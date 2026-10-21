package xiaojin.gachaaddiction.reward.number;

import xiaojin.gachaaddiction.reward.parameter.GachaParameterKey;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterSet;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterView;

import java.util.Objects;
import java.util.Optional;

public record GachaNamedLongProvider(GachaParameterKey parameter,
                                     GachaParameterView view) implements GachaLongProvider {
    public GachaNamedLongProvider {
        Objects.requireNonNull(parameter, "parameter");
        Objects.requireNonNull(view, "view");
    }

    @Override
    public long get(GachaNumberContext context) {
        GachaParameterSet parameters = view == GachaParameterView.INITIAL
                ? context.initialParameters() : context.currentParameters();
        Object value = parameters.get(parameter).orElseThrow(() ->
                new IllegalArgumentException("Missing numerical parameter: " + parameter));
        if (!(value instanceof Number number)) {
            throw new IllegalArgumentException("Parameter is not numerical: " + parameter);
        }
        return number.longValue();
    }

    @Override
    public GachaNumberExpression expression() {
        return new GachaNumberExpression.Parameter(parameter, view, Optional.empty());
    }
}

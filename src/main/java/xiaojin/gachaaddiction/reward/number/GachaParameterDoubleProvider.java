package xiaojin.gachaaddiction.reward.number;

import xiaojin.gachaaddiction.reward.parameter.GachaParameterDefinition;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterSet;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterView;

import java.util.Objects;

public record GachaParameterDoubleProvider(GachaParameterDefinition<? extends Number> parameter,
                                           GachaParameterView view) implements IGachaDoubleProvider {
    public GachaParameterDoubleProvider {
        Objects.requireNonNull(parameter, "parameter");
        Objects.requireNonNull(view, "view");
    }

    @Override
    public double get(GachaNumberContext context) {
        GachaParameterSet parameters = view == GachaParameterView.INITIAL
                ? context.initialParameters() : context.currentParameters();
        Number value = parameters.get(parameter).map(Number.class::cast).orElseGet(() ->
                parameter.defaultValue().map(Number.class::cast).orElseThrow(() ->
                        new IllegalArgumentException("Missing numerical parameter: " + parameter.key())));
        double result = value.doubleValue();
        if (!Double.isFinite(result)) {
            throw new IllegalArgumentException("Numerical parameter must be finite: " + parameter.key());
        }
        return result;
    }

    @Override
    public IGachaNumberExpression expression() {
        return new IGachaNumberExpression.Parameter(parameter.key(), view,
                parameter.defaultValue().map(Number.class::cast));
    }
}

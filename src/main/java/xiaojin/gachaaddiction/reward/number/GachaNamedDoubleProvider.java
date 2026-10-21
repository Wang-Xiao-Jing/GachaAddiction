package xiaojin.gachaaddiction.reward.number;

import xiaojin.gachaaddiction.reward.parameter.GachaParameterKey;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterSet;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterView;

import java.util.Objects;
import java.util.Optional;

public record GachaNamedDoubleProvider(GachaParameterKey parameter,
                                       GachaParameterView view) implements IGachaDoubleProvider {
    public GachaNamedDoubleProvider {
        Objects.requireNonNull(parameter, "parameter");
        Objects.requireNonNull(view, "view");
    }

    @Override
    public double get(GachaNumberContext context) {
        GachaParameterSet parameters = view == GachaParameterView.INITIAL
                ? context.initialParameters() : context.currentParameters();
        Object value = parameters.get(parameter).orElseThrow(() ->
                new IllegalArgumentException("Missing numerical parameter: " + parameter));
        if (!(value instanceof Number number)) {
            throw new IllegalArgumentException("Parameter is not numerical: " + parameter);
        }
        double result = number.doubleValue();
        if (!Double.isFinite(result)) {
            throw new IllegalArgumentException("Numerical parameter must be finite: " + parameter);
        }
        return result;
    }

    @Override
    public IGachaNumberExpression expression() {
        return new IGachaNumberExpression.Parameter(parameter, view, Optional.empty());
    }
}

package xiaojin.gachaaddiction.reward.number;

import xiaojin.gachaaddiction.reward.parameter.GachaParameterKey;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterView;

import java.util.Objects;
import java.util.Optional;

public sealed interface IGachaNumberExpression permits IGachaNumberExpression.DoubleConstant,
        IGachaNumberExpression.LongConstant, IGachaNumberExpression.Parameter,
        IGachaNumberExpression.Binary, IGachaNumberExpression.Clamp, IGachaNumberExpression.Unavailable {
    enum Unavailable implements IGachaNumberExpression {
        INSTANCE
    }

    record DoubleConstant(double value) implements IGachaNumberExpression {
        public DoubleConstant {
            if (!Double.isFinite(value)) {
                throw new IllegalArgumentException("Expression constant must be finite");
            }
        }
    }

    record LongConstant(long value) implements IGachaNumberExpression {
    }

    record Parameter(GachaParameterKey key, GachaParameterView view,
                     Optional<Number> defaultValue) implements IGachaNumberExpression {
        public Parameter {
            Objects.requireNonNull(key, "key");
            Objects.requireNonNull(view, "view");
            Objects.requireNonNull(defaultValue, "defaultValue");
        }
    }

    record Binary(GachaDoubleOperation operation, IGachaNumberExpression left,
                  IGachaNumberExpression right) implements IGachaNumberExpression {
        public Binary {
            Objects.requireNonNull(operation, "operation");
            Objects.requireNonNull(left, "left");
            Objects.requireNonNull(right, "right");
        }
    }

    record Clamp(IGachaNumberExpression value, IGachaNumberExpression minimum,
                 IGachaNumberExpression maximum) implements IGachaNumberExpression {
        public Clamp {
            Objects.requireNonNull(value, "value");
            Objects.requireNonNull(minimum, "minimum");
            Objects.requireNonNull(maximum, "maximum");
        }
    }
}

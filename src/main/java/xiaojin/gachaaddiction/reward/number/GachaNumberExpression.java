package xiaojin.gachaaddiction.reward.number;

import xiaojin.gachaaddiction.reward.parameter.GachaParameterKey;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterView;

import java.util.Objects;
import java.util.Optional;

public sealed interface GachaNumberExpression permits GachaNumberExpression.DoubleConstant,
        GachaNumberExpression.LongConstant, GachaNumberExpression.Parameter,
        GachaNumberExpression.Binary, GachaNumberExpression.Clamp, GachaNumberExpression.Unavailable {
    enum Unavailable implements GachaNumberExpression {
        INSTANCE
    }

    record DoubleConstant(double value) implements GachaNumberExpression {
        public DoubleConstant {
            if (!Double.isFinite(value)) {
                throw new IllegalArgumentException("Expression constant must be finite");
            }
        }
    }

    record LongConstant(long value) implements GachaNumberExpression {
    }

    record Parameter(GachaParameterKey key, GachaParameterView view,
                     Optional<Number> defaultValue) implements GachaNumberExpression {
        public Parameter {
            Objects.requireNonNull(key, "key");
            Objects.requireNonNull(view, "view");
            Objects.requireNonNull(defaultValue, "defaultValue");
        }
    }

    record Binary(GachaDoubleOperation operation, GachaNumberExpression left,
                  GachaNumberExpression right) implements GachaNumberExpression {
        public Binary {
            Objects.requireNonNull(operation, "operation");
            Objects.requireNonNull(left, "left");
            Objects.requireNonNull(right, "right");
        }
    }

    record Clamp(GachaNumberExpression value, GachaNumberExpression minimum,
                 GachaNumberExpression maximum) implements GachaNumberExpression {
        public Clamp {
            Objects.requireNonNull(value, "value");
            Objects.requireNonNull(minimum, "minimum");
            Objects.requireNonNull(maximum, "maximum");
        }
    }
}

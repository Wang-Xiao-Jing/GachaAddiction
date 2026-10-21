package xiaojin.gachaaddiction.cardpool.parameter;

import java.math.BigDecimal;
import java.util.Objects;

public record GachaNumberRangeChoiceConstraint<T extends Number>(BigDecimal minimum,
                                                                 BigDecimal maximum,
                                                                 BigDecimal step)
        implements IGachaPlayerChoiceConstraint<T> {
    public GachaNumberRangeChoiceConstraint {
        Objects.requireNonNull(minimum, "minimum");
        Objects.requireNonNull(maximum, "maximum");
        Objects.requireNonNull(step, "step");
        if (minimum.compareTo(maximum) > 0) {
            throw new IllegalArgumentException("Player choice minimum cannot exceed maximum");
        }
        if (step.signum() <= 0) {
            throw new IllegalArgumentException("Player choice step must be positive");
        }
    }

    @Override
    public boolean allows(T value) {
        if (value == null) {
            return false;
        }
        BigDecimal decimal;
        try {
            decimal = new BigDecimal(value.toString());
        } catch (NumberFormatException failure) {
            return false;
        }
        return decimal.compareTo(minimum) >= 0 && decimal.compareTo(maximum) <= 0
                && decimal.subtract(minimum).remainder(step).signum() == 0;
    }
}

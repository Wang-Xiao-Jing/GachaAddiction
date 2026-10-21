package xiaojin.gachaaddiction.reward.quantity;

import xiaojin.gachaaddiction.reward.number.GachaDoubleProvider;
import xiaojin.gachaaddiction.reward.number.GachaLongProvider;

import java.util.Objects;

public final class GachaQuantityOperations {
    private GachaQuantityOperations() {
    }

    public record Keep(boolean stopAfter) implements GachaQuantityOperation {
        @Override
        public GachaQuantityOperationType type() {
            return GachaQuantityOperationType.KEEP;
        }

        @Override
        public long apply(long current, GachaQuantityContext context) {
            return current;
        }
    }

    public record Set(GachaLongProvider value, boolean stopAfter) implements GachaQuantityOperation {
        public Set {
            Objects.requireNonNull(value, "value");
        }

        @Override
        public GachaQuantityOperationType type() {
            return GachaQuantityOperationType.SET;
        }

        @Override
        public long apply(long current, GachaQuantityContext context) {
            return value.get(context.numbers());
        }
    }

    public record Add(GachaLongProvider value, boolean stopAfter) implements GachaQuantityOperation {
        public Add {
            Objects.requireNonNull(value, "value");
        }

        @Override
        public GachaQuantityOperationType type() {
            return GachaQuantityOperationType.ADD;
        }

        @Override
        public long apply(long current, GachaQuantityContext context) {
            return Math.addExact(current, value.get(context.numbers()));
        }
    }

    public record Multiply(GachaDoubleProvider factor, GachaQuantityRounding rounding,
                           boolean stopAfter) implements GachaQuantityOperation {
        public Multiply {
            Objects.requireNonNull(factor, "factor");
            Objects.requireNonNull(rounding, "rounding");
        }

        @Override
        public GachaQuantityOperationType type() {
            return GachaQuantityOperationType.MULTIPLY;
        }

        @Override
        public long apply(long current, GachaQuantityContext context) {
            double product = current * factor.get(context.numbers());
            if (!Double.isFinite(product) || product < 0 || product > Long.MAX_VALUE) {
                throw new ArithmeticException("Quantity multiplication is outside the supported range");
            }
            return switch (rounding) {
                case FLOOR -> (long) Math.floor(product);
                case CEILING -> (long) Math.ceil(product);
                case NEAREST -> Math.round(product);
            };
        }
    }

    public record Min(GachaLongProvider value, boolean stopAfter) implements GachaQuantityOperation {
        public Min {
            Objects.requireNonNull(value, "value");
        }

        @Override
        public GachaQuantityOperationType type() {
            return GachaQuantityOperationType.MIN;
        }

        @Override
        public long apply(long current, GachaQuantityContext context) {
            return Math.min(current, value.get(context.numbers()));
        }
    }

    public record Max(GachaLongProvider value, boolean stopAfter) implements GachaQuantityOperation {
        public Max {
            Objects.requireNonNull(value, "value");
        }

        @Override
        public GachaQuantityOperationType type() {
            return GachaQuantityOperationType.MAX;
        }

        @Override
        public long apply(long current, GachaQuantityContext context) {
            return Math.max(current, value.get(context.numbers()));
        }
    }

    public record Clamp(GachaLongProvider minimum, GachaLongProvider maximum,
                        boolean stopAfter) implements GachaQuantityOperation {
        public Clamp {
            Objects.requireNonNull(minimum, "minimum");
            Objects.requireNonNull(maximum, "maximum");
        }

        @Override
        public GachaQuantityOperationType type() {
            return GachaQuantityOperationType.CLAMP;
        }

        @Override
        public long apply(long current, GachaQuantityContext context) {
            long minimumValue = minimum.get(context.numbers());
            long maximumValue = maximum.get(context.numbers());
            if (minimumValue > maximumValue) {
                throw new IllegalArgumentException("Quantity clamp minimum cannot exceed maximum");
            }
            return Math.max(minimumValue, Math.min(current, maximumValue));
        }
    }

    public record Roll(GachaLongProvider minimum, GachaLongProvider maximum,
                       boolean stopAfter) implements GachaQuantityOperation {
        public Roll {
            Objects.requireNonNull(minimum, "minimum");
            Objects.requireNonNull(maximum, "maximum");
        }

        @Override
        public GachaQuantityOperationType type() {
            return GachaQuantityOperationType.ROLL;
        }

        @Override
        public long apply(long current, GachaQuantityContext context) {
            return GachaRandomLong.inclusive(context.numbers().random(), minimum.get(context.numbers()),
                    maximum.get(context.numbers()));
        }
    }
}

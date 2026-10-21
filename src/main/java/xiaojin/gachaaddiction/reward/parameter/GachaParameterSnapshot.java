package xiaojin.gachaaddiction.reward.parameter;

import java.util.Objects;

public record GachaParameterSnapshot(GachaParameterSet values, int operationCount) {
    public GachaParameterSnapshot {
        Objects.requireNonNull(values, "values");
        if (operationCount < 0) {
            throw new IllegalArgumentException("Operation count cannot be negative");
        }
    }
}

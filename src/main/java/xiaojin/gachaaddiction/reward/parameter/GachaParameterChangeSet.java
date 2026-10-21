package xiaojin.gachaaddiction.reward.parameter;

import java.util.List;
import java.util.Objects;

public record GachaParameterChangeSet(String inputFingerprint, List<GachaParameterOperation> operations,
                                      GachaParameterSet finalValues) {
    public GachaParameterChangeSet {
        Objects.requireNonNull(inputFingerprint, "inputFingerprint");
        operations = List.copyOf(operations);
        Objects.requireNonNull(finalValues, "finalValues");
    }
}

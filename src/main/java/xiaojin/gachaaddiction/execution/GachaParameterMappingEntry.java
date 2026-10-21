package xiaojin.gachaaddiction.execution;

import xiaojin.gachaaddiction.reward.parameter.GachaParameterKey;

import java.util.Objects;

public record GachaParameterMappingEntry(GachaParameterKey target, IGachaParameterValue value) {
    public GachaParameterMappingEntry {
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(value, "value");
    }
}

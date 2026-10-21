package xiaojin.gachaaddiction.cardpool.parameter;

import xiaojin.gachaaddiction.reward.parameter.GachaParameterSet;

import java.util.Objects;

public record GachaParameterResolution(GachaParameterSet parameters, String inputFingerprint) {
    public GachaParameterResolution {
        Objects.requireNonNull(parameters, "parameters");
        Objects.requireNonNull(inputFingerprint, "inputFingerprint");
    }
}

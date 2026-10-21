package xiaojin.gachaaddiction.reward.pity;

import xiaojin.gachaaddiction.reward.parameter.GachaParameterDefinition;

import java.util.Objects;
import java.util.Optional;

public record GachaPityOutputs(Optional<GachaParameterDefinition<Boolean>> naturalHit,
                               Optional<GachaParameterDefinition<Boolean>> pityTriggered,
                               Optional<GachaParameterDefinition<Boolean>> finalHit) {
    public GachaPityOutputs {
        naturalHit = Objects.requireNonNull(naturalHit, "naturalHit");
        pityTriggered = Objects.requireNonNull(pityTriggered, "pityTriggered");
        finalHit = Objects.requireNonNull(finalHit, "finalHit");
    }
}

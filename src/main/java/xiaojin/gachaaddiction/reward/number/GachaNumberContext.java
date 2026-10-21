package xiaojin.gachaaddiction.reward.number;

import net.minecraft.util.RandomSource;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterSet;

import java.util.Objects;

public record GachaNumberContext(GachaParameterSet initialParameters, GachaParameterSet currentParameters,
                                 RandomSource random) {
    public GachaNumberContext {
        Objects.requireNonNull(initialParameters, "initialParameters");
        Objects.requireNonNull(currentParameters, "currentParameters");
        Objects.requireNonNull(random, "random");
    }
}

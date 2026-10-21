package xiaojin.gachaaddiction.cardpool.parameter;

import xiaojin.gachaaddiction.reward.parameter.GachaParameterSchema;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterSet;

@FunctionalInterface
public interface GachaParameterFingerprint {
    String create(GachaParameterSchema schema, GachaParameterSet parameters);
}

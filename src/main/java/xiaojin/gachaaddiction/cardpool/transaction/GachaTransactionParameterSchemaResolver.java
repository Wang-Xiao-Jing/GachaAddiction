package xiaojin.gachaaddiction.cardpool.transaction;

import xiaojin.gachaaddiction.reward.parameter.GachaParameterSchema;

@FunctionalInterface
public interface GachaTransactionParameterSchemaResolver {
    GachaParameterSchema resolve(GachaTransactionSource source);
}

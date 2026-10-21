package xiaojin.gachaaddiction.cardpool.transaction;

import xiaojin.gachaaddiction.reward.parameter.GachaParameterSchema;

@FunctionalInterface
public interface IGachaTransactionParameterSchemaResolver {
    GachaParameterSchema resolve(GachaTransactionSource source);
}

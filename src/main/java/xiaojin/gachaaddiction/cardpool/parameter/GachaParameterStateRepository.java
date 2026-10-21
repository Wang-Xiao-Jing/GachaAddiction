package xiaojin.gachaaddiction.cardpool.parameter;

import xiaojin.gachaaddiction.cardpool.transaction.GachaTransactionContext;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterChangeSet;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterSchema;

import java.util.List;

public interface GachaParameterStateRepository extends GachaParameterStateReader {
    void prepare(GachaTransactionContext context, GachaParameterSchema schema,
                 List<GachaParameterBinding<?>> bindings,
                 GachaParameterChangeSet changes);

    void commitPrepared(GachaTransactionContext context);

    void rollbackPrepared(GachaTransactionContext context);

    void recover(GachaTransactionContext context, GachaParameterSchema schema,
                 List<GachaParameterBinding<?>> bindings,
                 GachaParameterChangeSet changes);

    boolean wasApplied(GachaTransactionContext context);
}

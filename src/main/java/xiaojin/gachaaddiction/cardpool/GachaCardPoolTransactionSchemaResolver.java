package xiaojin.gachaaddiction.cardpool;

import xiaojin.gachaaddiction.cardpool.transaction.GachaTransactionParameterSchemaResolver;
import xiaojin.gachaaddiction.cardpool.transaction.GachaTransactionSource;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterSchema;

import java.util.Objects;

public final class GachaCardPoolTransactionSchemaResolver implements GachaTransactionParameterSchemaResolver {
    private final GachaCardPoolSnapshotResolver snapshots;

    public GachaCardPoolTransactionSchemaResolver(GachaCardPoolSnapshotResolver snapshots) {
        this.snapshots = Objects.requireNonNull(snapshots, "snapshots");
    }

    @Override
    public GachaParameterSchema resolve(GachaTransactionSource source) {
        String snapshotHash = source.snapshotHash().orElseThrow(() ->
                new IllegalStateException("Transaction source has no parameter schema snapshot: " + source.id()));
        return snapshots.resolveSnapshot(snapshotHash).orElseThrow(() ->
                new IllegalStateException("Missing transaction source snapshot: " + snapshotHash)).parameterSchema();
    }
}

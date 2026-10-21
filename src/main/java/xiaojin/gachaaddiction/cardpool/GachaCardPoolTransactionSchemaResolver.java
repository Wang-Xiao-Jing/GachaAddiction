package xiaojin.gachaaddiction.cardpool;

import xiaojin.gachaaddiction.cardpool.transaction.IGachaTransactionParameterSchemaResolver;
import xiaojin.gachaaddiction.cardpool.transaction.GachaTransactionSource;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterSchema;

import java.util.Objects;

public final class GachaCardPoolTransactionSchemaResolver implements IGachaTransactionParameterSchemaResolver {
    private final IGachaCardPoolSnapshotResolver snapshots;

    public GachaCardPoolTransactionSchemaResolver(IGachaCardPoolSnapshotResolver snapshots) {
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

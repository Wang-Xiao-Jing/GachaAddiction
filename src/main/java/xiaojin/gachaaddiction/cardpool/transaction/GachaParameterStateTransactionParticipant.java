package xiaojin.gachaaddiction.cardpool.transaction;

import xiaojin.gachaaddiction.cardpool.GachaCardPoolDefinition;
import xiaojin.gachaaddiction.cardpool.IGachaCardPoolSnapshotResolver;
import xiaojin.gachaaddiction.cardpool.parameter.IGachaParameterStateRepository;

import java.util.Objects;

public final class GachaParameterStateTransactionParticipant implements IGachaTransactionParticipant {
    private final IGachaCardPoolSnapshotResolver snapshots;
    private final IGachaParameterStateRepository stateRepository;

    public GachaParameterStateTransactionParticipant(IGachaCardPoolSnapshotResolver snapshots,
                                                     IGachaParameterStateRepository stateRepository) {
        this.snapshots = Objects.requireNonNull(snapshots, "snapshots");
        this.stateRepository = Objects.requireNonNull(stateRepository, "stateRepository");
    }

    @Override
    public void prepareCommit(GachaTransactionContext context, GachaTransactionResult result) {
        GachaCardPoolDefinition cardPool = requireSnapshot(context);
        stateRepository.prepare(context, cardPool.parameterSchema(), cardPool.parameterBindings(),
                result.parameterChanges());
    }

    @Override
    public void commit(GachaTransactionContext context, GachaTransactionResult result) {
        stateRepository.commitPrepared(context);
    }

    @Override
    public void rollback(GachaTransactionContext context) {
        stateRepository.rollbackPrepared(context);
    }

    @Override
    public void recover(GachaTransactionContext context, GachaTransactionResult result,
                        GachaTransactionStage durableStage) {
        if (durableStage == GachaTransactionStage.COMMITTED || durableStage == GachaTransactionStage.DELIVERED) {
            GachaCardPoolDefinition cardPool = requireSnapshot(context);
            stateRepository.recover(context, cardPool.parameterSchema(), cardPool.parameterBindings(),
                    result.parameterChanges());
        } else {
            stateRepository.rollbackPrepared(context);
        }
    }

    private GachaCardPoolDefinition requireSnapshot(GachaTransactionContext context) {
        String snapshotHash = context.source().snapshotHash().orElseThrow(() ->
                new IllegalStateException("Card pool transaction source has no snapshot: " + context.source().id()));
        return snapshots.resolveSnapshot(snapshotHash).orElseThrow(() ->
                new IllegalStateException("Missing card pool transaction snapshot: "
                        + snapshotHash));
    }
}

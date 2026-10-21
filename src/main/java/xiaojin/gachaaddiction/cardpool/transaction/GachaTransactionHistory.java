package xiaojin.gachaaddiction.cardpool.transaction;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public record GachaTransactionHistory(GachaTransactionId transactionId, List<GachaTransactionRecord> records) {
    public GachaTransactionHistory {
        Objects.requireNonNull(transactionId, "transactionId");
        records = records.stream().sorted(Comparator.comparingLong(GachaTransactionRecord::sequence)).toList();
        if (records.isEmpty()) {
            throw new IllegalArgumentException("Transaction history cannot be empty");
        }
        if (records.stream().anyMatch(record -> !record.transactionId().equals(transactionId))) {
            throw new IllegalArgumentException("Transaction history contains another transaction ID");
        }
        validateTransitions(records);
    }

    private static void validateTransitions(List<GachaTransactionRecord> records) {
        GachaTransactionStage previous = null;
        long previousSequence = -1;
        for (GachaTransactionRecord record : records) {
            if (record.sequence() <= previousSequence) {
                throw new IllegalArgumentException("Transaction record sequence must increase");
            }
            if (!isValidTransition(previous, record.stage())) {
                throw new IllegalArgumentException("Invalid transaction stage transition: " + previous + " -> "
                        + record.stage());
            }
            previous = record.stage();
            previousSequence = record.sequence();
        }
    }

    private static boolean isValidTransition(GachaTransactionStage previous, GachaTransactionStage next) {
        if (previous == null) {
            return next == GachaTransactionStage.PREPARED;
        }
        return switch (previous) {
            case PREPARED -> next == GachaTransactionStage.RESERVED || next == GachaTransactionStage.ROLLED_BACK;
            case RESERVED -> next == GachaTransactionStage.REWARD_COMPUTED
                    || next == GachaTransactionStage.ROLLED_BACK;
            case REWARD_COMPUTED -> next == GachaTransactionStage.COMMITTED
                    || next == GachaTransactionStage.ROLLED_BACK;
            case COMMITTED -> next == GachaTransactionStage.DELIVERED;
            case DELIVERED, ROLLED_BACK -> false;
        };
    }

    public GachaTransactionRecord latest() {
        return records.getLast();
    }

    public GachaRecoveryAction recoveryAction() {
        return switch (latest().stage()) {
            case PREPARED, RESERVED, REWARD_COMPUTED -> GachaRecoveryAction.ROLLBACK;
            case COMMITTED -> GachaRecoveryAction.COMPLETE_COMMIT;
            case DELIVERED -> GachaRecoveryAction.RESTORE_STATE;
            case ROLLED_BACK -> GachaRecoveryAction.NONE;
        };
    }

    public long recoveryOrder() {
        return records.stream().filter(record -> record.stage() == GachaTransactionStage.COMMITTED)
                .mapToLong(GachaTransactionRecord::sequence).findFirst().orElse(latest().sequence());
    }
}

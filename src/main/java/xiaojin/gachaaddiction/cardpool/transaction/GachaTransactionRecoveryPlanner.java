package xiaojin.gachaaddiction.cardpool.transaction;

import java.util.*;

public final class GachaTransactionRecoveryPlanner {
    private GachaTransactionRecoveryPlanner() {
    }

    public static List<GachaTransactionHistory> histories(List<GachaTransactionRecord> records) {
        List<GachaTransactionRecord> ordered = records.stream()
                .sorted(Comparator.comparingLong(GachaTransactionRecord::sequence)).toList();
        Map<GachaTransactionId, List<GachaTransactionRecord>> grouped = new LinkedHashMap<>();
        long previousSequence = -1;
        for (GachaTransactionRecord record : ordered) {
            if (record.sequence() <= previousSequence) {
                throw new IllegalArgumentException("WAL record sequence must be globally increasing");
            }
            grouped.computeIfAbsent(record.transactionId(), ignored -> new ArrayList<>()).add(record);
            previousSequence = record.sequence();
        }
        return grouped.entrySet().stream().map(entry -> new GachaTransactionHistory(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparingLong(GachaTransactionHistory::recoveryOrder)).toList();
    }
}

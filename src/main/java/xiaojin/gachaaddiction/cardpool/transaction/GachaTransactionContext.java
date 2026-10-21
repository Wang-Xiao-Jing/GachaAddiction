package xiaojin.gachaaddiction.cardpool.transaction;

import java.util.Objects;
import java.util.UUID;

public record GachaTransactionContext(GachaTransactionId transactionId, UUID playerId,
                                      GachaTransactionSource source) {
    public GachaTransactionContext {
        Objects.requireNonNull(transactionId, "transactionId");
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(source, "source");
    }
}

package xiaojin.gachaaddiction.cardpool.transaction;

import java.util.Objects;
import java.util.UUID;

public record GachaTransactionId(UUID value) {
    public GachaTransactionId {
        Objects.requireNonNull(value, "value");
    }

    public static GachaTransactionId create() {
        return new GachaTransactionId(UUID.randomUUID());
    }
}

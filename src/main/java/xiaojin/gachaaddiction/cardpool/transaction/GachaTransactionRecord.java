package xiaojin.gachaaddiction.cardpool.transaction;

import net.minecraft.nbt.CompoundTag;

import java.util.Objects;

public record GachaTransactionRecord(long sequence, GachaTransactionId transactionId, GachaTransactionStage stage,
                                     CompoundTag payload) {
    public GachaTransactionRecord(long sequence, GachaTransactionId transactionId,
                                  GachaTransactionStage stage, CompoundTag payload) {
        if (sequence < 0) {
            throw new IllegalArgumentException("Transaction sequence cannot be negative");
        }
        this.sequence = sequence;
        this.transactionId = Objects.requireNonNull(transactionId, "transactionId");
        this.stage = Objects.requireNonNull(stage, "stage");
        this.payload = Objects.requireNonNull(payload, "payload").copy();
    }

    @Override
    public CompoundTag payload() {
        return payload.copy();
    }
}

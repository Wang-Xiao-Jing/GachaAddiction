package xiaojin.gachaaddiction.cardpool.transaction;

import net.minecraft.nbt.CompoundTag;

import java.util.List;
import java.util.concurrent.CompletionStage;

public interface GachaTransactionJournal extends AutoCloseable {
    CompletionStage<Long> append(GachaTransactionId transactionId, GachaTransactionStage stage,
                                 CompoundTag payload, GachaDurability durability);

    List<GachaTransactionRecord> readAll();

    CompletionStage<Void> flush();

    @Override
    void close();
}

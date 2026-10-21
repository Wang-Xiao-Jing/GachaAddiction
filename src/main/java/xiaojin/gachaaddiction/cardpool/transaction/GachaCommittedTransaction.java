package xiaojin.gachaaddiction.cardpool.transaction;

import java.util.Objects;
import java.util.concurrent.CompletionStage;

public record GachaCommittedTransaction(GachaTransactionContext context, GachaTransactionResult result,
                                        CompletionStage<Void> effectsCompletion) {
    public GachaCommittedTransaction {
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(result, "result");
        Objects.requireNonNull(effectsCompletion, "effectsCompletion");
    }
}

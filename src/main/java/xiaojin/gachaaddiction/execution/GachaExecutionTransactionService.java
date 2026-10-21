package xiaojin.gachaaddiction.execution;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.cardpool.transaction.*;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public final class GachaExecutionTransactionService {
    private final GachaExecutionEngine engine;
    private final GachaTransactionCoordinator transactions;

    public GachaExecutionTransactionService(GachaExecutionEngine engine,
                                            GachaTransactionCoordinator transactions) {
        this.engine = Objects.requireNonNull(engine, "engine");
        this.transactions = Objects.requireNonNull(transactions, "transactions");
    }

    public CompletionStage<GachaCommittedTransaction> execute(UUID playerId, ResourceLocation planId,
                                                              GachaExecutionRequest request,
                                                              CompoundTag sourceData,
                                                              GachaDurability durability,
                                                              GachaTransactionEffects effects) {
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(planId, "planId");
        Objects.requireNonNull(request, "request");
        GachaTransactionSource source = new GachaTransactionSource(request.source().id(),
                request.source().snapshotHash(), Objects.requireNonNull(sourceData, "sourceData"));
        GachaTransactionContext context = new GachaTransactionContext(
                GachaTransactionId.create(), playerId, source);
        GachaExecutionResult result = engine.execute(planId, request);
        return transactions.execute(context, result.consumption(), durability,
                () -> GachaTransactionResult.from(result), effects);
    }
}

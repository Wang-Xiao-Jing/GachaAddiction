package xiaojin.gachaaddiction.cardpool.transaction;

import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.persistence.GachaDecodedTransactionPayload;
import xiaojin.gachaaddiction.persistence.GachaTransactionNbtPayloadDecoder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.Predicate;

public final class GachaTransactionRecovery {
    private final GachaTransactionJournal journal;
    private final GachaConsumptionHandlerResolver handlerResolver;
    private final GachaTransactionNbtPayloadDecoder payloadDecoder;
    private final GachaTransactionParticipant participant;
    private final GachaTransactionEffects effects;
    private final Executor mainThreadExecutor;

    public GachaTransactionRecovery(GachaTransactionJournal journal,
                                    GachaConsumptionHandlerResolver handlerResolver,
                                    GachaTransactionNbtPayloadDecoder payloadDecoder,
                                    GachaTransactionParticipant participant,
                                    GachaTransactionEffects effects, Executor mainThreadExecutor) {
        this.journal = Objects.requireNonNull(journal, "journal");
        this.handlerResolver = Objects.requireNonNull(handlerResolver, "handlerResolver");
        this.payloadDecoder = Objects.requireNonNull(payloadDecoder, "payloadDecoder");
        this.participant = Objects.requireNonNull(participant, "participant");
        this.effects = Objects.requireNonNull(effects, "effects");
        this.mainThreadExecutor = Objects.requireNonNull(mainThreadExecutor, "mainThreadExecutor");
    }

    public CompletionStage<Void> recover(GachaDurability durability) {
        return recover(durability, context -> true);
    }

    public CompletionStage<Void> recover(GachaDurability durability,
                                         Predicate<GachaTransactionContext> commitEligibility) {
        Objects.requireNonNull(durability, "durability");
        Objects.requireNonNull(commitEligibility, "commitEligibility");
        CompletionStage<Void> recovery = CompletableFuture.completedFuture(null);
        for (GachaTransactionHistory history : GachaTransactionRecoveryPlanner.histories(journal.readAll())) {
            if (history.recoveryAction() != GachaRecoveryAction.NONE) {
                recovery = recovery.thenComposeAsync(ignored -> recover(history, durability, commitEligibility),
                        mainThreadExecutor);
            }
        }
        return recovery;
    }

    private CompletionStage<Void> recover(GachaTransactionHistory history, GachaDurability durability,
                                          Predicate<GachaTransactionContext> commitEligibility) {
        GachaTransactionRecord latest = history.latest();
        GachaDecodedTransactionPayload payload = payloadDecoder.decode(latest.payload());
        if (!payload.context().transactionId().equals(history.transactionId())) {
            return CompletableFuture.failedFuture(new IllegalArgumentException(
                    "Transaction payload ID does not match WAL frame ID"));
        }
        return switch (history.recoveryAction()) {
            case NONE -> CompletableFuture.completedFuture(null);
            case RESTORE_STATE -> restoreState(payload);
            case ROLLBACK -> rollback(latest, payload, durability);
            case COMPLETE_COMMIT -> commitEligibility.test(payload.context())
                    && effects.canRecover(payload.context())
                    ? completeCommit(latest, payload, durability) : CompletableFuture.completedFuture(null);
        };
    }

    private CompletionStage<Void> restoreState(GachaDecodedTransactionPayload payload) {
        var result = payload.result().orElseThrow(() ->
                new IllegalArgumentException("Delivered transaction is missing its reward result"));
        participant.recover(payload.context(), result, GachaTransactionStage.DELIVERED);
        return CompletableFuture.completedFuture(null);
    }

    private CompletionStage<Void> rollback(GachaTransactionRecord latest,
                                           GachaDecodedTransactionPayload payload,
                                           GachaDurability durability) {
        List<GachaConsumptionReservation> reservations = latest.stage() == GachaTransactionStage.PREPARED
                ? findUnrecordedReservations(payload) : payload.reservations();
        List<GachaConsumptionReservation> reversed = new ArrayList<>(reservations);
        Collections.reverse(reversed);
        for (GachaConsumptionReservation reservation : reversed) {
            resolveHandler(reservation.handlerType()).rollback(payload.context(), reservation);
        }
        participant.rollback(payload.context());
        return journal.append(latest.transactionId(), GachaTransactionStage.ROLLED_BACK, latest.payload(), durability)
                .thenApply(sequence -> null);
    }

    private CompletionStage<Void> completeCommit(GachaTransactionRecord latest,
                                                 GachaDecodedTransactionPayload payload,
                                                 GachaDurability durability) {
        for (GachaConsumptionReservation reservation : payload.reservations()) {
            resolveHandler(reservation.handlerType()).recover(payload.context(), reservation,
                    GachaTransactionStage.COMMITTED);
        }
        var result = payload.result().orElseThrow(() ->
                new IllegalArgumentException("Committed transaction is missing its reward result"));
        participant.recover(payload.context(), result, GachaTransactionStage.COMMITTED);
        effects.apply(payload.context(), result);
        return journal.append(latest.transactionId(), GachaTransactionStage.DELIVERED, latest.payload(), durability)
                .thenApply(sequence -> null);
    }

    private List<GachaConsumptionReservation> findUnrecordedReservations(GachaDecodedTransactionPayload payload) {
        List<GachaConsumptionReservation> reservations = new ArrayList<>();
        for (GachaConsumptionSpec specification : payload.consumption()) {
            GachaConsumptionHandler handler = resolveHandler(specification.type());
            handler.findReservation(payload.context(), specification).ifPresent(reservations::add);
        }
        return List.copyOf(reservations);
    }

    private GachaConsumptionHandler resolveHandler(ResourceLocation type) {
        return handlerResolver.resolve(type).orElseThrow(() ->
                new IllegalStateException("Missing consumption handler during recovery: " + type));
    }
}

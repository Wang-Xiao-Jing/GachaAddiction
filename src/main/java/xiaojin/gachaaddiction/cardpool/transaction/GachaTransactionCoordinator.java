package xiaojin.gachaaddiction.cardpool.transaction;

import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

public final class GachaTransactionCoordinator {
    private final GachaTransactionJournal journal;
    private final GachaConsumptionHandlerResolver handlerResolver;
    private final GachaTransactionPayloadEncoder payloadEncoder;
    private final GachaTransactionParticipant participant;
    private final Executor mainThreadExecutor;

    public GachaTransactionCoordinator(GachaTransactionJournal journal,
                                       GachaConsumptionHandlerResolver handlerResolver,
                                       GachaTransactionPayloadEncoder payloadEncoder,
                                       GachaTransactionParticipant participant,
                                       Executor mainThreadExecutor) {
        this.journal = Objects.requireNonNull(journal, "journal");
        this.handlerResolver = Objects.requireNonNull(handlerResolver, "handlerResolver");
        this.payloadEncoder = Objects.requireNonNull(payloadEncoder, "payloadEncoder");
        this.participant = Objects.requireNonNull(participant, "participant");
        this.mainThreadExecutor = Objects.requireNonNull(mainThreadExecutor, "mainThreadExecutor");
    }

    public CompletionStage<GachaCommittedTransaction> execute(GachaTransactionContext context,
                                                              List<GachaConsumptionSpec> consumption,
                                                              GachaDurability durability,
                                                              Supplier<GachaTransactionResult> draw,
                                                              GachaTransactionEffects effects) {
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(consumption, "consumption");
        Objects.requireNonNull(durability, "durability");
        Objects.requireNonNull(draw, "draw");
        Objects.requireNonNull(effects, "effects");
        TransactionState state;
        try {
            effects.validate(context);
            state = new TransactionState(resolveAndValidate(context, consumption));
        } catch (RuntimeException failure) {
            return CompletableFuture.failedFuture(failure);
        }
        CompoundTag preparedPayload;
        try {
            preparedPayload = payloadEncoder.prepared(context, consumption);
        } catch (RuntimeException failure) {
            return CompletableFuture.failedFuture(failure);
        }

        CompletionStage<TransactionWork> prepared = journal.append(context.transactionId(),
                        GachaTransactionStage.PREPARED, preparedPayload, durability)
                .thenComposeAsync(sequence -> {
                    state.preparedRecorded = true;
                    return reserveAndRecord(context, durability, state);
                }, mainThreadExecutor)
                .thenComposeAsync(work -> computeAndRecord(context, durability, state, draw), mainThreadExecutor)
                .thenApplyAsync(work -> {
                    participant.prepareCommit(context, work.result);
                    state.participantPrepared = true;
                    return work;
                }, mainThreadExecutor);

        CompletionStage<TransactionWork> recoverable = prepared.exceptionallyCompose(failure ->
                rollbackAndFail(context, durability, state, failure));
        return recoverable.thenCompose(work -> commitRecord(context, durability, work, effects));
    }

    private List<ResolvedConsumption> resolveAndValidate(GachaTransactionContext context,
                                                         List<GachaConsumptionSpec> consumption) {
        List<ResolvedConsumption> resolved = new ArrayList<>();
        for (GachaConsumptionSpec specification : consumption) {
            GachaConsumptionHandler handler = handlerResolver.resolve(specification.type())
                    .orElseThrow(() -> new IllegalArgumentException("Unknown consumption handler: "
                            + specification.type()));
            handler.validate(context, specification);
            resolved.add(new ResolvedConsumption(handler, specification));
        }
        return List.copyOf(resolved);
    }

    private CompletionStage<TransactionWork> reserveAndRecord(GachaTransactionContext context,
                                                              GachaDurability durability,
                                                              TransactionState state) {
        for (ResolvedConsumption consumption : state.consumption) {
            GachaConsumptionReservation reservation = consumption.handler.reserve(context, consumption.specification);
            if (!reservation.handlerType().equals(consumption.handler.type())) {
                throw new IllegalStateException("Consumption reservation type does not match its handler");
            }
            state.reservations.add(new ResolvedReservation(consumption.handler, reservation));
        }
        List<GachaConsumptionReservation> reservations = state.reservationValues();
        return journal.append(context.transactionId(), GachaTransactionStage.RESERVED,
                        payloadEncoder.reserved(context, reservations), durability)
                .thenApply(sequence -> new TransactionWork(reservations, null));
    }

    private CompletionStage<TransactionWork> computeAndRecord(GachaTransactionContext context,
                                                              GachaDurability durability,
                                                              TransactionState state,
                                                              Supplier<GachaTransactionResult> draw) {
        GachaTransactionResult result = Objects.requireNonNull(draw.get(), "draw result");
        List<GachaConsumptionReservation> reservations = state.reservationValues();
        CompoundTag payload = payloadEncoder.rewardComputed(context, result, reservations);
        return journal.append(context.transactionId(), GachaTransactionStage.REWARD_COMPUTED, payload, durability)
                .thenApply(sequence -> new TransactionWork(reservations, result));
    }

    private CompletionStage<GachaCommittedTransaction> commitRecord(GachaTransactionContext context,
                                                                    GachaDurability durability,
                                                                    TransactionWork work,
                                                                    GachaTransactionEffects effects) {
        CompoundTag payload = payloadEncoder.rewardComputed(context, work.result, work.reservations);
        return journal.append(context.transactionId(), GachaTransactionStage.COMMITTED, payload, durability)
                .thenApply(sequence -> {
                    CompletionStage<Void> effectsCompletion = CompletableFuture.runAsync(() -> {
                        commitReservations(context, work.reservations);
                        participant.commit(context, work.result);
                        effects.apply(context, work.result);
                    }, mainThreadExecutor).thenCompose(ignored -> journal.append(context.transactionId(),
                            GachaTransactionStage.DELIVERED, payload, durability).thenApply(delivered -> null));
                    return new GachaCommittedTransaction(context, work.result, effectsCompletion);
                });
    }

    private CompletionStage<TransactionWork> rollbackAndFail(GachaTransactionContext context,
                                                             GachaDurability durability,
                                                             TransactionState state,
                                                             Throwable failure) {
        if (!state.preparedRecorded) {
            return CompletableFuture.failedFuture(failure);
        }
        if (state.participantPrepared) {
            try {
                participant.rollback(context);
            } catch (RuntimeException rollbackFailure) {
                failure.addSuppressed(rollbackFailure);
            }
        }
        rollbackReservations(context, state.reservations, failure);
        CompoundTag payload = payloadEncoder.reserved(context, state.reservationValues());
        return journal.append(context.transactionId(), GachaTransactionStage.ROLLED_BACK, payload, durability)
                .thenCompose(sequence -> CompletableFuture.failedFuture(failure));
    }

    private void commitReservations(GachaTransactionContext context,
                                    List<GachaConsumptionReservation> reservations) {
        for (GachaConsumptionReservation reservation : reservations) {
            resolveHandler(reservation).commit(context, reservation);
        }
    }

    private void rollbackReservations(GachaTransactionContext context, List<ResolvedReservation> reservations,
                                      Throwable originalFailure) {
        List<ResolvedReservation> reversed = new ArrayList<>(reservations);
        Collections.reverse(reversed);
        for (ResolvedReservation reservation : reversed) {
            try {
                reservation.handler.rollback(context, reservation.reservation);
            } catch (RuntimeException rollbackFailure) {
                originalFailure.addSuppressed(rollbackFailure);
            }
        }
    }

    private GachaConsumptionHandler resolveHandler(GachaConsumptionReservation reservation) {
        return handlerResolver.resolve(reservation.handlerType())
                .orElseThrow(() -> new IllegalStateException("Missing consumption handler during commit: "
                        + reservation.handlerType()));
    }

    private record ResolvedConsumption(GachaConsumptionHandler handler, GachaConsumptionSpec specification) {
    }

    private record ResolvedReservation(GachaConsumptionHandler handler,
                                       GachaConsumptionReservation reservation) {
    }

    private record TransactionWork(List<GachaConsumptionReservation> reservations, GachaTransactionResult result) {
    }

    private static final class TransactionState {
        private final List<ResolvedConsumption> consumption;
        private final List<ResolvedReservation> reservations = new ArrayList<>();
        private boolean preparedRecorded;
        private boolean participantPrepared;

        private TransactionState(List<ResolvedConsumption> consumption) {
            this.consumption = consumption;
        }

        private List<GachaConsumptionReservation> reservationValues() {
            return reservations.stream().map(ResolvedReservation::reservation).toList();
        }
    }
}

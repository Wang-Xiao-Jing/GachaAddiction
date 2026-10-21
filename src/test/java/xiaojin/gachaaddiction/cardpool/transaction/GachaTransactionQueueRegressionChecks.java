package xiaojin.gachaaddiction.cardpool.transaction;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

public class GachaTransactionQueueRegressionChecks {
    public static void main(String[] args) {
        checkInlineReentry();
        checkRecoveryAndDrawStayTogether();
        checkFailureDoesNotPoisonQueue();
        checkCallerCannotReleaseReservation();
        checkOwnerClose();
        checkCloseBeforeStart();
        checkClosedExecutorDropsContinuation();
        System.out.println("Transaction queue regression checks passed");
    }

    private static void checkInlineReentry() {
        GachaTransactionQueue queue = new GachaTransactionQueue(Runnable::run);
        CompletableFuture<String> finishDraw = new CompletableFuture<>();
        List<String> events = new ArrayList<>();
        List<CompletionStage<String>> nested = new ArrayList<>();

        CompletionStage<String> draw = queue.submit(() -> {
            events.add("draw");
            nested.add(queue.submit(() -> {
                events.add("nested-recovery");
                return CompletableFuture.completedFuture("recovered");
            }));
            return finishDraw;
        });

        require(events.equals(List.of("draw")), "Reentrant recovery started before the active draw finished");
        require(!nested.getFirst().toCompletableFuture().isDone(), "Reentrant queue reservation completed early");
        finishDraw.complete("committed");
        require(draw.toCompletableFuture().join().equals("committed"), "Draw result changed");
        require(nested.getFirst().toCompletableFuture().join().equals("recovered"), "Nested recovery did not resume");
        require(events.equals(List.of("draw", "nested-recovery")), "Inline execution order changed");
    }

    private static void checkRecoveryAndDrawStayTogether() {
        ManualExecutor executor = new ManualExecutor();
        GachaTransactionQueue queue = new GachaTransactionQueue(executor);
        CompletableFuture<Void> finishRecovery = new CompletableFuture<>();
        CompletableFuture<String> finishDraw = new CompletableFuture<>();
        List<String> events = new ArrayList<>();

        CompletionStage<String> draw = queue.submit(() -> {
            events.add("recovery");
            return finishRecovery.thenComposeAsync(ignored -> {
                events.add("draw");
                return finishDraw;
            }, executor);
        });
        CompletionStage<Void> nextRecovery = queue.submit(() -> {
            events.add("next-recovery");
            return CompletableFuture.completedFuture(null);
        });

        require(executor.size() == 1, "Both queued operations were scheduled before recovery");
        executor.runNext();
        require(events.equals(List.of("recovery")), "Recovery did not run first");
        require(executor.size() == 0, "Later recovery bypassed the pending draw");
        finishRecovery.complete(null);
        require(executor.size() == 1, "Draw continuation did not return to the supplied executor");
        executor.runNext();
        require(events.equals(List.of("recovery", "draw")), "Later recovery slipped between recovery and draw");
        require(!nextRecovery.toCompletableFuture().isDone(), "Later recovery completed while draw was active");

        finishDraw.complete("committed");
        executor.runNext();
        require(draw.toCompletableFuture().join().equals("committed"), "Committed draw result changed");
        nextRecovery.toCompletableFuture().join();
        require(events.equals(List.of("recovery", "draw", "next-recovery")), "Queued recovery order changed");
        require(executor.size() == 0, "Unexpected queued work remained");
    }

    private static void checkFailureDoesNotPoisonQueue() {
        GachaTransactionQueue queue = new GachaTransactionQueue(Runnable::run);
        IllegalStateException expected = new IllegalStateException("Draw rejected");
        CompletionStage<String> failedDraw = queue.submit(() -> CompletableFuture.failedFuture(expected));
        expectFailure(failedDraw, expected);
        require(queue.submit(() -> CompletableFuture.completedFuture("recovered"))
                .toCompletableFuture().join().equals("recovered"), "Failed draw poisoned subsequent recovery");

        CompletionStage<String> failedStart = queue.submit(() -> {
            throw expected;
        });
        expectFailure(failedStart, expected);
        require(queue.submit(() -> CompletableFuture.completedFuture("drawn"))
                .toCompletableFuture().join().equals("drawn"), "Synchronous failure retained the queue reservation");
    }

    private static void checkCallerCannotReleaseReservation() {
        GachaTransactionQueue queue = new GachaTransactionQueue(Runnable::run);
        CompletableFuture<String> finishDraw = new CompletableFuture<>();
        CompletionStage<String> draw = queue.submit(() -> finishDraw);
        List<String> events = new ArrayList<>();
        CompletionStage<String> recovery = queue.submit(() -> {
            events.add("recovery");
            return CompletableFuture.completedFuture("recovered");
        });

        draw.toCompletableFuture().cancel(true);
        draw.toCompletableFuture().complete("forged");
        require(events.isEmpty(), "Caller cancellation or completion released the live draw reservation");
        require(!finishDraw.isDone(), "Caller mutated the underlying draw completion");
        require(!recovery.toCompletableFuture().isDone(), "Recovery bypassed a cancelled observer");
        finishDraw.complete("committed");
        require(draw.toCompletableFuture().join().equals("committed"), "Caller replaced the committed result");
        require(recovery.toCompletableFuture().join().equals("recovered"), "Queue did not resume after actual completion");
    }

    private static void checkOwnerClose() {
        ManualExecutor executor = new ManualExecutor();
        GachaTransactionQueue queue = new GachaTransactionQueue(executor);
        CompletableFuture<String> operation = new CompletableFuture<>();
        List<String> events = new ArrayList<>();
        CompletionStage<String> active = queue.submit(() -> {
            events.add("active");
            return operation;
        });
        CompletionStage<String> waiting = queue.submit(() -> {
            events.add("waiting");
            return CompletableFuture.completedFuture("unexpected");
        });
        executor.runNext();
        queue.close();
        require(active.toCompletableFuture().isCompletedExceptionally(), "Closing left an active observer waiting");
        require(waiting.toCompletableFuture().isCompletedExceptionally(), "Closing left queued work waiting");
        require(!operation.isDone(), "Queue owner cancelled the underlying durable operation");
        while (executor.size() > 0) {
            executor.runNext();
        }
        require(events.equals(List.of("active")), "Queued work ran after the owner closed the queue");
        require(queue.submit(() -> CompletableFuture.completedFuture("unexpected"))
                .toCompletableFuture().isCompletedExceptionally(), "Closed queue accepted a new operation");
        operation.complete("durable");
        require(active.toCompletableFuture().isCompletedExceptionally(), "Late completion reopened a closed observer");
        queue.close();
    }

    private static void checkCloseBeforeStart() {
        ManualExecutor executor = new ManualExecutor();
        GachaTransactionQueue queue = new GachaTransactionQueue(executor);
        List<String> events = new ArrayList<>();
        CompletionStage<Void> queued = queue.submit(() -> {
            events.add("started");
            return CompletableFuture.completedFuture(null);
        });
        queue.close();
        while (executor.size() > 0) {
            executor.runNext();
        }
        require(events.isEmpty(), "A scheduled operation started after shutdown");
        require(queued.toCompletableFuture().isCompletedExceptionally(), "Shutdown did not finish its queued observer");
    }

    private static void checkClosedExecutorDropsContinuation() {
        ManualExecutor executor = new ManualExecutor();
        GachaTransactionQueue queue = new GachaTransactionQueue(executor);
        List<String> worldChanges = new ArrayList<>();
        queue.execute(() -> worldChanges.add("changed"));
        queue.close();
        while (executor.size() > 0) {
            executor.runNext();
        }
        require(worldChanges.isEmpty(), "A main-thread continuation changed state after runtime closure");
        try {
            queue.execute(() -> worldChanges.add("late"));
        } catch (RejectedExecutionException expected) {
            return;
        }
        throw new AssertionError("Closed transaction executor accepted a new continuation");
    }

    private static void expectFailure(CompletionStage<?> stage, Throwable expected) {
        try {
            stage.toCompletableFuture().join();
            throw new AssertionError("Expected draw failure was not reported");
        } catch (CompletionException failure) {
            require(failure.getCause() == expected, "Queue replaced the original draw failure");
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static class ManualExecutor implements Executor {
        private final ArrayDeque<Runnable> pending = new ArrayDeque<>();

        @Override
        public void execute(@NotNull Runnable command) {
            pending.addLast(command);
        }

        private int size() {
            return pending.size();
        }

        private void runNext() {
            require(!pending.isEmpty(), "Expected queued main-thread continuation");
            pending.removeFirst().run();
        }
    }
}

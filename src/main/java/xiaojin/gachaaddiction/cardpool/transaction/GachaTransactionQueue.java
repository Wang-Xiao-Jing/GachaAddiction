package xiaojin.gachaaddiction.cardpool.transaction;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Supplier;

public class GachaTransactionQueue implements Executor, AutoCloseable {
    private final Executor executor;
    private CompletionStage<Void> tail = CompletableFuture.completedFuture(null);
    private final Set<CompletableFuture<?>> tasks = new HashSet<>();
    private boolean closed;

    public GachaTransactionQueue(Executor executor) {
        this.executor = Objects.requireNonNull(executor, "executor");
    }

    @Override
    public void execute(@NotNull Runnable command) {
        Objects.requireNonNull(command, "command");
        synchronized (this) {
            if (closed) {
                throw new RejectedExecutionException("Gacha transaction executor is closed");
            }
        }
        executor.execute(() -> {
            synchronized (this) {
                if (closed) {
                    return;
                }
            }
            command.run();
        });
    }

    public <T> CompletionStage<T> submit(Supplier<? extends CompletionStage<T>> operation) {
        Objects.requireNonNull(operation, "operation");
        CompletableFuture<T> result = new CompletableFuture<>();
        CompletionStage<Void> previous;
        synchronized (this) {
            if (closed) {
                return CompletableFuture.<T>failedFuture(new IllegalStateException("Gacha transaction queue is closed"))
                        .minimalCompletionStage();
            }
            previous = tail;
            // 执行器可以内联运行并重入提交，必须先占住队列位置。
            tail = result.handle((ignored, failure) -> null);
            tasks.add(result);
            result.whenComplete((ignored, failure) -> {
                synchronized (this) {
                    tasks.remove(result);
                }
            });
        }

        try {
            previous.thenComposeAsync(ignored -> {
                synchronized (this) {
                    if (closed) {
                        throw new IllegalStateException("Gacha transaction queue is closed");
                    }
                }
                return operation.get();
            }, executor).whenComplete((value, failure) -> {
                if (failure == null) {
                    result.complete(value);
                } else {
                    result.completeExceptionally(failure);
                }
            });
        } catch (RuntimeException | Error failure) {
            result.completeExceptionally(failure);
        }

        return result.minimalCompletionStage();
    }

    @Override
    public void close() {
        List<CompletableFuture<?>> unfinished;
        synchronized (this) {
            if (closed) {
                return;
            }
            closed = true;
            unfinished = List.copyOf(tasks);
            tasks.clear();
        }
        for (CompletableFuture<?> task : unfinished) {
            task.completeExceptionally(new IllegalStateException("Gacha transaction queue closed before completion"));
        }
    }
}

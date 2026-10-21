package xiaojin.gachaaddiction.persistence;

import net.minecraft.nbt.CompoundTag;
import xiaojin.gachaaddiction.cardpool.transaction.GachaDurability;
import xiaojin.gachaaddiction.cardpool.transaction.GachaTransactionId;
import xiaojin.gachaaddiction.cardpool.transaction.GachaTransactionRecord;
import xiaojin.gachaaddiction.cardpool.transaction.GachaTransactionStage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class GachaWalRegressionChecks {
    public static void main(String[] args) throws Exception {
        Path directory = Files.createTempDirectory("gacha-wal-regression-");
        Path wal = directory.resolve("transactions.wal");
        try {
            checkIoCallbackAndPayloadOwnership(wal);
            checkPersistedDurabilityModes(wal);
            System.out.println("WAL regression checks passed");
        } finally {
            try (var paths = Files.walk(directory)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                    Files.delete(path);
                }
            }
        }
    }

    private static void checkIoCallbackAndPayloadOwnership(Path wal) throws Exception {
        GachaFileTransactionJournal journal = new GachaFileTransactionJournal(wal, 4096, Duration.ofSeconds(1));
        CountDownLatch occupied = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        try {
            CompletableFuture<Void> blocker = journal.append(id(), GachaTransactionStage.PREPARED,
                    new CompoundTag(), GachaDurability.GROUP_COMMIT).thenRun(() -> {
                requireIoThread();
                occupied.countDown();
                try {
                    require(release.await(10, TimeUnit.SECONDS), "IO callback release timed out");
                } catch (InterruptedException failure) {
                    Thread.currentThread().interrupt();
                    throw new AssertionError(failure);
                }
            }).toCompletableFuture();
            require(occupied.await(10, TimeUnit.SECONDS), "IO callback did not start");

            CompoundTag payload = new CompoundTag();
            payload.putString("value", "original");
            CompletableFuture<List<GachaTransactionRecord>> snapshot = journal.append(id(),
                    GachaTransactionStage.PREPARED, payload, GachaDurability.SYNC).thenApply(sequence -> {
                requireIoThread();
                return journal.readAll();
            }).toCompletableFuture();
            payload.putString("value", "changed");
            release.countDown();
            blocker.get(10, TimeUnit.SECONDS);
            List<GachaTransactionRecord> records = snapshot.get(10, TimeUnit.SECONDS);
            require(records.size() == 2, "Unexpected WAL snapshot size");
            require(records.getLast().payload().getString("value").equals("original"),
                    "Queued WAL append retained the caller's mutable payload");

            for (GachaDurability durability : GachaDurability.values()) {
                journal.append(id(), GachaTransactionStage.PREPARED, new CompoundTag(), durability)
                        .toCompletableFuture().get(10, TimeUnit.SECONDS);
            }
        } finally {
            release.countDown();
            closeBounded(journal);
        }
    }

    private static void checkPersistedDurabilityModes(Path wal) throws Exception {
        GachaFileTransactionJournal journal = new GachaFileTransactionJournal(wal, 4096, Duration.ofMillis(10));
        try {
            List<GachaTransactionRecord> records = journal.readAll();
            require(records.size() == 2 + GachaDurability.values().length, "WAL records were not persisted");
            for (int index = 0; index < records.size(); index++) {
                require(records.get(index).sequence() == index, "WAL sequence order changed");
            }
            require(records.get(1).payload().getString("value").equals("original"),
                    "Persisted WAL payload differs from the submitted snapshot");
        } finally {
            closeBounded(journal);
        }
    }

    private static GachaTransactionId id() {
        return new GachaTransactionId(UUID.randomUUID());
    }

    private static void requireIoThread() {
        require(Thread.currentThread().getName().equals(GachaWalFormat.IO_THREAD_NAME),
                "Regression scenario did not execute on the WAL IO thread");
    }

    private static void closeBounded(GachaFileTransactionJournal journal) throws Exception {
        CompletableFuture.runAsync(journal::close).get(10, TimeUnit.SECONDS);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}


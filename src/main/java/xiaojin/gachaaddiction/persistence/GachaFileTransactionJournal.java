package xiaojin.gachaaddiction.persistence;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import xiaojin.gachaaddiction.cardpool.transaction.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.zip.CRC32;

public final class GachaFileTransactionJournal implements GachaTransactionJournal {
    private static final int FIXED_HEADER_SIZE = Integer.BYTES * 4 + Long.BYTES * 3 + Byte.BYTES;

    private final int maxPayloadBytes;
    private final long groupCommitDelayMillis;
    private final ScheduledExecutorService ioExecutor;
    private final FileChannel channel;
    private final List<GachaTransactionRecord> records;
    private final List<PendingCommit> pendingGroupCommits = new ArrayList<>();
    private long nextSequence;
    private boolean groupCommitScheduled;

    public GachaFileTransactionJournal(Path path, int maxPayloadBytes, Duration groupCommitDelay) throws IOException {
        Path normalizedPath = Objects.requireNonNull(path, "path").toAbsolutePath().normalize();
        if (maxPayloadBytes <= 0) {
            throw new IllegalArgumentException("Maximum WAL payload size must be positive");
        }
        this.maxPayloadBytes = maxPayloadBytes;
        this.groupCommitDelayMillis = requireDelay(groupCommitDelay);
        Path parent = normalizedPath.getParent();
        if (parent == null) {
            throw new IllegalArgumentException("WAL path must have a parent directory");
        }
        Files.createDirectories(parent);
        this.records = new ArrayList<>(readFrames(normalizedPath, maxPayloadBytes));
        this.nextSequence = records.isEmpty() ? 0 : Math.incrementExact(records.getLast().sequence());
        this.channel = FileChannel.open(normalizedPath, StandardOpenOption.CREATE, StandardOpenOption.READ,
                StandardOpenOption.WRITE);
        this.channel.position(this.channel.size());
        this.ioExecutor = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, GachaWalFormat.IO_THREAD_NAME);
            thread.setDaemon(true);
            return thread;
        });
    }

    private static List<GachaTransactionRecord> readFrames(Path path, int maxPayloadBytes) throws IOException {
        if (!Files.exists(path)) {
            return List.of();
        }
        List<GachaTransactionRecord> records = new ArrayList<>();
        try (FileChannel readChannel = FileChannel.open(path, StandardOpenOption.READ, StandardOpenOption.WRITE);
             DataInputStream input = new DataInputStream(Channels.newInputStream(readChannel))) {
            while (true) {
                long frameStart = readChannel.position();
                try {
                    records.add(readFrame(input, maxPayloadBytes));
                } catch (EOFException truncatedTail) {
                    if (readChannel.size() > frameStart) {
                        readChannel.truncate(frameStart);
                        readChannel.force(true);
                    }
                    break;
                }
            }
        }
        return List.copyOf(records);
    }

    private static GachaTransactionRecord readFrame(DataInputStream input, int maxPayloadBytes) throws IOException {
        int magic = input.readInt();
        if (magic != GachaWalFormat.MAGIC) {
            throw new IOException("Invalid WAL frame magic");
        }
        int version = input.readInt();
        if (version != GachaWalFormat.FRAME_VERSION) {
            throw new IOException("Unsupported WAL frame version: " + version);
        }
        long sequence = input.readLong();
        UUID transactionId = new UUID(input.readLong(), input.readLong());
        int stageOrdinal = Byte.toUnsignedInt(input.readByte());
        if (stageOrdinal >= GachaTransactionStage.values().length) {
            throw new IOException("Invalid WAL transaction stage: " + stageOrdinal);
        }
        int payloadLength = input.readInt();
        int expectedChecksum = input.readInt();
        if (payloadLength < 0 || payloadLength > maxPayloadBytes) {
            throw new IOException("Invalid WAL payload length: " + payloadLength);
        }
        byte[] payload = input.readNBytes(payloadLength);
        if (payload.length != payloadLength) {
            throw new EOFException("Truncated WAL payload");
        }
        CRC32 checksum = new CRC32();
        checksum.update(payload);
        if ((int) checksum.getValue() != expectedChecksum) {
            throw new IOException("WAL payload checksum mismatch");
        }
        CompoundTag decoded;
        try {
            decoded = NbtIo.read(new DataInputStream(new ByteArrayInputStream(payload)),
                    NbtAccounter.create(maxPayloadBytes));
        } catch (EOFException malformedPayload) {
            throw new IOException("Invalid WAL NBT payload", malformedPayload);
        }
        return new GachaTransactionRecord(sequence, new GachaTransactionId(transactionId),
                GachaTransactionStage.values()[stageOrdinal], decoded);
    }

    private static long requireDelay(Duration delay) {
        Objects.requireNonNull(delay, "groupCommitDelay");
        long milliseconds = delay.toMillis();
        if (milliseconds <= 0) {
            throw new IllegalArgumentException("Group commit delay must be positive");
        }
        return milliseconds;
    }

    @Override
    public CompletionStage<Long> append(GachaTransactionId transactionId, GachaTransactionStage stage,
                                        CompoundTag payload, GachaDurability durability) {
        Objects.requireNonNull(transactionId, "transactionId");
        Objects.requireNonNull(stage, "stage");
        Objects.requireNonNull(payload, "payload");
        Objects.requireNonNull(durability, "durability");
        CompletableFuture<Long> completion = new CompletableFuture<>();
        ioExecutor.execute(() -> appendOnIoThread(transactionId, stage, payload.copy(), durability, completion));
        return completion;
    }

    @Override
    public List<GachaTransactionRecord> readAll() {
        return CompletableFuture.supplyAsync(() -> List.copyOf(records), ioExecutor).join();
    }

    @Override
    public CompletionStage<Void> flush() {
        CompletableFuture<Void> completion = new CompletableFuture<>();
        ioExecutor.execute(() -> {
            try {
                forceAndCompleteGroup();
                completion.complete(null);
            } catch (IOException failure) {
                completion.completeExceptionally(failure);
            }
        });
        return completion;
    }

    @Override
    public void close() {
        try {
            flush().toCompletableFuture().join();
        } finally {
            CompletableFuture<Void> closed = new CompletableFuture<>();
            ioExecutor.execute(() -> {
                try {
                    channel.close();
                    closed.complete(null);
                } catch (IOException failure) {
                    closed.completeExceptionally(failure);
                }
            });
            try {
                closed.join();
            } finally {
                ioExecutor.shutdown();
            }
        }
    }

    private void appendOnIoThread(GachaTransactionId transactionId, GachaTransactionStage stage,
                                  CompoundTag payload, GachaDurability durability,
                                  CompletableFuture<Long> completion) {
        try {
            byte[] payloadBytes = encodePayload(payload);
            long sequence = nextSequence++;
            writeFrame(sequence, transactionId.value(), stage, payloadBytes);
            records.add(new GachaTransactionRecord(sequence, transactionId, stage, payload));
            switch (durability) {
                case SYNC -> {
                    forceAndCompleteGroup();
                    completion.complete(sequence);
                }
                case GROUP_COMMIT -> enqueueGroupCommit(sequence, completion);
                case ASYNC -> completion.complete(sequence);
            }
        } catch (Throwable failure) {
            completion.completeExceptionally(failure);
        }
    }

    private byte[] encodePayload(CompoundTag payload) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream output = new DataOutputStream(bytes)) {
            NbtIo.write(payload, output);
        }
        byte[] encoded = bytes.toByteArray();
        if (encoded.length > maxPayloadBytes) {
            throw new IOException("WAL payload exceeds configured maximum size");
        }
        return encoded;
    }

    private void writeFrame(long sequence, UUID transactionId, GachaTransactionStage stage,
                            byte[] payload) throws IOException {
        CRC32 checksum = new CRC32();
        checksum.update(payload);
        ByteBuffer frame = ByteBuffer.allocate(Math.addExact(FIXED_HEADER_SIZE, payload.length));
        frame.putInt(GachaWalFormat.MAGIC);
        frame.putInt(GachaWalFormat.FRAME_VERSION);
        frame.putLong(sequence);
        frame.putLong(transactionId.getMostSignificantBits());
        frame.putLong(transactionId.getLeastSignificantBits());
        frame.put((byte) stage.ordinal());
        frame.putInt(payload.length);
        frame.putInt((int) checksum.getValue());
        frame.put(payload);
        frame.flip();
        while (frame.hasRemaining()) {
            int written = channel.write(frame);
            if (written <= 0) {
                throw new IOException("Unable to write WAL frame");
            }
        }
    }

    private void enqueueGroupCommit(long sequence, CompletableFuture<Long> completion) {
        pendingGroupCommits.add(new PendingCommit(sequence, completion));
        if (!groupCommitScheduled) {
            groupCommitScheduled = true;
            ioExecutor.schedule(this::runGroupCommit, groupCommitDelayMillis, TimeUnit.MILLISECONDS);
        }
    }

    private void runGroupCommit() {
        try {
            forceAndCompleteGroup();
        } catch (IOException failure) {
            failPendingGroup(failure);
        }
    }

    private void forceAndCompleteGroup() throws IOException {
        channel.force(true);
        List<PendingCommit> pending = List.copyOf(pendingGroupCommits);
        pendingGroupCommits.clear();
        groupCommitScheduled = false;
        pending.forEach(commit -> commit.completion.complete(commit.sequence));
    }

    private void failPendingGroup(Throwable failure) {
        List<PendingCommit> pending = List.copyOf(pendingGroupCommits);
        pendingGroupCommits.clear();
        groupCommitScheduled = false;
        pending.forEach(commit -> commit.completion.completeExceptionally(failure));
    }

    private record PendingCommit(long sequence, CompletableFuture<Long> completion) {
    }
}

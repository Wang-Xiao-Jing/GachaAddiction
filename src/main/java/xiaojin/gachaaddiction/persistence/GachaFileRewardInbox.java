package xiaojin.gachaaddiction.persistence;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.world.item.ItemStack;
import xiaojin.gachaaddiction.cardpool.delivery.IGachaRewardInbox;
import xiaojin.gachaaddiction.cardpool.delivery.GachaRewardInboxEntry;
import xiaojin.gachaaddiction.cardpool.delivery.GachaRewardInboxEntryKind;
import xiaojin.gachaaddiction.cardpool.transaction.GachaTransactionContext;
import xiaojin.gachaaddiction.cardpool.transaction.GachaTransactionId;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.*;

public final class GachaFileRewardInbox implements IGachaRewardInbox {
    public static final int FORMAT_VERSION = 2;

    private static final String FILE_EXTENSION = ".nbt";
    private static final String TEMP_FILE_PREFIX = "reward-";
    private static final String TEMP_FILE_SUFFIX = ".tmp";
    private static final String FORMAT_VERSION_KEY = "format_version";
    private static final String TRANSACTION_ID_KEY = "transaction_id";
    private static final String PLAYER_ID_KEY = "player_id";
    private static final String REWARDS_KEY = "rewards";
    private static final String KIND_KEY = "kind";

    private final Path root;
    private final int maximumEntryBytes;
    private final HolderLookup.Provider registries;

    public GachaFileRewardInbox(Path root, int maximumEntryBytes,
                                HolderLookup.Provider registries) throws IOException {
        this.root = Objects.requireNonNull(root, "root").toAbsolutePath().normalize();
        if (maximumEntryBytes <= 0) {
            throw new IllegalArgumentException("Maximum inbox entry size must be positive");
        }
        this.maximumEntryBytes = maximumEntryBytes;
        this.registries = Objects.requireNonNull(registries, "registries");
        Files.createDirectories(this.root);
    }

    @Override
    public synchronized void deliver(GachaTransactionContext context, List<ItemStack> rewards) {
        deliver(context, rewards, GachaRewardInboxEntryKind.REWARD);
    }

    @Override
    public synchronized void deliver(GachaTransactionContext context, List<ItemStack> rewards,
                                     GachaRewardInboxEntryKind kind) {
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(rewards, "rewards");
        Objects.requireNonNull(kind, "kind");
        try {
            store(new GachaRewardInboxEntry(context.transactionId(), context.playerId(), kind, rewards));
        } catch (IOException failure) {
            throw new UncheckedIOException("Unable to store gacha reward inbox entry", failure);
        }
    }

    @Override
    public synchronized Optional<GachaRewardInboxEntry> find(UUID playerId, GachaTransactionId transactionId) {
        try {
            Path target = entryPath(playerId, transactionId);
            return Files.exists(target) ? Optional.of(read(target)) : Optional.empty();
        } catch (IOException failure) {
            throw new UncheckedIOException("Unable to read gacha reward inbox entry", failure);
        }
    }

    @Override
    public synchronized List<GachaRewardInboxEntry> pending(UUID playerId) {
        Path playerDirectory = playerDirectory(playerId);
        if (!Files.isDirectory(playerDirectory)) {
            return List.of();
        }
        try (var files = Files.list(playerDirectory)) {
            List<GachaRewardInboxEntry> entries = new ArrayList<>();
            for (Path path : files.filter(this::isEntryFile).sorted(Comparator.comparing(Path::toString)).toList()) {
                entries.add(read(path));
            }
            return List.copyOf(entries);
        } catch (IOException failure) {
            throw new UncheckedIOException("Unable to list gacha reward inbox entries", failure);
        }
    }

    @Override
    public synchronized Optional<GachaRewardInboxEntry> take(UUID playerId, GachaTransactionId transactionId) {
        try {
            Path target = entryPath(playerId, transactionId);
            if (!Files.exists(target)) {
                return Optional.empty();
            }
            GachaRewardInboxEntry entry = read(target);
            Files.delete(target);
            return Optional.of(entry);
        } catch (IOException failure) {
            throw new UncheckedIOException("Unable to take gacha reward inbox entry", failure);
        }
    }

    @Override
    public synchronized void restore(GachaRewardInboxEntry entry) {
        try {
            store(Objects.requireNonNull(entry, "entry"));
        } catch (IOException failure) {
            throw new UncheckedIOException("Unable to restore gacha reward inbox entry", failure);
        }
    }

    private void store(GachaRewardInboxEntry entry) throws IOException {
        byte[] encoded = encode(entry);
        requireSize(encoded.length);
        Path target = entryPath(entry.playerId(), entry.transactionId());
        Files.createDirectories(target.getParent());
        if (Files.exists(target)) {
            requireIdentical(target, encoded);
            return;
        }
        Path temporary = Files.createTempFile(target.getParent(), TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX);
        try {
            try (FileChannel channel = FileChannel.open(temporary, StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING)) {
                ByteBuffer buffer = ByteBuffer.wrap(encoded);
                while (buffer.hasRemaining()) {
                    int written = channel.write(buffer);
                    if (written <= 0) {
                        throw new IOException("Unable to write reward inbox entry");
                    }
                }
                channel.force(true);
            }
            try {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException unsupported) {
                try {
                    Files.move(temporary, target);
                } catch (FileAlreadyExistsException raced) {
                    requireIdentical(target, encoded);
                }
            } catch (FileAlreadyExistsException raced) {
                requireIdentical(target, encoded);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private byte[] encode(GachaRewardInboxEntry entry) throws IOException {
        CompoundTag encoded = new CompoundTag();
        encoded.putInt(FORMAT_VERSION_KEY, FORMAT_VERSION);
        encoded.putUUID(TRANSACTION_ID_KEY, entry.transactionId().value());
        encoded.putUUID(PLAYER_ID_KEY, entry.playerId());
        encoded.putString(KIND_KEY, entry.kind().name());
        ListTag rewards = new ListTag();
        entry.rewards().forEach(stack -> rewards.add(stack.save(registries)));
        encoded.put(REWARDS_KEY, rewards);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream output = new DataOutputStream(bytes)) {
            NbtIo.write(encoded, output);
        }
        return bytes.toByteArray();
    }

    private GachaRewardInboxEntry read(Path path) throws IOException {
        requireSize(Files.size(path));
        byte[] bytes = Files.readAllBytes(path);
        CompoundTag encoded;
        try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(bytes))) {
            encoded = NbtIo.read(input, NbtAccounter.create(maximumEntryBytes));
        }
        int formatVersion = encoded.getInt(FORMAT_VERSION_KEY);
        if (formatVersion != FORMAT_VERSION) {
            throw new IOException("Unsupported reward inbox entry version: "
                    + encoded.getInt(FORMAT_VERSION_KEY));
        }
        UUID playerId = encoded.getUUID(PLAYER_ID_KEY);
        GachaTransactionId transactionId = new GachaTransactionId(encoded.getUUID(TRANSACTION_ID_KEY));
        if (!path.equals(entryPath(playerId, transactionId))) {
            throw new IOException("Reward inbox entry path does not match its payload");
        }
        List<ItemStack> rewards = new ArrayList<>();
        ListTag encodedRewards = encoded.getList(REWARDS_KEY, Tag.TAG_COMPOUND);
        for (int index = 0; index < encodedRewards.size(); index++) {
            ItemStack reward = ItemStack.parseOptional(registries, encodedRewards.getCompound(index));
            if (!reward.isEmpty()) {
                rewards.add(reward);
            }
        }
        GachaRewardInboxEntryKind kind = GachaRewardInboxEntryKind.valueOf(encoded.getString(KIND_KEY));
        return new GachaRewardInboxEntry(transactionId, playerId, kind, rewards);
    }

    private void requireIdentical(Path target, byte[] expected) throws IOException {
        requireSize(Files.size(target));
        if (!Arrays.equals(Files.readAllBytes(target), expected)) {
            throw new IOException("Reward inbox transaction ID already contains different rewards");
        }
    }

    private Path entryPath(UUID playerId, GachaTransactionId transactionId) {
        return playerDirectory(playerId).resolve(transactionId.value() + FILE_EXTENSION);
    }

    private Path playerDirectory(UUID playerId) {
        return root.resolve(Objects.requireNonNull(playerId, "playerId").toString());
    }

    private boolean isEntryFile(Path path) {
        return Files.isRegularFile(path) && path.getFileName().toString().endsWith(FILE_EXTENSION);
    }

    private void requireSize(long size) throws IOException {
        if (size <= 0 || size > maximumEntryBytes) {
            throw new IOException("Reward inbox entry size is outside the configured limit: " + size);
        }
    }
}

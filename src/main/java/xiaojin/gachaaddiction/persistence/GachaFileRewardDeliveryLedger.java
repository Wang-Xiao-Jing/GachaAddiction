package xiaojin.gachaaddiction.persistence;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.world.item.ItemStack;
import xiaojin.gachaaddiction.cardpool.GachaRewardDeliveryMode;
import xiaojin.gachaaddiction.cardpool.delivery.IGachaRewardDeliveryLedger;
import xiaojin.gachaaddiction.cardpool.delivery.GachaRewardDeliveryLedgerEntry;
import xiaojin.gachaaddiction.cardpool.delivery.GachaRewardDeliveryLedgerStage;
import xiaojin.gachaaddiction.cardpool.delivery.GachaRewardDeliveryAttempt;
import xiaojin.gachaaddiction.cardpool.transaction.GachaTransactionId;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.*;

public class GachaFileRewardDeliveryLedger implements IGachaRewardDeliveryLedger {
    public static final int FORMAT_VERSION = 4;
    private static final int PRE_CHECKPOINT_FORMAT_VERSION = 3;

    private static final String FILE_EXTENSION = ".nbt";
    private static final String TEMP_FILE_PREFIX = "delivery-";
    private static final String TEMP_FILE_SUFFIX = ".tmp";
    private static final String FORMAT_VERSION_KEY = "format_version";
    private static final String TRANSACTION_ID_KEY = "transaction_id";
    private static final String PLAYER_ID_KEY = "player_id";
    private static final String MODE_KEY = "mode";
    private static final String STAGE_KEY = "stage";
    private static final String REWARDS_KEY = "rewards";
    private static final String REMAINDER_COUNTS_KEY = "remainder_counts";
    private static final String REMAINDER_COUNT_KEY = "count";
    private static final String RETURN_REWARDS_KEY = "return_rewards";
    private static final String RETURN_REWARDS_RECORDED_KEY = "return_rewards_recorded";
    private static final String DELIVERY_PROGRESS_RECORDED_KEY = "delivery_progress_recorded";
    private static final String ATTEMPT_KEY = "attempt";
    private static final String INBOX_ENTRY_ID_KEY = "inbox_entry_id";

    private final Path root;
    private final int maximumEntryBytes;
    private final HolderLookup.Provider registries;

    public GachaFileRewardDeliveryLedger(Path root, int maximumEntryBytes,
                                         HolderLookup.Provider registries) throws IOException {
        this.root = Objects.requireNonNull(root, "root").toAbsolutePath().normalize();
        if (maximumEntryBytes <= 0) {
            throw new IllegalArgumentException("Maximum delivery ledger entry size must be positive");
        }
        this.maximumEntryBytes = maximumEntryBytes;
        this.registries = Objects.requireNonNull(registries, "registries");
        Files.createDirectories(this.root);
    }

    @Override
    public synchronized Optional<GachaRewardDeliveryLedgerEntry> find(UUID playerId,
                                                                      GachaTransactionId transactionId) {
        try {
            Path target = entryPath(playerId, transactionId);
            return Files.exists(target) ? Optional.of(read(target)) : Optional.empty();
        } catch (IOException failure) {
            throw new UncheckedIOException("Unable to read gacha reward delivery ledger entry", failure);
        }
    }

    @Override
    public synchronized List<GachaRewardDeliveryLedgerEntry> pending(UUID playerId) {
        Path directory = playerDirectory(Objects.requireNonNull(playerId, "playerId"));
        if (!Files.isDirectory(directory)) {
            return List.of();
        }
        try (var files = Files.list(directory)) {
            List<GachaRewardDeliveryLedgerEntry> entries = new ArrayList<>();
            for (Path path : files.filter(this::isEntryFile).sorted().toList()) {
                GachaRewardDeliveryLedgerEntry entry = read(path);
                if (entry.stage() != GachaRewardDeliveryLedgerStage.DELIVERED) {
                    entries.add(entry);
                }
            }
            return List.copyOf(entries);
        } catch (IOException failure) {
            throw new UncheckedIOException("Unable to list pending gacha reward delivery entries", failure);
        }
    }

    @Override
    public synchronized void store(GachaRewardDeliveryLedgerEntry entry) {
        try {
            write(Objects.requireNonNull(entry, "entry"));
        } catch (IOException failure) {
            throw new UncheckedIOException("Unable to store gacha reward delivery ledger entry", failure);
        }
    }

    private void write(GachaRewardDeliveryLedgerEntry entry) throws IOException {
        byte[] encoded = encode(entry);
        requireSize(encoded.length);
        Path target = entryPath(entry.playerId(), entry.transactionId());
        Files.createDirectories(target.getParent());
        Path temporary = Files.createTempFile(target.getParent(), TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX);
        try {
            try (FileChannel channel = FileChannel.open(temporary, StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING)) {
                ByteBuffer buffer = ByteBuffer.wrap(encoded);
                while (buffer.hasRemaining()) {
                    int written = channel.write(buffer);
                    if (written <= 0) {
                        throw new IOException("Unable to write reward delivery ledger entry");
                    }
                }
                channel.force(true);
            }
            try {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException unsupported) {
                throw new IOException("Reward delivery ledger requires atomic replacement", unsupported);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private byte[] encode(GachaRewardDeliveryLedgerEntry entry) throws IOException {
        CompoundTag encoded = new CompoundTag();
        encoded.putInt(FORMAT_VERSION_KEY, FORMAT_VERSION);
        encoded.putUUID(TRANSACTION_ID_KEY, entry.transactionId().value());
        encoded.putUUID(PLAYER_ID_KEY, entry.playerId());
        encoded.putString(MODE_KEY, entry.mode().name());
        encoded.putString(STAGE_KEY, entry.stage().name());
        ListTag rewards = new ListTag();
        entry.rewards().forEach(stack -> rewards.add(stack.save(registries)));
        encoded.put(REWARDS_KEY, rewards);
        ListTag remainderCounts = new ListTag();
        for (int remainderCount : entry.remainderCounts()) {
            CompoundTag encodedCount = new CompoundTag();
            encodedCount.putInt(REMAINDER_COUNT_KEY, remainderCount);
            remainderCounts.add(encodedCount);
        }
        encoded.put(REMAINDER_COUNTS_KEY, remainderCounts);
        ListTag returnRewards = new ListTag();
        entry.returnRewards().forEach(stack -> returnRewards.add(stack.save(registries)));
        encoded.put(RETURN_REWARDS_KEY, returnRewards);
        encoded.putBoolean(RETURN_REWARDS_RECORDED_KEY, entry.returnRewardsRecorded());
        encoded.putBoolean(DELIVERY_PROGRESS_RECORDED_KEY, entry.deliveryProgressRecorded());
        entry.attempt().ifPresent(attempt -> encoded.put(ATTEMPT_KEY, attempt.encode()));
        entry.inboxEntryId().ifPresent(id -> encoded.putUUID(INBOX_ENTRY_ID_KEY, id.value()));
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream output = new DataOutputStream(bytes)) {
            NbtIo.write(encoded, output);
        }
        return bytes.toByteArray();
    }

    private GachaRewardDeliveryLedgerEntry read(Path path) throws IOException {
        requireSize(Files.size(path));
        byte[] bytes = Files.readAllBytes(path);
        CompoundTag encoded;
        try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(bytes))) {
            encoded = NbtIo.read(input, NbtAccounter.create(maximumEntryBytes));
        }
        int formatVersion = encoded.getInt(FORMAT_VERSION_KEY);
        if (formatVersion != FORMAT_VERSION && formatVersion != PRE_CHECKPOINT_FORMAT_VERSION) {
            throw new IOException("Unsupported reward delivery ledger entry version: "
                    + formatVersion);
        }
        if (formatVersion == PRE_CHECKPOINT_FORMAT_VERSION
                && (encoded.contains(ATTEMPT_KEY) || encoded.contains(INBOX_ENTRY_ID_KEY))) {
            throw new IOException("Delivery receipt metadata requires ledger version " + FORMAT_VERSION);
        }
        UUID playerId = encoded.getUUID(PLAYER_ID_KEY);
        GachaTransactionId transactionId = new GachaTransactionId(encoded.getUUID(TRANSACTION_ID_KEY));
        if (!path.equals(entryPath(playerId, transactionId))) {
            throw new IOException("Reward delivery ledger path does not match its payload");
        }
        List<ItemStack> rewards = new ArrayList<>();
        ListTag encodedRewards = encoded.getList(REWARDS_KEY, Tag.TAG_COMPOUND);
        for (int index = 0; index < encodedRewards.size(); index++) {
            rewards.add(ItemStack.parseOptional(registries, encodedRewards.getCompound(index)));
        }
        List<Integer> remainderCounts = new ArrayList<>();
        ListTag encodedRemainderCounts = encoded.getList(REMAINDER_COUNTS_KEY, Tag.TAG_COMPOUND);
        for (int index = 0; index < encodedRemainderCounts.size(); index++) {
            remainderCounts.add(encodedRemainderCounts.getCompound(index).getInt(REMAINDER_COUNT_KEY));
        }
        List<ItemStack> returnRewards = new ArrayList<>();
        ListTag encodedReturnRewards = encoded.getList(RETURN_REWARDS_KEY, Tag.TAG_COMPOUND);
        for (int index = 0; index < encodedReturnRewards.size(); index++) {
            returnRewards.add(ItemStack.parseOptional(registries, encodedReturnRewards.getCompound(index)));
        }
        boolean returnRewardsRecorded = encoded.getBoolean(RETURN_REWARDS_RECORDED_KEY);
        boolean deliveryProgressRecorded = encoded.getBoolean(DELIVERY_PROGRESS_RECORDED_KEY);
        try {
            Optional<GachaRewardDeliveryAttempt> attempt = Optional.empty();
            if (encoded.contains(ATTEMPT_KEY)) {
                if (!encoded.contains(ATTEMPT_KEY, Tag.TAG_COMPOUND)) {
                    throw new IllegalArgumentException("Delivery attempt must be a compound");
                }
                attempt = Optional.of(GachaRewardDeliveryAttempt.decode(encoded.getCompound(ATTEMPT_KEY)));
            }
            Optional<GachaTransactionId> inboxEntryId = Optional.empty();
            if (encoded.contains(INBOX_ENTRY_ID_KEY)) {
                if (!encoded.hasUUID(INBOX_ENTRY_ID_KEY)) {
                    throw new IllegalArgumentException("Inbox entry identity must be a UUID");
                }
                inboxEntryId = Optional.of(new GachaTransactionId(encoded.getUUID(INBOX_ENTRY_ID_KEY)));
            }
            GachaRewardDeliveryMode mode = GachaRewardDeliveryMode.valueOf(encoded.getString(MODE_KEY));
            GachaRewardDeliveryLedgerStage stage = GachaRewardDeliveryLedgerStage.valueOf(
                    encoded.getString(STAGE_KEY));
            return new GachaRewardDeliveryLedgerEntry(transactionId, playerId, mode, stage,
                    rewards, remainderCounts, deliveryProgressRecorded,
                    returnRewards, returnRewardsRecorded, attempt, inboxEntryId);
        } catch (IllegalArgumentException failure) {
            throw new IOException("Invalid reward delivery ledger entry", failure);
        }
    }

    private Path entryPath(UUID playerId, GachaTransactionId transactionId) {
        return playerDirectory(playerId).resolve(Objects.requireNonNull(transactionId, "transactionId").value()
                + FILE_EXTENSION);
    }

    private Path playerDirectory(UUID playerId) {
        return root.resolve(Objects.requireNonNull(playerId, "playerId").toString());
    }

    private boolean isEntryFile(Path path) {
        return Files.isRegularFile(path) && path.getFileName().toString().endsWith(FILE_EXTENSION);
    }

    private void requireSize(long size) throws IOException {
        if (size <= 0 || size > maximumEntryBytes) {
            throw new IOException("Reward delivery ledger entry size is outside the configured limit: " + size);
        }
    }
}

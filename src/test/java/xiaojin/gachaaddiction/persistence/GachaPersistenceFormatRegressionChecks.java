package xiaojin.gachaaddiction.persistence;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.GachaConstants;
import xiaojin.gachaaddiction.cardpool.GachaRewardDeliveryMode;
import xiaojin.gachaaddiction.cardpool.delivery.GachaRewardDeliveryLedgerEntry;
import xiaojin.gachaaddiction.cardpool.delivery.GachaRewardDeliveryAttempt;
import xiaojin.gachaaddiction.cardpool.delivery.GachaRewardDeliveryReceipt;
import xiaojin.gachaaddiction.cardpool.delivery.GachaRewardDropSnapshot;
import xiaojin.gachaaddiction.cardpool.delivery.GachaPendingRewardDrops;
import xiaojin.gachaaddiction.cardpool.delivery.GachaRewardInboxEntry;
import xiaojin.gachaaddiction.cardpool.delivery.GachaRewardInboxEntryKind;
import xiaojin.gachaaddiction.cardpool.delivery.GachaRewardInboxClaim;
import xiaojin.gachaaddiction.cardpool.transaction.GachaTransactionContext;
import xiaojin.gachaaddiction.cardpool.transaction.GachaTransactionId;
import xiaojin.gachaaddiction.cardpool.transaction.GachaTransactionSource;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterSchema;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public class GachaPersistenceFormatRegressionChecks {
    public static void main(String[] args) throws Exception {
        HolderLookup.Provider registries = HolderLookup.Provider.create(Stream.empty());
        checkTransactionPayload(registries);
        checkSnapshotVersionGuard(registries);
        checkAttemptSnapshots();
        checkClaimIdentity();
        checkSavedEntityProof();
        Path directory = Files.createTempDirectory("gacha-format-regression-");
        try {
            checkInboxAndLedger(directory, registries);
            checkAttemptLedger(directory, registries);
            System.out.println("Persistence format regression checks passed");
        } finally {
            try (var paths = Files.walk(directory)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                    Files.delete(path);
                }
            }
        }
    }

    private static void checkTransactionPayload(HolderLookup.Provider registries) {
        GachaParameterSchema schema = new GachaParameterSchema(List.of());
        GachaTransactionNbtPayloadEncoder encoder = new GachaTransactionNbtPayloadEncoder(registries,
                source -> schema);
        GachaTransactionNbtPayloadDecoder decoder = new GachaTransactionNbtPayloadDecoder(registries,
                source -> schema);
        GachaTransactionContext context = new GachaTransactionContext(
                new GachaTransactionId(UUID.randomUUID()), UUID.randomUUID(),
                new GachaTransactionSource(ResourceLocation.fromNamespaceAndPath("gacha_test", "format_check"),
                        Optional.empty(), new CompoundTag()));
        CompoundTag current = encoder.prepared(context, List.of());
        require(decoder.decode(current).context().equals(context), "Current transaction payload did not round-trip");
        for (int version : new int[]{0, 1, GachaTransactionNbtPayloadEncoder.PAYLOAD_VERSION + 1}) {
            CompoundTag unsupported = current.copy();
            unsupported.putInt("payload_version", version);
            expectRejected(() -> decoder.decode(unsupported), "Unsupported transaction payload version: " + version);
        }
    }

    private static void checkSnapshotVersionGuard(HolderLookup.Provider registries) {
        GachaCardPoolNbtSnapshotCodec codec = new GachaCardPoolNbtSnapshotCodec(registries, id -> Optional.empty());
        for (int version : new int[]{0, 3, 4, GachaCardPoolNbtSnapshotCodec.FORMAT_VERSION + 1}) {
            CompoundTag unsupported = new CompoundTag();
            unsupported.putInt("format_version", version);
            expectRejected(() -> codec.decode(unsupported), "Unsupported card pool snapshot version: " + version);
        }
    }

    private static void checkInboxAndLedger(Path directory, HolderLookup.Provider registries) throws IOException {
        UUID playerId = UUID.randomUUID();
        GachaTransactionId transactionId = new GachaTransactionId(UUID.randomUUID());
        Path inboxRoot = directory.resolve("inbox");
        GachaFileRewardInbox inbox = new GachaFileRewardInbox(inboxRoot, 4096, registries);
        GachaRewardInboxEntry inboxEntry = new GachaRewardInboxEntry(transactionId, playerId,
                GachaRewardInboxEntryKind.REWARD, List.of());
        inbox.restore(inboxEntry);
        require(inbox.find(playerId, transactionId).orElseThrow().equals(inboxEntry),
                "Current inbox entry did not round-trip");
        Path inboxFile = inboxRoot.resolve(playerId.toString()).resolve(transactionId.value() + ".nbt");
        overwriteVersion(inboxFile, 1);
        expectRejectedIo(() -> inbox.find(playerId, transactionId), "Unsupported reward inbox entry version: 1");

        Path ledgerRoot = directory.resolve("ledger");
        GachaFileRewardDeliveryLedger ledger = new GachaFileRewardDeliveryLedger(ledgerRoot, 4096, registries);
        GachaRewardDeliveryLedgerEntry ledgerEntry = GachaRewardDeliveryLedgerEntry.prepared(transactionId,
                playerId, GachaRewardDeliveryMode.INVENTORY, List.of());
        ledger.store(ledgerEntry);
        require(ledger.find(playerId, transactionId).orElseThrow().equals(ledgerEntry),
                "Current ledger entry did not round-trip");
        Path ledgerFile = ledgerRoot.resolve(playerId.toString()).resolve(transactionId.value() + ".nbt");
        for (int version : new int[]{1, 2}) {
            overwriteVersion(ledgerFile, version);
            expectRejectedIo(() -> ledger.find(playerId, transactionId),
                    "Unsupported reward delivery ledger entry version: " + version);
        }
    }

    private static void checkAttemptSnapshots() {
        GachaRewardDeliveryReceipt receipt = new GachaRewardDeliveryReceipt(UUID.randomUUID(),
                new GachaTransactionId(UUID.randomUUID()), UUID.randomUUID());
        CompoundTag entity = dropEntity(receipt);
        GachaRewardDropSnapshot drop = new GachaRewardDropSnapshot(
                ResourceLocation.withDefaultNamespace("overworld"), entity);
        entity.getCompound("Item").putInt("count", 99);
        require(drop.entityData().getCompound("Item").getInt("count") == 2,
                "Drop snapshot retained mutable entity input");
        CompoundTag exported = drop.entityData();
        exported.getCompound("Item").putInt("count", 98);
        require(drop.entityData().getCompound("Item").getInt("count") == 2,
                "Drop snapshot exposed mutable entity state");

        ListTag inventory = new ListTag();
        CompoundTag slot = new CompoundTag();
        slot.putByte("Slot", (byte)0);
        slot.putString("id", "minecraft:stone");
        slot.putInt("count", 1);
        inventory.add(slot);
        GachaRewardDeliveryAttempt attempt = new GachaRewardDeliveryAttempt(receipt, inventory, List.of(drop));
        inventory.clear();
        attempt.inventory().clear();
        require(attempt.inventory().size() == 1, "Attempt inventory ownership was not isolated");
        require(GachaRewardDeliveryAttempt.decode(attempt.encode()).equals(attempt),
                "Delivery attempt with drop metadata did not round-trip");
        expectRejected(() -> new GachaRewardDeliveryAttempt(receipt, new ListTag(), List.of(drop, drop)),
                "Reward delivery attempt contains a duplicate drop identity");
        GachaRewardDeliveryReceipt other = new GachaRewardDeliveryReceipt(receipt.playerId(),
                receipt.transactionId(), UUID.randomUUID());
        expectRejected(() -> new GachaRewardDeliveryAttempt(other, new ListTag(), List.of(drop)),
                "Reward drop receipt does not match its delivery attempt");
        CompoundTag invalidPosition = drop.entityData();
        ListTag position = new ListTag();
        position.add(DoubleTag.valueOf(Double.NaN));
        position.add(DoubleTag.valueOf(0));
        position.add(DoubleTag.valueOf(0));
        invalidPosition.put("Pos", position);
        expectRejected(() -> new GachaRewardDropSnapshot(drop.dimension(), invalidPosition),
                "Reward drop position must be finite");
    }

    private static void checkAttemptLedger(Path directory, HolderLookup.Provider registries) throws IOException {
        UUID playerId = UUID.randomUUID();
        GachaTransactionId transactionId = new GachaTransactionId(UUID.randomUUID());
        GachaTransactionId inboxEntryId = new GachaTransactionId(UUID.randomUUID());
        GachaRewardDeliveryReceipt receipt = new GachaRewardDeliveryReceipt(playerId, transactionId, UUID.randomUUID());
        GachaRewardDeliveryAttempt attempt = new GachaRewardDeliveryAttempt(receipt, new ListTag(), List.of());
        GachaRewardDeliveryLedgerEntry prepared = GachaRewardDeliveryLedgerEntry.prepared(
                transactionId, playerId, GachaRewardDeliveryMode.INVENTORY, List.of()).forInboxClaim(inboxEntryId);
        GachaRewardDeliveryLedgerEntry applying = prepared.applying(attempt, List.of());
        GachaRewardDeliveryLedgerEntry online = applying.onlineApplied(List.of());
        GachaRewardDeliveryLedgerEntry delivered = online.delivered();
        require(delivered.attempt().orElseThrow().equals(attempt), "Committed entry lost its attempt identity");
        require(delivered.inboxEntryId().orElseThrow().equals(inboxEntryId), "Committed entry lost its inbox source");
        expectStateRejected(() -> applying.applyingProgress(List.of()));
        expectStateRejected(applying::returnPending);
        expectStateRejected(() -> applying.onlineApplied(List.of(1)));

        Path root = directory.resolve("attempt-ledger");
        GachaFileRewardDeliveryLedger ledger = new GachaFileRewardDeliveryLedger(root, 4096, registries);
        for (GachaRewardDeliveryLedgerEntry entry : List.of(prepared, applying, online, delivered)) {
            ledger.store(entry);
            require(ledger.find(playerId, transactionId).orElseThrow().equals(entry),
                    "Ledger lost receipt or inbox metadata while round-tripping");
        }
        require(ledger.pending(playerId).isEmpty(), "Committed delivery remained pending");
        Path file = root.resolve(playerId.toString()).resolve(transactionId.value() + ".nbt");
        require(readLedger(file).getInt("format_version") == 4, "New ledger write did not use receipt format");
        overwriteVersion(file, 3);
        expectRejectedIo(() -> ledger.find(playerId, transactionId), "Delivery receipt metadata requires ledger version 4");

        GachaRewardDeliveryLedgerEntry noReceipt = GachaRewardDeliveryLedgerEntry.prepared(
                transactionId, playerId, GachaRewardDeliveryMode.INVENTORY, List.of()).applying();
        ledger.store(noReceipt);
        overwriteVersion(file, 3);
        GachaRewardDeliveryLedgerEntry oldEntry = ledger.find(playerId, transactionId).orElseThrow();
        require(oldEntry.equals(noReceipt) && oldEntry.attempt().isEmpty(), "Old progress gained a fabricated receipt");
        require(readLedger(file).getInt("format_version") == 3, "Reading old ledger rewrote its file");
        overwriteVersion(file, GachaFileRewardDeliveryLedger.FORMAT_VERSION + 1);
        expectRejectedIo(() -> ledger.find(playerId, transactionId), "Unsupported reward delivery ledger entry version: 5");
    }

    private static CompoundTag dropEntity(GachaRewardDeliveryReceipt receipt) {
        CompoundTag entity = new CompoundTag();
        entity.putString("id", "minecraft:item");
        entity.putUUID("UUID", UUID.randomUUID());
        ListTag position = new ListTag();
        position.add(DoubleTag.valueOf(0));
        position.add(DoubleTag.valueOf(64));
        position.add(DoubleTag.valueOf(0));
        entity.put("Pos", position);
        CompoundTag item = new CompoundTag();
        item.putString("id", "minecraft:stone");
        item.putInt("count", 2);
        entity.put("Item", item);
        CompoundTag modData = new CompoundTag();
        modData.put(GachaRewardDropSnapshot.PENDING_RECEIPT_KEY, receipt.encode());
        CompoundTag persistentData = new CompoundTag();
        persistentData.put(GachaConstants.MODID, modData);
        entity.put("NeoForgeData", persistentData);
        return entity;
    }

    private static void checkClaimIdentity() {
        UUID playerId = UUID.randomUUID();
        GachaTransactionId sourceId = new GachaTransactionId(UUID.randomUUID());
        GachaTransactionId claimId = GachaRewardInboxClaim.transactionIdFor(playerId, sourceId);
        require(claimId.equals(GachaRewardInboxClaim.transactionIdFor(playerId, sourceId)),
                "Repeated inbox request did not retain its claim identity");
        require(!claimId.equals(GachaRewardInboxClaim.transactionIdFor(UUID.randomUUID(), sourceId)),
                "Different players shared a claim identity");
        require(!claimId.equals(GachaRewardInboxClaim.transactionIdFor(playerId,
                new GachaTransactionId(UUID.randomUUID()))), "Different inbox entries shared a claim identity");
    }

    private static void checkSavedEntityProof() throws IOException {
        GachaRewardDeliveryReceipt receipt = new GachaRewardDeliveryReceipt(UUID.randomUUID(),
                new GachaTransactionId(UUID.randomUUID()), UUID.randomUUID());
        GachaRewardDropSnapshot drop = new GachaRewardDropSnapshot(
                ResourceLocation.withDefaultNamespace("overworld"), dropEntity(receipt));
        CompoundTag saved = savedEntityChunk(drop.entityData());
        GachaEntitySaveCheckpoint.verifySavedChunk(saved, drop);
        require(GachaEntitySaveCheckpoint.containsSavedEntity(saved, drop), "Saved drop could not be found");
        require(GachaPendingRewardDrops.isPending(drop.entityData().getCompound("NeoForgeData")),
                "Pending entity marker was not protected");
        require(!GachaPendingRewardDrops.isPending(new CompoundTag()), "Ordinary entity was treated as pending");

        CompoundTag missing = saved.copy();
        missing.put("Entities", new ListTag());
        require(!GachaEntitySaveCheckpoint.containsSavedEntity(missing, drop), "Missing drop was treated as saved");
        expectUnconfirmedEntity(() -> GachaEntitySaveCheckpoint.verifySavedChunk(missing, drop));
        CompoundTag misplaced = saved.copy();
        misplaced.putIntArray("Position", new int[]{1, 0});
        expectUnconfirmedEntity(() -> GachaEntitySaveCheckpoint.verifySavedChunk(misplaced, drop));
        CompoundTag changedItem = drop.entityData();
        changedItem.getCompound("Item").putInt("count", 1);
        expectUnconfirmedEntity(() -> GachaEntitySaveCheckpoint.verifySavedChunk(savedEntityChunk(changedItem), drop));
        CompoundTag changedReceipt = drop.entityData();
        changedReceipt.getCompound("NeoForgeData").getCompound(GachaConstants.MODID)
                .getCompound(GachaRewardDropSnapshot.PENDING_RECEIPT_KEY).putUUID("attempt_id", UUID.randomUUID());
        expectUnconfirmedEntity(() -> GachaEntitySaveCheckpoint.verifySavedChunk(savedEntityChunk(changedReceipt), drop));
        CompoundTag duplicate = saved.copy();
        duplicate.getList("Entities", 10).add(drop.entityData());
        expectUnconfirmedEntity(() -> GachaEntitySaveCheckpoint.verifySavedChunk(duplicate, drop));
        CompoundTag invalid = drop.entityData();
        invalid.remove("Pos");
        expectUnconfirmedEntity(() -> GachaEntitySaveCheckpoint.verifySavedChunk(savedEntityChunk(invalid), drop));
    }

    private static CompoundTag savedEntityChunk(CompoundTag entity) {
        CompoundTag chunk = new CompoundTag();
        chunk.putIntArray("Position", new int[]{0, 0});
        ListTag entities = new ListTag();
        entities.add(entity.copy());
        chunk.put("Entities", entities);
        return chunk;
    }

    private static void expectUnconfirmedEntity(SavedEntityCheck action) {
        try {
            action.run();
        } catch (IOException expected) {
            return;
        }

        throw new AssertionError("Unconfirmed reward entity snapshot was accepted");
    }

    private interface SavedEntityCheck {
        void run() throws IOException;
    }

    private static CompoundTag readLedger(Path file) throws IOException {
        try (DataInputStream input = new DataInputStream(Files.newInputStream(file))) {
            return NbtIo.read(input, NbtAccounter.create(4096));
        }
    }

    private static void expectStateRejected(Runnable action) {
        try {
            action.run();
        } catch (IllegalStateException expected) {
            return;
        }

        throw new AssertionError("Unreconciled attempt changed its planned disposition");
    }

    private static void overwriteVersion(Path file, int version) throws IOException {
        CompoundTag data;
        try (DataInputStream input = new DataInputStream(Files.newInputStream(file))) {
            data = NbtIo.read(input, NbtAccounter.create(4096));
        }
        data.putInt("format_version", version);
        try (DataOutputStream output = new DataOutputStream(Files.newOutputStream(file))) {
            NbtIo.write(data, output);
        }
    }

    private static void expectRejected(Runnable action, String message) {
        try {
            action.run();
        } catch (IllegalArgumentException failure) {
            require(message.equals(failure.getMessage()), "Unexpected rejection: " + failure);
            return;
        }
        throw new AssertionError("Unsupported version was accepted: " + message);
    }

    private static void expectRejectedIo(Runnable action, String message) {
        try {
            action.run();
        } catch (UncheckedIOException failure) {
            require(message.equals(failure.getCause().getMessage()), "Unexpected IO rejection: " + failure);
            return;
        }
        throw new AssertionError("Unsupported version was accepted: " + message);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}

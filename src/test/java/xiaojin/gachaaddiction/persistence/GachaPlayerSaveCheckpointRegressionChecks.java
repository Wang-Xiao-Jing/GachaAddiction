package xiaojin.gachaaddiction.persistence;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import xiaojin.gachaaddiction.GachaConstants;
import xiaojin.gachaaddiction.cardpool.delivery.GachaRewardDeliveryReceipt;
import xiaojin.gachaaddiction.cardpool.transaction.GachaTransactionId;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.UUID;
import java.util.List;

public class GachaPlayerSaveCheckpointRegressionChecks {
    public static void main(String[] args) throws Exception {
        checkReceiptIdentityAndOwnership();
        checkSnapshotRequiresInventoryAndReceipt();
        checkReceiptFormat();
        Path directory = Files.createTempDirectory("gacha-player-checkpoint-regression-");
        try {
            checkSavedSnapshots(directory);
            System.out.println("Player save checkpoint regression checks passed");
        } finally {
            try (var paths = Files.walk(directory)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                    Files.delete(path);
                }
            }
        }
    }

    private static void checkReceiptIdentityAndOwnership() {
        GachaRewardDeliveryReceipt receipt = receipt();
        require(GachaRewardDeliveryReceipt.decode(receipt.encode()).equals(receipt), "Receipt did not round-trip");
        CompoundTag persistentData = new CompoundTag();
        persistentData.putString("other_mod", "preserved");
        CompoundTag modData = new CompoundTag();
        modData.putInt("unrelated", 7);
        persistentData.put(GachaConstants.MODID, modData);
        receipt.writeTo(persistentData);
        require(receipt.isPresentIn(persistentData), "Written receipt was not found");
        require(GachaRewardDeliveryReceipt.all(persistentData).equals(List.of(receipt)),
                "Receipt enumeration did not preserve the stored identity");
        require(!new GachaRewardDeliveryReceipt(UUID.randomUUID(), receipt.transactionId(), receipt.attemptId())
                .isPresentIn(persistentData), "Receipt accepted another player");
        require(!new GachaRewardDeliveryReceipt(receipt.playerId(), receipt.transactionId(), UUID.randomUUID())
                .isPresentIn(persistentData), "Receipt accepted another delivery attempt");
        require(GachaRewardDeliveryReceipt.find(persistentData,
                new GachaTransactionId(UUID.randomUUID())).isEmpty(), "Receipt accepted another transaction");

        GachaRewardDeliveryReceipt replacement = new GachaRewardDeliveryReceipt(receipt.playerId(),
                receipt.transactionId(), UUID.randomUUID());
        replacement.writeTo(persistentData);
        receipt.removeFrom(persistentData);
        require(replacement.isPresentIn(persistentData), "Old attempt removed the replacement receipt");
        GachaRewardDeliveryReceipt another = receipt();
        another.writeTo(persistentData);
        require(GachaRewardDeliveryReceipt.all(persistentData).size() == 2,
                "Receipt enumeration lost an independent transaction");
        replacement.removeFrom(persistentData);
        require(another.isPresentIn(persistentData), "Cleanup removed another transaction receipt");
        another.removeFrom(persistentData);
        require(GachaRewardDeliveryReceipt.all(persistentData).isEmpty(), "Committed receipt cleanup left stale entries");
        require(persistentData.getString("other_mod").equals("preserved"), "Receipt cleanup changed another mod's data");
        require(persistentData.getCompound(GachaConstants.MODID).getInt("unrelated") == 7,
                "Receipt cleanup changed unrelated gacha data");
        require(GachaRewardDeliveryReceipt.find(persistentData, receipt.transactionId()).isEmpty(),
                "Removed receipt was retained");
    }

    private static void checkSnapshotRequiresInventoryAndReceipt() {
        GachaRewardDeliveryReceipt receipt = receipt();
        ListTag inventory = inventory();
        CompoundTag snapshot = playerSnapshot(receipt, inventory);
        require(receipt.matchesPlayerSnapshot(snapshot, inventory), "Matching player snapshot was rejected");

        CompoundTag wrongPlayer = snapshot.copy();
        wrongPlayer.putUUID("UUID", UUID.randomUUID());
        require(!receipt.matchesPlayerSnapshot(wrongPlayer, inventory), "Saved UUID was not checked");
        CompoundTag wrongInventory = snapshot.copy();
        ListTag otherInventory = inventory.copy();
        otherInventory.getCompound(0).putInt("count", 1);
        wrongInventory.put("Inventory", otherInventory);
        require(!receipt.matchesPlayerSnapshot(wrongInventory, inventory), "Saved inventory was not checked");
        CompoundTag missingInventory = snapshot.copy();
        missingInventory.remove("Inventory");
        require(!receipt.matchesPlayerSnapshot(missingInventory, new ListTag()), "Missing inventory was treated as empty");
        ListTag wrongElementType = new ListTag();
        wrongElementType.add(IntTag.valueOf(1));
        missingInventory.put("Inventory", wrongElementType);
        require(!receipt.matchesPlayerSnapshot(missingInventory, new ListTag()),
                "Non-compound inventory list was coerced to an empty inventory");

        CompoundTag oldMarkerOnly = snapshot.copy();
        CompoundTag oldPending = new CompoundTag();
        oldPending.putBoolean(receipt.transactionId().value().toString(), true);
        CompoundTag modData = new CompoundTag();
        modData.put("pending_reward_deliveries", oldPending);
        CompoundTag persistentData = new CompoundTag();
        persistentData.put(GachaConstants.MODID, modData);
        oldMarkerOnly.put("NeoForgeData", persistentData);
        require(!receipt.matchesPlayerSnapshot(oldMarkerOnly, inventory), "Old pending boolean was accepted as a receipt");
    }

    private static void checkReceiptFormat() {
        GachaRewardDeliveryReceipt receipt = receipt();
        CompoundTag encoded = receipt.encode();
        encoded.putInt("format_version", 2);
        expectInvalid(() -> GachaRewardDeliveryReceipt.decode(encoded));
        CompoundTag incomplete = receipt.encode();
        incomplete.remove("attempt_id");
        expectInvalid(() -> GachaRewardDeliveryReceipt.decode(incomplete));

        CompoundTag persistentData = new CompoundTag();
        receipt.writeTo(persistentData);
        persistentData.getCompound(GachaConstants.MODID).getCompound("reward_delivery_receipts")
                .getCompound(receipt.transactionId().value().toString()).putUUID("transaction_id", UUID.randomUUID());
        expectInvalid(() -> GachaRewardDeliveryReceipt.find(persistentData, receipt.transactionId()));
        expectInvalid(() -> GachaRewardDeliveryReceipt.all(persistentData));
    }

    private static void checkSavedSnapshots(Path directory) throws IOException {
        GachaRewardDeliveryReceipt receipt = receipt();
        ListTag inventory = inventory();
        CompoundTag snapshot = playerSnapshot(receipt, inventory);
        Path playerFile = directory.resolve(receipt.playerId() + ".dat");
        Path levelFile = directory.resolve("level.dat");
        NbtIo.writeCompressed(snapshot, playerFile);
        writeLevel(levelFile, snapshot);
        GachaPlayerSaveCheckpoint.verifyPlayerFile(playerFile, receipt, inventory);
        GachaPlayerSaveCheckpoint.verifyOwnerFiles(playerFile, levelFile, receipt, inventory);

        GachaRewardDeliveryReceipt earlier = new GachaRewardDeliveryReceipt(receipt.playerId(),
                receipt.transactionId(), UUID.randomUUID());
        writeLevel(levelFile, playerSnapshot(earlier, inventory));
        expectUnconfirmed(() -> GachaPlayerSaveCheckpoint.verifyOwnerFiles(
                playerFile, levelFile, receipt, inventory));
        GachaPlayerSaveCheckpoint.verifyPlayerFile(playerFile, receipt, inventory);

        writeLevel(levelFile, snapshot);
        NbtIo.writeCompressed(playerSnapshot(earlier, inventory), playerFile);
        expectUnconfirmed(() -> GachaPlayerSaveCheckpoint.verifyOwnerFiles(
                playerFile, levelFile, receipt, inventory));

        NbtIo.writeCompressed(snapshot, directory.resolve(receipt.playerId() + ".dat_old"));
        Files.delete(playerFile);
        expectUnconfirmed(() -> GachaPlayerSaveCheckpoint.verifyPlayerFile(playerFile, receipt, inventory));
        expectUnconfirmed(() -> GachaPlayerSaveCheckpoint.verifyOwnerFiles(
                playerFile, levelFile, receipt, inventory));
    }

    private static GachaRewardDeliveryReceipt receipt() {
        return new GachaRewardDeliveryReceipt(UUID.randomUUID(),
                new GachaTransactionId(UUID.randomUUID()), UUID.randomUUID());
    }

    private static ListTag inventory() {
        CompoundTag stack = new CompoundTag();
        stack.putByte("Slot", (byte)0);
        stack.putString("id", "minecraft:stone");
        stack.putInt("count", 2);
        ListTag inventory = new ListTag();
        inventory.add(stack);
        return inventory;
    }

    private static CompoundTag playerSnapshot(GachaRewardDeliveryReceipt receipt, ListTag inventory) {
        CompoundTag snapshot = new CompoundTag();
        snapshot.putUUID("UUID", receipt.playerId());
        snapshot.put("Inventory", inventory.copy());
        CompoundTag persistentData = new CompoundTag();
        receipt.writeTo(persistentData);
        snapshot.put("NeoForgeData", persistentData);
        return snapshot;
    }

    private static void writeLevel(Path levelFile, CompoundTag playerSnapshot) throws IOException {
        CompoundTag data = new CompoundTag();
        data.put("Player", playerSnapshot.copy());
        CompoundTag level = new CompoundTag();
        level.put("Data", data);
        NbtIo.writeCompressed(level, levelFile);
    }

    private static void expectInvalid(Runnable action) {
        try {
            action.run();
        } catch (IllegalArgumentException expected) {
            return;
        }

        throw new AssertionError("Invalid delivery receipt was accepted");
    }

    private static void expectUnconfirmed(IoCheck action) {
        try {
            action.run();
        } catch (IOException expected) {
            return;
        }

        throw new AssertionError("Unconfirmed player snapshot was accepted");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private interface IoCheck {
        void run() throws IOException;
    }
}

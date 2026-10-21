package xiaojin.gachaaddiction.cardpool.delivery;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import xiaojin.gachaaddiction.GachaConstants;
import xiaojin.gachaaddiction.cardpool.transaction.GachaTransactionId;

import java.util.Objects;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record GachaRewardDeliveryReceipt(UUID playerId, GachaTransactionId transactionId, UUID attemptId) {
    private static final int FORMAT_VERSION = 1;
    private static final String RECEIPTS_KEY = "reward_delivery_receipts";
    private static final String FORMAT_VERSION_KEY = "format_version";
    private static final String PLAYER_ID_KEY = "player_id";
    private static final String TRANSACTION_ID_KEY = "transaction_id";
    private static final String ATTEMPT_ID_KEY = "attempt_id";
    private static final String PERSISTENT_DATA_KEY = "NeoForgeData";
    private static final String PLAYER_UUID_KEY = "UUID";
    private static final String INVENTORY_KEY = "Inventory";

    public GachaRewardDeliveryReceipt {
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(transactionId, "transactionId");
        Objects.requireNonNull(attemptId, "attemptId");
    }

    public CompoundTag encode() {
        CompoundTag encoded = new CompoundTag();
        encoded.putInt(FORMAT_VERSION_KEY, FORMAT_VERSION);
        encoded.putUUID(PLAYER_ID_KEY, playerId);
        encoded.putUUID(TRANSACTION_ID_KEY, transactionId.value());
        encoded.putUUID(ATTEMPT_ID_KEY, attemptId);
        return encoded;
    }

    public static GachaRewardDeliveryReceipt decode(CompoundTag encoded) {
        Objects.requireNonNull(encoded, "encoded");
        if (!encoded.contains(FORMAT_VERSION_KEY, Tag.TAG_INT)
                || encoded.getInt(FORMAT_VERSION_KEY) != FORMAT_VERSION) {
            throw new IllegalArgumentException("Unsupported reward delivery receipt version");
        }

        if (!encoded.hasUUID(PLAYER_ID_KEY) || !encoded.hasUUID(TRANSACTION_ID_KEY)
                || !encoded.hasUUID(ATTEMPT_ID_KEY)) {
            throw new IllegalArgumentException("Reward delivery receipt identity is incomplete");
        }

        return new GachaRewardDeliveryReceipt(encoded.getUUID(PLAYER_ID_KEY),
                new GachaTransactionId(encoded.getUUID(TRANSACTION_ID_KEY)), encoded.getUUID(ATTEMPT_ID_KEY));
    }

    public void writeTo(CompoundTag persistentData) {
        Objects.requireNonNull(persistentData, "persistentData");
        CompoundTag modData = persistentData.getCompound(GachaConstants.MODID);
        CompoundTag receipts = modData.getCompound(RECEIPTS_KEY);
        receipts.put(transactionId.value().toString(), encode());
        modData.put(RECEIPTS_KEY, receipts);
        persistentData.put(GachaConstants.MODID, modData);
    }

    public static Optional<GachaRewardDeliveryReceipt> find(CompoundTag persistentData,
                                                           GachaTransactionId transactionId) {
        Objects.requireNonNull(persistentData, "persistentData");
        Objects.requireNonNull(transactionId, "transactionId");
        CompoundTag receipts = persistentData.getCompound(GachaConstants.MODID).getCompound(RECEIPTS_KEY);
        String key = transactionId.value().toString();
        if (!receipts.contains(key)) {
            return Optional.empty();
        }

        if (!receipts.contains(key, Tag.TAG_COMPOUND)) {
            throw new IllegalArgumentException("Reward delivery receipt must be a compound");
        }

        GachaRewardDeliveryReceipt receipt = decode(receipts.getCompound(key));
        if (!receipt.transactionId().equals(transactionId)) {
            throw new IllegalArgumentException("Reward delivery receipt key does not match its transaction");
        }

        return Optional.of(receipt);
    }

    public static List<GachaRewardDeliveryReceipt> all(CompoundTag persistentData) {
        Objects.requireNonNull(persistentData, "persistentData");
        CompoundTag receipts = persistentData.getCompound(GachaConstants.MODID).getCompound(RECEIPTS_KEY);
        List<GachaRewardDeliveryReceipt> found = new ArrayList<>();
        for (String key : receipts.getAllKeys()) {
            GachaTransactionId transactionId = new GachaTransactionId(UUID.fromString(key));
            found.add(find(persistentData, transactionId).orElseThrow(
                    () -> new IllegalArgumentException("Reward delivery receipt key is not canonical")));
        }
        return List.copyOf(found);
    }

    public boolean isPresentIn(CompoundTag persistentData) {
        return find(persistentData, transactionId).filter(this::equals).isPresent();
    }

    public boolean matchesPlayerSnapshot(CompoundTag playerSnapshot, ListTag expectedInventory) {
        Objects.requireNonNull(playerSnapshot, "playerSnapshot");
        Objects.requireNonNull(expectedInventory, "expectedInventory");
        return playerSnapshot.hasUUID(PLAYER_UUID_KEY)
                && playerSnapshot.getUUID(PLAYER_UUID_KEY).equals(playerId)
                && playerSnapshot.get(INVENTORY_KEY) instanceof ListTag inventory
                && inventory.equals(expectedInventory)
                && isPresentIn(playerSnapshot.getCompound(PERSISTENT_DATA_KEY));
    }

    public void removeFrom(CompoundTag persistentData) {
        if (!isPresentIn(persistentData)) {
            return;
        }

        CompoundTag modData = persistentData.getCompound(GachaConstants.MODID);
        CompoundTag receipts = modData.getCompound(RECEIPTS_KEY);
        receipts.remove(transactionId.value().toString());
        if (receipts.isEmpty()) {
            modData.remove(RECEIPTS_KEY);
        } else {
            modData.put(RECEIPTS_KEY, receipts);
        }
        persistentData.put(GachaConstants.MODID, modData);
    }
}

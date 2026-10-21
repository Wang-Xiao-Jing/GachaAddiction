package xiaojin.gachaaddiction.cardpool.delivery;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public record GachaRewardDeliveryAttempt(GachaRewardDeliveryReceipt receipt, ListTag inventory,
                                          List<GachaRewardDropSnapshot> drops) {
    private static final String RECEIPT_KEY = "receipt";
    private static final String INVENTORY_KEY = "inventory";
    private static final String DROPS_KEY = "drops";

    public GachaRewardDeliveryAttempt {
        Objects.requireNonNull(receipt, "receipt");
        inventory = Objects.requireNonNull(inventory, "inventory").copy();
        drops = List.copyOf(Objects.requireNonNull(drops, "drops"));
        if (!inventory.isEmpty() && inventory.getElementType() != Tag.TAG_COMPOUND) {
            throw new IllegalArgumentException("Delivery inventory must contain compound slots");
        }

        Set<UUID> entityIds = new HashSet<>();
        for (GachaRewardDropSnapshot drop : drops) {
            if (!drop.receipt().equals(receipt)) {
                throw new IllegalArgumentException("Reward drop receipt does not match its delivery attempt");
            }

            if (!entityIds.add(drop.entityId())) {
                throw new IllegalArgumentException("Reward delivery attempt contains a duplicate drop identity");
            }
        }
    }

    @Override
    public ListTag inventory() {
        return inventory.copy();
    }

    public CompoundTag encode() {
        CompoundTag encoded = new CompoundTag();
        encoded.put(RECEIPT_KEY, receipt.encode());
        encoded.put(INVENTORY_KEY, inventory.copy());
        ListTag encodedDrops = new ListTag();
        drops.forEach(drop -> encodedDrops.add(drop.encode()));
        encoded.put(DROPS_KEY, encodedDrops);
        return encoded;
    }

    public static GachaRewardDeliveryAttempt decode(CompoundTag encoded) {
        Objects.requireNonNull(encoded, "encoded");
        if (!encoded.contains(RECEIPT_KEY, Tag.TAG_COMPOUND)
                || !(encoded.get(INVENTORY_KEY) instanceof ListTag inventory)
                || !(encoded.get(DROPS_KEY) instanceof ListTag encodedDrops)) {
            throw new IllegalArgumentException("Reward delivery attempt is incomplete");
        }

        if (!encodedDrops.isEmpty() && encodedDrops.getElementType() != Tag.TAG_COMPOUND) {
            throw new IllegalArgumentException("Reward delivery drops must contain compound snapshots");
        }

        List<GachaRewardDropSnapshot> drops = new ArrayList<>();
        for (int index = 0; index < encodedDrops.size(); index++) {
            drops.add(GachaRewardDropSnapshot.decode(encodedDrops.getCompound(index)));
        }

        return new GachaRewardDeliveryAttempt(GachaRewardDeliveryReceipt.decode(encoded.getCompound(RECEIPT_KEY)),
                inventory, drops);
    }
}

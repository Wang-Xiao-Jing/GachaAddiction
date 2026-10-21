package xiaojin.gachaaddiction.cardpool.delivery;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.GachaConstants;

import java.util.Objects;
import java.util.UUID;

public record GachaRewardDropSnapshot(ResourceLocation dimension, CompoundTag entityData) {
    public static final String PENDING_RECEIPT_KEY = "pending_reward_delivery";

    private static final String DIMENSION_KEY = "dimension";
    private static final String ENTITY_KEY = "entity";
    private static final String ENTITY_TYPE_KEY = "id";
    private static final String ITEM_ENTITY_ID = "minecraft:item";
    private static final String ENTITY_UUID_KEY = "UUID";
    private static final String POSITION_KEY = "Pos";
    private static final String ITEM_KEY = "Item";
    private static final String PERSISTENT_DATA_KEY = "NeoForgeData";

    public GachaRewardDropSnapshot {
        Objects.requireNonNull(dimension, "dimension");
        entityData = Objects.requireNonNull(entityData, "entityData").copy();
        if (!ITEM_ENTITY_ID.equals(entityData.getString(ENTITY_TYPE_KEY))
                || !entityData.hasUUID(ENTITY_UUID_KEY)
                || !entityData.contains(ITEM_KEY, Tag.TAG_COMPOUND)
                || entityData.getCompound(ITEM_KEY).isEmpty()) {
            throw new IllegalArgumentException("Reward drop snapshot must contain an item entity");
        }

        ListTag position = entityData.getList(POSITION_KEY, Tag.TAG_DOUBLE);
        if (position.size() != 3) {
            throw new IllegalArgumentException("Reward drop position must contain three coordinates");
        }

        for (int index = 0; index < position.size(); index++) {
            if (!Double.isFinite(position.getDouble(index))) {
                throw new IllegalArgumentException("Reward drop position must be finite");
            }
        }

        receiptFrom(entityData);
    }

    @Override
    public CompoundTag entityData() {
        return entityData.copy();
    }

    public UUID entityId() {
        return entityData.getUUID(ENTITY_UUID_KEY);
    }

    public GachaRewardDeliveryReceipt receipt() {
        return receiptFrom(entityData);
    }

    public CompoundTag encode() {
        CompoundTag encoded = new CompoundTag();
        encoded.putString(DIMENSION_KEY, dimension.toString());
        encoded.put(ENTITY_KEY, entityData.copy());
        return encoded;
    }

    public static GachaRewardDropSnapshot decode(CompoundTag encoded) {
        Objects.requireNonNull(encoded, "encoded");
        if (!encoded.contains(DIMENSION_KEY, Tag.TAG_STRING) || !encoded.contains(ENTITY_KEY, Tag.TAG_COMPOUND)) {
            throw new IllegalArgumentException("Reward drop snapshot is incomplete");
        }

        return new GachaRewardDropSnapshot(ResourceLocation.parse(encoded.getString(DIMENSION_KEY)),
                encoded.getCompound(ENTITY_KEY));
    }

    private static GachaRewardDeliveryReceipt receiptFrom(CompoundTag entityData) {
        CompoundTag modData = entityData.getCompound(PERSISTENT_DATA_KEY).getCompound(GachaConstants.MODID);
        if (!modData.contains(PENDING_RECEIPT_KEY, Tag.TAG_COMPOUND)) {
            throw new IllegalArgumentException("Reward drop snapshot requires a delivery receipt");
        }

        return GachaRewardDeliveryReceipt.decode(modData.getCompound(PENDING_RECEIPT_KEY));
    }
}

package xiaojin.gachaaddiction.cardpool.delivery;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import xiaojin.gachaaddiction.GachaConstants;
import xiaojin.gachaaddiction.cardpool.transaction.GachaRewardDeliveryServices;
import xiaojin.gachaaddiction.persistence.GachaEntitySaveCheckpoint;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class GachaPendingRewardDrops {
    public static final int RECONCILE_INTERVAL_TICKS = 20;
    private static final String ORIGINAL_INVULNERABLE_KEY = "original_invulnerable";

    public static boolean isPending(CompoundTag persistentData) {
        return Objects.requireNonNull(persistentData, "persistentData").getCompound(GachaConstants.MODID)
                .contains(GachaRewardDropSnapshot.PENDING_RECEIPT_KEY);
    }

    public static boolean isPending(ItemEntity entity) {
        return isPending(Objects.requireNonNull(entity, "entity").getPersistentData());
    }

    public static void protect(ItemEntity entity, GachaRewardDeliveryReceipt receipt) {
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(receipt, "receipt");
        if (isPending(entity)) {
            throw new IllegalStateException("Reward drop is already awaiting confirmation");
        }

        CompoundTag marker = receipt.encode();
        marker.putBoolean(ORIGINAL_INVULNERABLE_KEY, entity.isInvulnerable());
        CompoundTag persistentData = entity.getPersistentData();
        CompoundTag modData = persistentData.getCompound(GachaConstants.MODID);
        modData.put(GachaRewardDropSnapshot.PENDING_RECEIPT_KEY, marker);
        persistentData.put(GachaConstants.MODID, modData);
        entity.setInvulnerable(true);
        entity.setNeverPickUp();
    }

    public static List<ItemEntity> prepare(ServerPlayer player, List<ItemStack> rewards,
                                           GachaRewardDeliveryReceipt receipt) {
        List<ItemEntity> drops = new ArrayList<>();
        for (int index = 0; index < rewards.size(); index++) {
            ItemStack reward = rewards.get(index);
            if (reward.isEmpty()) {
                continue;
            }

            if (player.captureDrops() != null) {
                throw new IllegalStateException("Cannot prepare reward drops inside another capture scope");
            }
            Collection<ItemEntity> captured = new ArrayList<>();
            Collection<ItemEntity> previous = player.captureDrops(captured);
            ItemEntity drop;
            try {
                drop = player.drop(reward.copy(), false, false);
            } finally {
                player.captureDrops(previous);
            }
            if (drop == null || captured.size() != 1 || !captured.contains(drop)) {
                throw new IllegalStateException("Unable to prepare a gacha reward drop");
            }

            String identity = "gacha-drop:" + receipt.playerId() + ":" + receipt.transactionId().value()
                    + ":" + receipt.attemptId() + ":" + index;
            drop.setUUID(UUID.nameUUIDFromBytes(identity.getBytes(StandardCharsets.UTF_8)));
            ItemTossEvent event = new ItemTossEvent(drop, player);
            if (NeoForge.EVENT_BUS.post(event).isCanceled() || drop.isRemoved()) {
                throw new IllegalStateException("Gacha reward drop was cancelled");
            }

            drop.setTarget(player.getUUID());
            protect(drop, receipt);
            drops.add(drop);
        }
        return List.copyOf(drops);
    }

    public static List<GachaRewardDropSnapshot> snapshots(List<ItemEntity> drops) {
        List<GachaRewardDropSnapshot> snapshots = new ArrayList<>();
        for (ItemEntity drop : drops) {
            CompoundTag data = new CompoundTag();
            if (!drop.save(data)) {
                throw new IllegalStateException("Unable to record a gacha reward drop");
            }
            snapshots.add(new GachaRewardDropSnapshot(drop.level().dimension().location(), data));
        }
        return List.copyOf(snapshots);
    }

    public static void ensurePresent(MinecraftServer server, GachaRewardDeliveryAttempt attempt) throws IOException {
        for (GachaRewardDropSnapshot snapshot : attempt.drops()) {
            ServerLevel level = level(server, snapshot);
            Entity existing = level.getEntity(snapshot.entityId());
            if (existing != null) {
                if (!(existing instanceof ItemEntity item) || !isPending(item)
                        || !receipt(item).equals(attempt.receipt())
                        || !item.getItem().save(level.registryAccess())
                        .equals(snapshot.entityData().getCompound("Item"))) {
                    throw new IOException("Loaded reward drop does not match its recorded attempt");
                }
                continue;
            }

            if (GachaEntitySaveCheckpoint.isSaved(server, snapshot)) {
                continue;
            }
            Entity restored = EntityType.loadEntityRecursive(snapshot.entityData(), level, entity -> entity);
            if (!(restored instanceof ItemEntity item) || !isPending(item) || !level.addFreshEntity(item)) {
                throw new IOException("Unable to restore a missing pending reward drop");
            }
        }
    }

    public static void releaseLoaded(MinecraftServer server, GachaRewardDeliveryAttempt attempt) {
        for (GachaRewardDropSnapshot snapshot : attempt.drops()) {
            Entity entity = level(server, snapshot).getEntity(snapshot.entityId());
            if (entity instanceof ItemEntity item && isPending(item)) {
                release(item, attempt.receipt());
            }
        }
    }

    public static void discardLoaded(MinecraftServer server, GachaRewardDeliveryAttempt attempt) {
        for (GachaRewardDropSnapshot snapshot : attempt.drops()) {
            Entity entity = level(server, snapshot).getEntity(snapshot.entityId());
            if (entity instanceof ItemEntity item && isPending(item) && receipt(item).equals(attempt.receipt())) {
                item.discard();
            }
        }
    }

    public static GachaRewardDeliveryReceipt receipt(ItemEntity entity) {
        CompoundTag modData = entity.getPersistentData().getCompound(GachaConstants.MODID);
        return GachaRewardDeliveryReceipt.decode(modData.getCompound(GachaRewardDropSnapshot.PENDING_RECEIPT_KEY));
    }

    public static void reconcile(ItemEntity entity, GachaRewardDeliveryServices services) {
        if (!isPending(entity)) {
            return;
        }
        try {
            GachaRewardDeliveryReceipt receipt = receipt(entity);
            var entry = services.deliveryLedger().find(receipt.playerId(), receipt.transactionId());
            if (entry.isEmpty() || entry.get().attempt().isEmpty()
                    || !entry.get().attempt().orElseThrow().receipt().equals(receipt)
                    || entry.get().attempt().orElseThrow().drops().stream()
                    .noneMatch(drop -> drop.entityId().equals(entity.getUUID()))) {
                entity.discard();
                return;
            }

            if (entry.get().stage() == GachaRewardDeliveryLedgerStage.DELIVERED) {
                release(entity, receipt);
            }
        } catch (RuntimeException failure) {
            GachaConstants.LOGGER.error("Unable to reconcile pending reward drop {}", entity.getUUID(), failure);
        }
    }

    private static ServerLevel level(MinecraftServer server, GachaRewardDropSnapshot snapshot) {
        ServerLevel level = server.getLevel(ResourceKey.create(Registries.DIMENSION, snapshot.dimension()));
        if (level == null) {
            throw new IllegalStateException("Reward drop dimension is not available: " + snapshot.dimension());
        }
        return level;
    }

    public static void release(ItemEntity entity, GachaRewardDeliveryReceipt receipt) {
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(receipt, "receipt");
        CompoundTag persistentData = entity.getPersistentData();
        CompoundTag modData = persistentData.getCompound(GachaConstants.MODID);
        if (!isPending(persistentData)) {
            return;
        }

        CompoundTag marker = modData.getCompound(GachaRewardDropSnapshot.PENDING_RECEIPT_KEY);
        if (!GachaRewardDeliveryReceipt.decode(marker).equals(receipt)) {
            throw new IllegalStateException("Cannot release a reward drop from another attempt");
        }

        entity.setInvulnerable(marker.getBoolean(ORIGINAL_INVULNERABLE_KEY));
        entity.setNoPickUpDelay();
        entity.setTarget(receipt.playerId());
        modData.remove(GachaRewardDropSnapshot.PENDING_RECEIPT_KEY);
        persistentData.put(GachaConstants.MODID, modData);
    }
}

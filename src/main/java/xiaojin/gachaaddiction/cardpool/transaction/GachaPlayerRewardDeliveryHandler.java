package xiaojin.gachaaddiction.cardpool.transaction;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import xiaojin.gachaaddiction.GachaConstants;
import xiaojin.gachaaddiction.cardpool.GachaRewardDeliveryMode;
import xiaojin.gachaaddiction.cardpool.delivery.GachaRewardDeliveryLedgerEntry;
import xiaojin.gachaaddiction.cardpool.delivery.GachaRewardDeliveryLedgerStage;
import xiaojin.gachaaddiction.cardpool.delivery.GachaRewardDeliveryAttempt;
import xiaojin.gachaaddiction.cardpool.delivery.GachaRewardDeliveryReceipt;
import xiaojin.gachaaddiction.cardpool.delivery.GachaRewardInboxEntry;
import xiaojin.gachaaddiction.cardpool.delivery.GachaRewardInboxEntryKind;
import xiaojin.gachaaddiction.cardpool.delivery.GachaRewardInboxClaim;
import xiaojin.gachaaddiction.cardpool.delivery.GachaPendingRewardDrops;
import xiaojin.gachaaddiction.persistence.GachaEntitySaveCheckpoint;
import xiaojin.gachaaddiction.persistence.GachaPlayerSaveCheckpoint;

import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;

public class GachaPlayerRewardDeliveryHandler implements IGachaRewardDeliveryHandler {
    private static final String RETURN_ID_SUFFIX = "return";
    private static final String OVERFLOW_ID_SUFFIX = "overflow";
    private static final String PLAYER_PENDING_DELIVERIES_KEY = "pending_reward_deliveries";
    private static final Set<GachaRewardDeliveryCapability> CAPABILITIES = Set.of(
            GachaRewardDeliveryCapability.IDEMPOTENT_BY_TRANSACTION,
            GachaRewardDeliveryCapability.DURABLE_BEFORE_ACKNOWLEDGEMENT,
            GachaRewardDeliveryCapability.SUPPORTS_OFFLINE_PLAYER);

    private final ResourceLocation type;
    private final GachaRewardDeliveryMode mode;

    public GachaPlayerRewardDeliveryHandler(ResourceLocation type, GachaRewardDeliveryMode mode) {
        this.type = Objects.requireNonNull(type, "type");
        this.mode = Objects.requireNonNull(mode, "mode");
        ResourceLocation expectedType = mode == GachaRewardDeliveryMode.INVENTORY
                ? GachaBuiltinRewardDeliveryTypes.INVENTORY : GachaBuiltinRewardDeliveryTypes.DROP;
        if (mode != GachaRewardDeliveryMode.INVENTORY && mode != GachaRewardDeliveryMode.DROP) {
            throw new IllegalArgumentException("Player reward delivery mode must be inventory or drop");
        }
        if (!expectedType.equals(type)) {
            throw new IllegalArgumentException("Player reward delivery type does not match its mode");
        }
    }

    private static void returnToInbox(GachaRewardDeliveryLedgerEntry entry, Optional<ServerPlayer> player,
                                      GachaRewardDeliveryServices services) {
        returnToInbox(entry, player, pendingRewards(entry), services);
    }

    private static void returnToInbox(GachaRewardDeliveryLedgerEntry entry, Optional<ServerPlayer> player,
                                      List<ItemStack> pendingRewards, GachaRewardDeliveryServices services) {
        if (entry.stage() != GachaRewardDeliveryLedgerStage.RETURN_PENDING
                || !entry.returnRewardsRecorded()) {
            entry = entry.returnPending(pendingRewards);
            services.deliveryLedger().store(entry);
        }
        if (player.isPresent()) {
            clearPendingMarker(player.get(), entry.transactionId());
        }
        List<ItemStack> recordedPendingRewards = entry.returnRewards();
        if (!recordedPendingRewards.isEmpty()) {
            deliverToReturnBox(entry.playerId(), entry.transactionId(), entry.mode(), recordedPendingRewards,
                    RETURN_ID_SUFFIX, services);
        }
        services.deliveryLedger().store(entry.delivered());
    }

    private static void deliverRemainders(GachaRewardDeliveryLedgerEntry entry,
                                          GachaRewardDeliveryServices services) {
        List<ItemStack> remainders = remainderRewards(entry);
        if (!remainders.isEmpty()) {
            deliverToReturnBox(entry.playerId(), entry.transactionId(), entry.mode(), remainders,
                    OVERFLOW_ID_SUFFIX, services);
        }
        services.deliveryLedger().store(entry.delivered());
    }

    private static List<ItemStack> pendingRewards(GachaRewardDeliveryLedgerEntry entry) {
        return entry.stage() == GachaRewardDeliveryLedgerStage.RETURN_PENDING
                ? entry.returnRewards() : entry.rewards();
    }

    private static List<ItemStack> remainderRewards(GachaRewardDeliveryLedgerEntry entry) {
        List<ItemStack> rewards = entry.rewards();
        List<ItemStack> remainders = new ArrayList<>();
        for (int index = 0; index < entry.remainderCounts().size(); index++) {
            int remainderCount = entry.remainderCounts().get(index);
            if (remainderCount > 0) {
                ItemStack remainder = rewards.get(index).copy();
                remainder.setCount(remainderCount);
                remainders.add(remainder);
            }
        }
        return List.copyOf(remainders);
    }

    private static void deliverToReturnBox(UUID playerId, GachaTransactionId transactionId,
                                           GachaRewardDeliveryMode mode, List<ItemStack> rewards, String suffix,
                                           GachaRewardDeliveryServices services) {
        String identity = playerId + ":" + transactionId.value() + ":" + mode.name() + ":" + suffix;
        GachaTransactionId returnTransactionId = new GachaTransactionId(
                UUID.nameUUIDFromBytes(identity.getBytes(StandardCharsets.UTF_8)));
        services.rewardInbox().restore(new GachaRewardInboxEntry(returnTransactionId, playerId,
                GachaRewardInboxEntryKind.RETURN, rewards));
    }

    static void reconcilePlayer(ServerPlayer player, GachaRewardDeliveryServices services) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(services, "services");
        for (GachaRewardDeliveryLedgerEntry entry : services.deliveryLedger().pending(player.getUUID())) {
            if (entry.attempt().isPresent()) {
                resumeOnline(player, entry, services);
                continue;
            }

            requireReceiptForProgress(entry);
            if (entry.stage() == GachaRewardDeliveryLedgerStage.PREPARED) {
                deliverOnlinePrepared(player, entry, services);
            } else {
                returnToInbox(entry, Optional.of(player), services);
            }
        }
        for (GachaRewardDeliveryReceipt receipt : GachaRewardDeliveryReceipt.all(player.getPersistentData())) {
            if (!receipt.playerId().equals(player.getUUID())) {
                throw new IllegalStateException("Saved delivery receipt belongs to another player");
            }
            var completed = services.deliveryLedger().find(player.getUUID(), receipt.transactionId());
            if (completed.isPresent() && completed.get().stage() == GachaRewardDeliveryLedgerStage.DELIVERED
                    && completed.get().attempt().filter(attempt -> attempt.receipt().equals(receipt)).isPresent()) {
                resumeOnline(player, completed.get(), services);
            }
        }
    }


    private static void clearPendingMarker(ServerPlayer player, GachaTransactionId transactionId) {
        CompoundTag persistentData = player.getPersistentData();
        CompoundTag modData = persistentData.getCompound(GachaConstants.MODID);
        CompoundTag pendingDeliveries = modData.getCompound(PLAYER_PENDING_DELIVERIES_KEY);
        String key = transactionId.value().toString();
        pendingDeliveries.remove(key);
        modData.put(PLAYER_PENDING_DELIVERIES_KEY, pendingDeliveries);
        persistentData.put(GachaConstants.MODID, modData);
    }

    @Override
    public ResourceLocation type() {
        return type;
    }

    @Override
    public Set<GachaRewardDeliveryCapability> capabilities() {
        return CAPABILITIES;
    }

    @Override
    public void validate(GachaTransactionContext context, GachaRewardDeliveryServices services) {
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(services, "services");
    }

    @Override
    public synchronized void deliver(GachaTransactionContext context, List<ItemStack> rewards,
                                     GachaRewardDeliveryServices services) {
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(rewards, "rewards");
        Objects.requireNonNull(services, "services");
        GachaRewardDeliveryLedgerEntry entry = services.deliveryLedger()
                .find(context.playerId(), context.transactionId())
                .orElseGet(() -> {
                    GachaRewardDeliveryLedgerEntry prepared = GachaRewardDeliveryLedgerEntry.prepared(
                            context.transactionId(), context.playerId(), mode, rewards);
                    services.deliveryLedger().store(prepared);
                    return prepared;
                });
        requireMatching(entry, context, rewards);
        if (entry.attempt().isPresent()) {
            services.player(context.playerId()).ifPresent(player -> resumeOnline(player, entry, services));
            return;
        }

        requireReceiptForProgress(entry);
        switch (entry.stage()) {
            case PREPARED -> deliverPrepared(context, entry, services);
            case RETURN_PENDING -> returnToInbox(context, entry, services);
            case APPLYING, ONLINE_APPLIED -> throw new IllegalStateException("Player delivery receipt is missing");
            case DELIVERED -> {
            }
        }
    }

    private void deliverPrepared(GachaTransactionContext context, GachaRewardDeliveryLedgerEntry entry,
                                 GachaRewardDeliveryServices services) {
        Optional<ServerPlayer> player = services.player(context.playerId());
        if (player.isEmpty()) {
            returnToInbox(context, entry, services);
            return;
        }

        deliverOnlinePrepared(player.get(), entry, services);
    }

    private static void deliverOnlinePrepared(ServerPlayer player, GachaRewardDeliveryLedgerEntry entry,
                                              GachaRewardDeliveryServices services) {
        if (!services.server().isSameThread() || player.getServer() != services.server()) {
            throw new IllegalStateException("Player delivery requires its server thread");
        }

        Inventory inventory = player.getInventory();
        Inventory original = copyInventory(inventory);
        Inventory planned = copyInventory(inventory);
        List<Integer> remainderCounts = new ArrayList<>();
        List<ItemStack> droppedRewards = new ArrayList<>();
        boolean dropRemainders = entry.mode() == GachaRewardDeliveryMode.DROP || entry.inboxEntryId().isPresent();
        for (ItemStack reward : entry.rewards()) {
            ItemStack remainder = reward.copy();
            if (entry.mode() == GachaRewardDeliveryMode.INVENTORY) {
                planned.add(remainder);
            }
            if (dropRemainders && !remainder.isEmpty()) {
                droppedRewards.add(remainder);
            }
            remainderCounts.add(dropRemainders ? 0 : remainder.getCount());
        }

        GachaRewardDeliveryReceipt receipt = new GachaRewardDeliveryReceipt(player.getUUID(),
                entry.transactionId(), UUID.randomUUID());
        List<ItemEntity> drops;
        try {
            drops = GachaPendingRewardDrops.prepare(player, droppedRewards, receipt);
        } catch (RuntimeException failure) {
            if (entry.inboxEntryId().isPresent()) {
                throw failure;
            }
            returnToInbox(entry, Optional.of(player), services);
            return;
        }
        GachaRewardDeliveryAttempt attempt = new GachaRewardDeliveryAttempt(receipt,
                planned.save(new ListTag()), GachaPendingRewardDrops.snapshots(drops));
        GachaRewardDeliveryLedgerEntry applying = entry.applying(attempt, remainderCounts);
        services.deliveryLedger().store(applying);
        if (!inventory.save(new ListTag()).equals(original.save(new ListTag()))) {
            throw new IllegalStateException("Player inventory changed while planning reward delivery");
        }

        try {
            if (entry.mode() == GachaRewardDeliveryMode.INVENTORY) {
                inventory.replaceWith(planned);
            }
            receipt.writeTo(player.getPersistentData());
        } catch (RuntimeException failure) {
            if (entry.mode() == GachaRewardDeliveryMode.INVENTORY) {
                inventory.replaceWith(original);
            }
            receipt.removeFrom(player.getPersistentData());
            throw failure;
        }
        for (ItemEntity drop : drops) {
            if (!player.serverLevel().addFreshEntity(drop)) {
                throw new IllegalStateException("Unable to publish a pending reward drop");
            }
        }
        GachaRewardDeliveryLedgerEntry onlineApplied = applying.onlineApplied(remainderCounts);
        services.deliveryLedger().store(onlineApplied);
        if (entry.mode() == GachaRewardDeliveryMode.INVENTORY) {
            inventory.setChanged();
            player.containerMenu.broadcastChanges();
        }
        confirmOnline(player, onlineApplied, services);
    }

    private static Inventory copyInventory(Inventory source) {
        Inventory copy = new Inventory(source.player);
        for (int slot = 0; slot < source.getContainerSize(); slot++) {
            copy.setItem(slot, source.getItem(slot).copy());
        }
        copy.selected = source.selected;
        return copy;
    }

    private static void resumeOnline(ServerPlayer player, GachaRewardDeliveryLedgerEntry entry,
                                     GachaRewardDeliveryServices services) {
        GachaRewardDeliveryAttempt attempt = entry.attempt().orElseThrow();
        if (entry.stage() == GachaRewardDeliveryLedgerStage.DELIVERED) {
            acknowledgeInbox(entry, services);
            GachaPendingRewardDrops.releaseLoaded(services.server(), attempt);
            attempt.receipt().removeFrom(player.getPersistentData());
            return;
        }

        if (attempt.receipt().isPresentIn(player.getPersistentData())) {
            try {
                GachaPendingRewardDrops.ensurePresent(services.server(), attempt);
            } catch (IOException failure) {
                throw new UncheckedIOException("Unable to restore pending reward drops", failure);
            }
            confirmOnline(player, entry, services);
            return;
        }

        GachaPendingRewardDrops.discardLoaded(services.server(), attempt);
        GachaRewardDeliveryLedgerEntry prepared = GachaRewardDeliveryLedgerEntry.prepared(
                entry.transactionId(), entry.playerId(), entry.mode(), entry.rewards());
        if (entry.inboxEntryId().isPresent()) {
            prepared = prepared.forInboxClaim(entry.inboxEntryId().orElseThrow());
        }
        services.deliveryLedger().store(prepared);
        deliverOnlinePrepared(player, prepared, services);
    }

    private static void confirmOnline(ServerPlayer player, GachaRewardDeliveryLedgerEntry entry,
                                      GachaRewardDeliveryServices services) {
        GachaRewardDeliveryAttempt attempt = entry.attempt().orElseThrow();
        if (attempt.drops().isEmpty()) {
            GachaPlayerSaveCheckpoint.saveAndVerify(player, attempt.receipt(), player.getInventory().save(new ListTag()));
        } else {
            services.server().saveEverything(true, true, true);
            GachaPlayerSaveCheckpoint.verifyAfterSave(player, attempt.receipt(), player.getInventory().save(new ListTag()));
            try {
                GachaEntitySaveCheckpoint.verifyAfterSave(services.server(), attempt.drops());
            } catch (IOException failure) {
                throw new UncheckedIOException("Unable to confirm pending reward drops", failure);
            }
        }
        deliverRemainders(entry, services);
        acknowledgeInbox(entry, services);
        GachaPendingRewardDrops.releaseLoaded(services.server(), attempt);
        attempt.receipt().removeFrom(player.getPersistentData());
    }

    private static void acknowledgeInbox(GachaRewardDeliveryLedgerEntry entry, GachaRewardDeliveryServices services) {
        entry.inboxEntryId().ifPresent(sourceId -> {
            services.rewardInbox().find(entry.playerId(), sourceId)
                    .ifPresent(source -> requireMatchingRewards(entry, source.rewards()));
            services.rewardInbox().take(entry.playerId(), sourceId);
        });
    }

    public static int claimInbox(ServerPlayer player, GachaRewardInboxEntry source,
                                 GachaRewardDeliveryServices services) {
        if (!services.server().isSameThread() || !source.playerId().equals(player.getUUID())) {
            throw new IllegalStateException("Inbox claim player or thread does not match");
        }
        GachaTransactionId claimId = GachaRewardInboxClaim.transactionIdFor(player.getUUID(), source.transactionId());
        GachaRewardDeliveryLedgerEntry entry = services.deliveryLedger().find(player.getUUID(), claimId)
                .orElseGet(() -> {
                    GachaRewardDeliveryLedgerEntry prepared = GachaRewardDeliveryLedgerEntry.prepared(claimId,
                                    player.getUUID(), GachaRewardDeliveryMode.INVENTORY, source.rewards())
                            .forInboxClaim(source.transactionId());
                    services.deliveryLedger().store(prepared);
                    return prepared;
                });
        if (!entry.inboxEntryId().equals(Optional.of(source.transactionId()))) {
            throw new IllegalStateException("Inbox claim source does not match its ledger entry");
        }
        requireMatchingRewards(entry, source.rewards());
        if (entry.attempt().isPresent()) {
            resumeOnline(player, entry, services);
        } else if (entry.stage() == GachaRewardDeliveryLedgerStage.PREPARED) {
            deliverOnlinePrepared(player, entry, services);
        } else {
            throw new IllegalStateException("Inbox claim has no verifiable delivery attempt");
        }
        if (services.deliveryLedger().find(player.getUUID(), claimId).orElseThrow().stage()
                != GachaRewardDeliveryLedgerStage.DELIVERED) {
            throw new IllegalStateException("Inbox claim is still awaiting confirmation");
        }
        return source.rewards().size();
    }


    private static void requireReceiptForProgress(GachaRewardDeliveryLedgerEntry entry) {
        if (entry.attempt().isEmpty()
                && (entry.stage() == GachaRewardDeliveryLedgerStage.APPLYING
                || entry.stage() == GachaRewardDeliveryLedgerStage.ONLINE_APPLIED)) {
            throw new IllegalStateException("Unverified legacy player delivery requires reconciliation: "
                    + entry.transactionId().value());
        }
    }

    private void returnToInbox(GachaTransactionContext context, GachaRewardDeliveryLedgerEntry entry,
                               GachaRewardDeliveryServices services) {
        returnToInbox(entry, services.player(context.playerId()), services);
    }

    private void requireMatching(GachaRewardDeliveryLedgerEntry entry, GachaTransactionContext context,
                                 List<ItemStack> rewards) {
        if (!entry.transactionId().equals(context.transactionId()) || !entry.playerId().equals(context.playerId())
                || entry.mode() != mode) {
            throw new IllegalStateException("Reward delivery transaction identity does not match its ledger entry");
        }
        requireMatchingRewards(entry, rewards);
    }

    private static void requireMatchingRewards(GachaRewardDeliveryLedgerEntry entry, List<ItemStack> rewards) {
        List<ItemStack> savedRewards = entry.rewards();
        if (savedRewards.size() != rewards.size()) {
            throw new IllegalStateException("Reward delivery transaction contains a different reward list");
        }
        for (int index = 0; index < savedRewards.size(); index++) {
            ItemStack saved = savedRewards.get(index);
            ItemStack requested = rewards.get(index);
            if (!ItemStack.isSameItemSameComponents(saved, requested) || saved.getCount() != requested.getCount()) {
                throw new IllegalStateException("Reward delivery transaction contains different rewards");
            }
        }
    }
}

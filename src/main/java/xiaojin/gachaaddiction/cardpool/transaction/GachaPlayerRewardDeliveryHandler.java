package xiaojin.gachaaddiction.cardpool.transaction;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import xiaojin.gachaaddiction.GachaAddiction;
import xiaojin.gachaaddiction.cardpool.GachaRewardDeliveryMode;
import xiaojin.gachaaddiction.cardpool.delivery.GachaRewardDeliveryLedgerEntry;
import xiaojin.gachaaddiction.cardpool.delivery.GachaRewardDeliveryLedgerStage;
import xiaojin.gachaaddiction.cardpool.delivery.GachaRewardInboxEntry;
import xiaojin.gachaaddiction.cardpool.delivery.GachaRewardInboxEntryKind;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class GachaPlayerRewardDeliveryHandler implements GachaRewardDeliveryHandler {
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
            setPendingMarker(player.get(), entry.transactionId(), false);
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
        if (entry.stage() == GachaRewardDeliveryLedgerStage.RETURN_PENDING
                && entry.returnRewardsRecorded()) {
            return entry.returnRewards();
        }
        if (entry.stage() == GachaRewardDeliveryLedgerStage.APPLYING
                && entry.deliveryProgressRecorded()) {
            return remainderRewards(entry);
        }
        if (entry.stage() != GachaRewardDeliveryLedgerStage.ONLINE_APPLIED
                || entry.mode() != GachaRewardDeliveryMode.INVENTORY) {
            return entry.rewards();
        }
        return remainderRewards(entry);
    }

    private static List<ItemStack> pendingRewards(List<ItemStack> rewards, List<Integer> remainderCounts,
                                                  List<Boolean> delivered) {
        List<ItemStack> pending = new ArrayList<>();
        for (int index = 0; index < rewards.size(); index++) {
            if (!delivered.get(index)) {
                ItemStack reward = rewards.get(index);
                if (!reward.isEmpty()) {
                    pending.add(reward.copy());
                }
            } else if (remainderCounts.get(index) > 0) {
                ItemStack remainder = rewards.get(index).copy();
                remainder.setCount(remainderCounts.get(index));
                pending.add(remainder);
            }
        }
        return List.copyOf(pending);
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
            if (entry.stage() == GachaRewardDeliveryLedgerStage.ONLINE_APPLIED
                    && entry.mode() == GachaRewardDeliveryMode.INVENTORY
                    && hasPendingMarker(player, entry.transactionId())) {
                deliverRemainders(entry, services);
                setPendingMarker(player, entry.transactionId(), false);
            } else if (entry.stage() == GachaRewardDeliveryLedgerStage.ONLINE_APPLIED
                    && entry.mode() == GachaRewardDeliveryMode.DROP) {
                deliverRemainders(entry, services);
            } else {
                returnToInbox(entry, Optional.of(player), services);
            }
        }
    }

    static void acknowledgePlayerSave(ServerPlayer player, GachaRewardDeliveryServices services) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(services, "services");
        for (GachaRewardDeliveryLedgerEntry entry : services.deliveryLedger().pending(player.getUUID())) {
            if (entry.stage() == GachaRewardDeliveryLedgerStage.ONLINE_APPLIED
                    && entry.mode() == GachaRewardDeliveryMode.INVENTORY
                    && hasPendingMarker(player, entry.transactionId())) {
                deliverRemainders(entry, services);
                setPendingMarker(player, entry.transactionId(), false);
            } else if (entry.stage() == GachaRewardDeliveryLedgerStage.ONLINE_APPLIED
                    && entry.mode() == GachaRewardDeliveryMode.DROP) {
                deliverRemainders(entry, services);
            }
        }
    }

    private static boolean hasPendingMarker(ServerPlayer player, GachaTransactionId transactionId) {
        return player.getPersistentData().getCompound(GachaAddiction.MODID)
                .getCompound(PLAYER_PENDING_DELIVERIES_KEY).getBoolean(transactionId.value().toString());
    }

    private static void setPendingMarker(ServerPlayer player, GachaTransactionId transactionId, boolean pending) {
        CompoundTag persistentData = player.getPersistentData();
        CompoundTag modData = persistentData.getCompound(GachaAddiction.MODID);
        CompoundTag pendingDeliveries = modData.getCompound(PLAYER_PENDING_DELIVERIES_KEY);
        String key = transactionId.value().toString();
        if (pending) {
            pendingDeliveries.putBoolean(key, true);
        } else {
            pendingDeliveries.remove(key);
        }
        modData.put(PLAYER_PENDING_DELIVERIES_KEY, pendingDeliveries);
        persistentData.put(GachaAddiction.MODID, modData);
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
        switch (entry.stage()) {
            case PREPARED -> deliverPrepared(context, entry, services);
            case APPLYING, RETURN_PENDING -> returnToInbox(context, entry, services);
            case ONLINE_APPLIED -> {
                if (mode == GachaRewardDeliveryMode.DROP) {
                    deliverRemainders(entry, services);
                }
            }
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
        GachaRewardDeliveryLedgerEntry applying = entry.applying();
        services.deliveryLedger().store(applying);
        if (mode == GachaRewardDeliveryMode.INVENTORY) {
            setPendingMarker(player.get(), context.transactionId(), true);
        }
        List<Integer> remainderCounts = new ArrayList<>(applying.remainderCounts());
        List<Boolean> delivered = new ArrayList<>(Collections.nCopies(entry.rewards().size(), false));
        try {
            deliverOnline(player.get(), entry.rewards(), remainderCounts, delivered, applying, services);
        } catch (RuntimeException failure) {
            try {
                returnToInbox(context, applying, pendingRewards(entry.rewards(), remainderCounts, delivered), services);
            } catch (RuntimeException fallbackFailure) {
                fallbackFailure.addSuppressed(failure);
                throw fallbackFailure;
            }
            return;
        }
        GachaRewardDeliveryLedgerEntry onlineApplied = applying.onlineApplied(remainderCounts);
        services.deliveryLedger().store(onlineApplied);
        if (mode == GachaRewardDeliveryMode.DROP) {
            deliverRemainders(onlineApplied, services);
        }
    }

    private void deliverOnline(ServerPlayer player, List<ItemStack> rewards, List<Integer> remainderCounts,
                               List<Boolean> delivered, GachaRewardDeliveryLedgerEntry applying,
                               GachaRewardDeliveryServices services) {
        for (int index = 0; index < rewards.size(); index++) {
            ItemStack reward = rewards.get(index);
            if (reward.isEmpty()) {
                delivered.set(index, true);
                services.deliveryLedger().store(applying.applyingProgress(remainderCounts));
                continue;
            }
            if (mode == GachaRewardDeliveryMode.INVENTORY) {
                ItemStack remainder = reward.copy();
                player.getInventory().add(remainder);
                remainderCounts.set(index, remainder.getCount());
                delivered.set(index, true);
                services.deliveryLedger().store(applying.applyingProgress(remainderCounts));
            } else {
                ItemEntity dropped = player.drop(reward.copy(), false);
                if (dropped == null) {
                    throw new IllegalStateException("Unable to drop a gacha reward stack");
                }
                delivered.set(index, true);
                remainderCounts.set(index, 0);
                services.deliveryLedger().store(applying.applyingProgress(remainderCounts));
                dropped.setNoPickUpDelay();
                dropped.setTarget(player.getUUID());
            }
        }
        if (mode == GachaRewardDeliveryMode.INVENTORY) {
            player.getInventory().setChanged();
            player.containerMenu.broadcastChanges();
        }
    }

    private void returnToInbox(GachaTransactionContext context, GachaRewardDeliveryLedgerEntry entry,
                               GachaRewardDeliveryServices services) {
        returnToInbox(entry, services.player(context.playerId()), services);
    }

    private void returnToInbox(GachaTransactionContext context, GachaRewardDeliveryLedgerEntry entry,
                               List<ItemStack> pendingRewards, GachaRewardDeliveryServices services) {
        returnToInbox(entry, services.player(context.playerId()), pendingRewards, services);
    }

    private void requireMatching(GachaRewardDeliveryLedgerEntry entry, GachaTransactionContext context,
                                 List<ItemStack> rewards) {
        if (!entry.transactionId().equals(context.transactionId()) || !entry.playerId().equals(context.playerId())
                || entry.mode() != mode) {
            throw new IllegalStateException("Reward delivery transaction identity does not match its ledger entry");
        }
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

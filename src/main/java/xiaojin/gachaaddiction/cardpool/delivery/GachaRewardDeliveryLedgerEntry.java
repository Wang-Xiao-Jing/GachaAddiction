package xiaojin.gachaaddiction.cardpool.delivery;

import net.minecraft.world.item.ItemStack;
import xiaojin.gachaaddiction.cardpool.GachaRewardDeliveryMode;
import xiaojin.gachaaddiction.cardpool.transaction.GachaTransactionId;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record GachaRewardDeliveryLedgerEntry(GachaTransactionId transactionId, UUID playerId,
                                             GachaRewardDeliveryMode mode,
                                             GachaRewardDeliveryLedgerStage stage,
                                             List<ItemStack> rewards, List<Integer> remainderCounts,
                                             boolean deliveryProgressRecorded,
                                             List<ItemStack> returnRewards, boolean returnRewardsRecorded) {
    public GachaRewardDeliveryLedgerEntry(GachaTransactionId transactionId, UUID playerId,
                                          GachaRewardDeliveryMode mode,
                                          GachaRewardDeliveryLedgerStage stage,
                                          List<ItemStack> rewards, List<Integer> remainderCounts) {
        this(transactionId, playerId, mode, stage, rewards, remainderCounts, false, List.of(), false);
    }

    public GachaRewardDeliveryLedgerEntry(GachaTransactionId transactionId, UUID playerId,
                                          GachaRewardDeliveryMode mode,
                                          GachaRewardDeliveryLedgerStage stage,
                                          List<ItemStack> rewards, List<Integer> remainderCounts,
                                          List<ItemStack> returnRewards) {
        this(transactionId, playerId, mode, stage, rewards, remainderCounts, returnRewards,
                stage == GachaRewardDeliveryLedgerStage.RETURN_PENDING);
    }

    public GachaRewardDeliveryLedgerEntry(GachaTransactionId transactionId, UUID playerId,
                                          GachaRewardDeliveryMode mode,
                                          GachaRewardDeliveryLedgerStage stage,
                                          List<ItemStack> rewards, List<Integer> remainderCounts,
                                          List<ItemStack> returnRewards, boolean returnRewardsRecorded) {
        this(transactionId, playerId, mode, stage, rewards, remainderCounts,
                stage == GachaRewardDeliveryLedgerStage.ONLINE_APPLIED,
                returnRewards, returnRewardsRecorded);
    }

    public GachaRewardDeliveryLedgerEntry {
        Objects.requireNonNull(transactionId, "transactionId");
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(mode, "mode");
        Objects.requireNonNull(stage, "stage");
        rewards = Objects.requireNonNull(rewards, "rewards").stream().map(ItemStack::copy).toList();
        remainderCounts = List.copyOf(Objects.requireNonNull(remainderCounts, "remainderCounts"));
        returnRewards = Objects.requireNonNull(returnRewards, "returnRewards").stream()
                .map(ItemStack::copy).toList();
        if (remainderCounts.size() != rewards.size()) {
            throw new IllegalArgumentException("Reward delivery remainder count does not match reward count");
        }
        if (stage != GachaRewardDeliveryLedgerStage.RETURN_PENDING
                && (returnRewardsRecorded || !returnRewards.isEmpty())) {
            throw new IllegalArgumentException("Return rewards are only valid while return is pending");
        }
        if (stage == GachaRewardDeliveryLedgerStage.RETURN_PENDING && !returnRewardsRecorded) {
            throw new IllegalArgumentException("Return rewards must be recorded while return is pending");
        }
        if (stage != GachaRewardDeliveryLedgerStage.APPLYING
                && stage != GachaRewardDeliveryLedgerStage.ONLINE_APPLIED
                && deliveryProgressRecorded) {
            throw new IllegalArgumentException("Delivery progress is only valid during online delivery");
        }
        if ((stage == GachaRewardDeliveryLedgerStage.APPLYING
                || stage == GachaRewardDeliveryLedgerStage.ONLINE_APPLIED)
                && !deliveryProgressRecorded
                && remainderCounts.stream().anyMatch(count -> count != 0)) {
            throw new IllegalArgumentException("Unrecorded delivery progress must have zero remainders");
        }
        for (int index = 0; index < remainderCounts.size(); index++) {
            int remainderCount = remainderCounts.get(index);
            if (remainderCount < 0 || remainderCount > rewards.get(index).getCount()) {
                throw new IllegalArgumentException("Reward delivery remainder count is outside its stack count");
            }
            if (stage != GachaRewardDeliveryLedgerStage.APPLYING
                    && stage != GachaRewardDeliveryLedgerStage.ONLINE_APPLIED
                    && remainderCount != 0) {
                throw new IllegalArgumentException("Remainder counts are only valid after online delivery");
            }
            if (mode == GachaRewardDeliveryMode.DROP
                    && stage != GachaRewardDeliveryLedgerStage.APPLYING
                    && remainderCount != 0) {
                throw new IllegalArgumentException("Dropped rewards cannot have inventory remainders");
            }
        }
    }

    public static GachaRewardDeliveryLedgerEntry prepared(GachaTransactionId transactionId, UUID playerId,
                                                          GachaRewardDeliveryMode mode,
                                                          List<ItemStack> rewards) {
        List<ItemStack> copiedRewards = Objects.requireNonNull(rewards, "rewards").stream()
                .map(ItemStack::copy).toList();
        return new GachaRewardDeliveryLedgerEntry(transactionId, playerId, mode,
                GachaRewardDeliveryLedgerStage.PREPARED, copiedRewards,
                Collections.nCopies(copiedRewards.size(), 0));
    }

    public GachaRewardDeliveryLedgerEntry applying() {
        requireStage(GachaRewardDeliveryLedgerStage.PREPARED);
        List<Integer> initialRemainders = rewards.stream().map(ItemStack::getCount).toList();
        return new GachaRewardDeliveryLedgerEntry(transactionId, playerId, mode,
                GachaRewardDeliveryLedgerStage.APPLYING, rewards, initialRemainders, true,
                List.of(), false);
    }

    public GachaRewardDeliveryLedgerEntry applyingProgress(List<Integer> remainders) {
        requireStage(GachaRewardDeliveryLedgerStage.APPLYING);
        return new GachaRewardDeliveryLedgerEntry(transactionId, playerId, mode,
                GachaRewardDeliveryLedgerStage.APPLYING, rewards, remainders, true,
                List.of(), false);
    }

    public GachaRewardDeliveryLedgerEntry onlineApplied(List<Integer> remainders) {
        requireStage(GachaRewardDeliveryLedgerStage.APPLYING);
        return new GachaRewardDeliveryLedgerEntry(transactionId, playerId, mode,
                GachaRewardDeliveryLedgerStage.ONLINE_APPLIED, rewards, remainders, true,
                List.of(), false);
    }

    public GachaRewardDeliveryLedgerEntry returnPending() {
        return returnPending(rewards);
    }

    public GachaRewardDeliveryLedgerEntry returnPending(List<ItemStack> pendingRewards) {
        if (stage != GachaRewardDeliveryLedgerStage.PREPARED
                && stage != GachaRewardDeliveryLedgerStage.APPLYING
                && stage != GachaRewardDeliveryLedgerStage.ONLINE_APPLIED
                && stage != GachaRewardDeliveryLedgerStage.RETURN_PENDING) {
            throw new IllegalStateException("Reward delivery cannot return to the inbox from stage " + stage);
        }
        return new GachaRewardDeliveryLedgerEntry(transactionId, playerId, mode,
                GachaRewardDeliveryLedgerStage.RETURN_PENDING, rewards,
                Collections.nCopies(rewards.size(), 0), false, pendingRewards, true);
    }

    public GachaRewardDeliveryLedgerEntry delivered() {
        return new GachaRewardDeliveryLedgerEntry(transactionId, playerId, mode,
                GachaRewardDeliveryLedgerStage.DELIVERED, rewards,
                Collections.nCopies(rewards.size(), 0));
    }

    @Override
    public List<ItemStack> rewards() {
        return rewards.stream().map(ItemStack::copy).toList();
    }

    @Override
    public List<ItemStack> returnRewards() {
        return returnRewards.stream().map(ItemStack::copy).toList();
    }

    private void requireStage(GachaRewardDeliveryLedgerStage expected) {
        if (stage != expected) {
            throw new IllegalStateException("Reward delivery ledger stage is " + stage + ", expected " + expected);
        }
    }
}

package xiaojin.gachaaddiction.reward.quantity;

import net.minecraft.world.item.ItemStack;
import xiaojin.gachaaddiction.reward.model.GachaLogicalReward;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class GachaRewardFinalizer {
    private GachaRewardFinalizer() {
    }

    public static List<ItemStack> finalizeRewards(List<GachaLogicalReward> rewards, GachaExecutionLimits limits) {
        Objects.requireNonNull(rewards, "rewards");
        Objects.requireNonNull(limits, "limits");
        if (rewards.size() > limits.maxLogicalRewards()) {
            throw new IllegalArgumentException("Logical reward count exceeds execution limit");
        }

        List<ItemStack> stacks = new ArrayList<>();
        for (GachaLogicalReward reward : rewards) {
            appendReward(stacks, Objects.requireNonNull(reward, "reward"), limits);
        }
        return List.copyOf(stacks);
    }

    private static void appendReward(List<ItemStack> stacks, GachaLogicalReward reward,
                                     GachaExecutionLimits limits) {
        if (reward.quantity() > limits.maxLogicalQuantity()) {
            throw new IllegalArgumentException("Logical reward quantity exceeds execution limit");
        }

        ItemStack template = reward.template();
        int maximumStackSize = template.getMaxStackSize();
        if (maximumStackSize <= 0) {
            throw new IllegalArgumentException("Reward has no valid maximum stack size");
        }

        long remaining = reward.quantity();
        while (remaining > 0) {
            if (stacks.size() >= limits.maxPhysicalStacks()) {
                throw new IllegalArgumentException("Physical stack count exceeds execution limit");
            }
            int stackSize = (int) Math.min(remaining, maximumStackSize);
            stacks.add(template.copyWithCount(stackSize));
            remaining -= stackSize;
        }
    }
}

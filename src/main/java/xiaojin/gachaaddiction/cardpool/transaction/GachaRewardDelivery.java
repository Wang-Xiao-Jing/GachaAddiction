package xiaojin.gachaaddiction.cardpool.transaction;

import net.minecraft.world.item.ItemStack;

import java.util.List;

@FunctionalInterface
public interface GachaRewardDelivery {
    void deliver(GachaTransactionContext context, List<ItemStack> rewards);
}

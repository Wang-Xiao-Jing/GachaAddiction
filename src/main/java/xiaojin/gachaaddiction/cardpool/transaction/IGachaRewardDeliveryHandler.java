package xiaojin.gachaaddiction.cardpool.transaction;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Set;

public interface IGachaRewardDeliveryHandler {
    ResourceLocation type();

    Set<GachaRewardDeliveryCapability> capabilities();

    default void validate(GachaTransactionContext context, GachaRewardDeliveryServices services) {
    }

    void deliver(GachaTransactionContext context, List<ItemStack> rewards, GachaRewardDeliveryServices services);
}

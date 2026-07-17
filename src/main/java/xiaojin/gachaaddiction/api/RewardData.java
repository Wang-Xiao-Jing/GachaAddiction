package xiaojin.gachaaddiction.api;

import net.minecraft.world.item.ItemStack;

public class RewardData {
    private final ItemStack itemStack;

    public RewardData(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof RewardData that)) return false;

        return ItemStack.isSameItemSameComponents(this.itemStack, that.itemStack);
    }

    @Override
    public int hashCode() {
        return itemStack.hashCode();
    }
}

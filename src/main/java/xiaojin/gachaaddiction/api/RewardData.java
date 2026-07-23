package xiaojin.gachaaddiction.api;

import net.minecraft.world.item.ItemStack;
import xiaojin.gachaaddiction.util.RarityUtil;

@SuppressWarnings("ClassCanBeRecord")
public class RewardData {
    private final ItemStack itemStack;
    private final int count;
    private final ItemStack countItemStack;

    public RewardData(ItemStack itemStack, int count) {
        this.itemStack = itemStack;
        this.count = count;
        this.countItemStack = itemStack.copyWithCount(count);
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public ItemStack getCountItemStack() {
        return countItemStack;
    }

    public int getCount() {
        return count;
    }

    public int getRarityLevel() {
        return RarityUtil.getRarityLevel(itemStack);
    }

    public int getRarityColor() {
        return RarityUtil.getRarityColor(itemStack);
    }
}

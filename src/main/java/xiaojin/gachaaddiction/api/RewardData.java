package xiaojin.gachaaddiction.api;

import net.minecraft.world.item.ItemStack;
import xiaojin.gachaaddiction.util.RarityUtil;

public class RewardData {
    private final ItemStack itemStack;
    private final int count;
    private final ItemStack countItemStack;
    private int rarityLevel = -1;
    private int rarityColor;

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

    public void setRarity(int level, int color) {
        this.rarityLevel = level;
        this.rarityColor = color;
    }

    public int getRarityLevel() {
        return rarityLevel >= 0 ? rarityLevel : RarityUtil.getRarityLevel(itemStack);
    }

    public int getRarityColor() {
        return rarityLevel >= 0 ? rarityColor : RarityUtil.getRarityColor(itemStack);
    }

    @Override
    public String toString() {
        return itemStack.toString() + " " + count;
    }
}

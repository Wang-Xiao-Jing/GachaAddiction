package xiaojin.gachaaddiction.util;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

public class RarityUtil {
    public static int getRarityColor(ItemStack itemStack) {
        Rarity rarity = itemStack.getRarity();
        Integer color = rarity.color().getColor();
        if (color != null) {
            return color;
        }
        Integer grayColor = ChatFormatting.GRAY.getColor();
        if (grayColor == null) {
            return 0xFFFFFF;
        }
        return grayColor;
    }

    public static int getRarityLevel(ItemStack itemStack) {
        return itemStack.getRarity().ordinal();
    }
}

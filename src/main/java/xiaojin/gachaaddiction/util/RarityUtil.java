package xiaojin.gachaaddiction.util;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

public class RarityUtil {
    public static int getRarityColor(ItemStack itemStack) {
        if (ConfluenceMagicLibUtil.isLoaded()) {
            var rarity = ConfluenceMagicLibUtil.getRarity(itemStack);
            if (rarity != null) {
                return rarity.color();
            }
        }

        if (RarityCoreUtil.isLoaded()) {
            return RarityCoreUtil.getColor(itemStack);
        }

        Rarity rarity = itemStack.getRarity();
        Integer color = rarity.color().getColor();
        if (color != null) {
            return color;
        }
        return ChatFormatting.GRAY.getColor();
    }

    public static int getRarityLevel(ItemStack itemStack) {
        if (ConfluenceMagicLibUtil.isLoaded()) {
            var rarity = ConfluenceMagicLibUtil.getRarity(itemStack);
            if (rarity != null) {
                return ConfluenceMagicLibUtil.getRarityLevel(rarity);
            }
        }

        if (RarityCoreUtil.isLoaded()) {
            return RarityCoreUtil.getRarity(itemStack);
        }

        return itemStack.getRarity().ordinal();
    }
}

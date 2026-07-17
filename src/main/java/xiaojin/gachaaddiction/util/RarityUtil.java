package xiaojin.gachaaddiction.util;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import xiaojin.gachaaddiction.GachaAddiction;

public class RarityUtil {
    public static int getRarityColor(ItemStack itemStack) {
        if (GachaAddiction.CONFLUENCE_MAGIC_LIB_LOADED) {
            var rarity = ConfluenceMagicLibUtil.getRarity(itemStack);
            if (rarity != null) {
                return rarity.color();
            }
        }

        if (GachaAddiction.RARITYCORE_LOADED) {
            return RarityCoreUtil.getColor(itemStack);
        }

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
        if (GachaAddiction.CONFLUENCE_MAGIC_LIB_LOADED) {
            var rarity = ConfluenceMagicLibUtil.getRarity(itemStack);
            if (rarity != null) {
                return ConfluenceMagicLibUtil.getRarityLevel(rarity);
            }
        }

        if (GachaAddiction.RARITYCORE_LOADED) {
            return RarityCoreUtil.getRarity(itemStack);
        }

        return itemStack.getRarity().ordinal();
    }
}

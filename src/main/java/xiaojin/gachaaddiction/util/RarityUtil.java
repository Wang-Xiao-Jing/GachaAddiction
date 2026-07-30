package xiaojin.gachaaddiction.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

/**
 * 稀有度取值。RarityCore / Confluence Magic Lib 目前没有 Fabric 26.2 版本，
 * 因此只保留原版稀有度逻辑。
 */
public class RarityUtil {
    public static int getRarityColor(ItemStack itemStack) {
        Rarity rarity = itemStack.getRarity();
        // 26.2 移除了 ChatFormatting#getColor，改由 TextColor 提供
        TextColor color = TextColor.fromLegacyFormat(rarity.color());
        if (color != null) {
            return color.getValue();
        }
        TextColor grayColor = TextColor.fromLegacyFormat(ChatFormatting.GRAY);
        if (grayColor == null) {
            return 0xFFFFFF;
        }
        return grayColor.getValue();
    }

    public static int getRarityLevel(ItemStack itemStack) {
        return itemStack.getRarity().ordinal();
    }
}

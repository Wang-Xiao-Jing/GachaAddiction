package xiaojin.gachaaddiction.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import xiaojin.gachaaddiction.registry.GachaRarityProviders;

import java.util.Locale;

public class RarityUtil {
    public static Component getRarityName(ItemStack itemStack) {
        var rarity = GachaRarityProviders.resolve(itemStack);
        if (rarity != null) {
            return rarity.name(itemStack).copy().withColor(rarity.color(itemStack));
        }
        String name = itemStack.getRarity().name().toLowerCase(Locale.ROOT);
        return Component.translatableWithFallback(TranslationKeys.RARITY_NAME_PREFIX + name, name)
                .withColor(getRarityColor(itemStack));
    }

    public static int getRarityColor(ItemStack itemStack) {
        var extension = GachaRarityProviders.resolve(itemStack);
        if (extension != null) {
            return extension.color(itemStack);
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
        var rarity = GachaRarityProviders.resolve(itemStack);
        if (rarity != null) {
            return rarity.level(itemStack);
        }

        return itemStack.getRarity().ordinal();
    }
}

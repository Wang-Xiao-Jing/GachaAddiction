package xiaojin.gachaaddiction.compat.rarity;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.yanbwe.raritycore.api.RarityCoreAPI;
import xiaojin.gachaaddiction.api.IGachaRarityProvider;

public class RarityCoreProvider implements IGachaRarityProvider {
    @Override
    public boolean supports(ItemStack stack) {
        return true;
    }

    @Override
    public Component name(ItemStack stack) {
        return getName(stack);
    }

    @Override
    public int color(ItemStack stack) {
        return getColor(stack);
    }

    @Override
    public int level(ItemStack stack) {
        return getRarity(stack);
    }


    public static int getColor(ItemStack itemStack) {
        return RarityCoreAPI.getColor(getRarity(itemStack));
    }

    public static int getRarity(ItemStack itemStack) {
        return RarityCoreAPI.getRarity(itemStack);
    }

    public static Component getName(ItemStack itemStack) {
        return Component.literal(RarityCoreAPI.getLocalizedTooltip(itemStack));
    }
}

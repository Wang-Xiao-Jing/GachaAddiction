package xiaojin.gachaaddiction.api;

import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;

public interface IGachaRarityProvider {
    boolean supports(ItemStack stack);

    Component name(ItemStack stack);

    int color(ItemStack stack);

    int level(ItemStack stack);
}

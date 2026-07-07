package xiaojin.gachaaddiction.util;

import net.minecraft.world.item.ItemStack;
import org.yanbwe.raritycore.api.RarityCoreAPI;
import xiaojin.gachaaddiction.GachaAddiction;

public class RarityCoreUtil {

    public static int getColor(ItemStack itemStack){
        return RarityCoreAPI.getColor(getRarity(itemStack));
    }
    public static int getRarity(ItemStack itemStack){
        return RarityCoreAPI.getRarity(itemStack);
    }
}

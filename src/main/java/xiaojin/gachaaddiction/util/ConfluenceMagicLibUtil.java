package xiaojin.gachaaddiction.util;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import org.confluence.lib.common.component.ModRarity;
import org.jetbrains.annotations.Nullable;
import xiaojin.gachaaddiction.GachaAddiction;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfluenceMagicLibUtil {
    public static final Map<ModRarity, Integer> RARITYS;

    static {
        RARITYS = Map.ofEntries(
                Map.entry(ModRarity.GRAY, -1),
                Map.entry(ModRarity.WHITE, 0),
                Map.entry(ModRarity.BLUE, 1),
                Map.entry(ModRarity.GREEN, 2),
                Map.entry(ModRarity.ORANGE, 3),
                Map.entry(ModRarity.LIGHT_RED, 4),
                Map.entry(ModRarity.PINK, 5),
                Map.entry(ModRarity.LIGHT_PURPLE, 6),
                Map.entry(ModRarity.LIME, 7),
                Map.entry(ModRarity.YELLOW, 8),
                Map.entry(ModRarity.CYAN, 9),
                Map.entry(ModRarity.RED, 10),
                Map.entry(ModRarity.PURPLE, 11),
                Map.entry(ModRarity.EXPERT, 12),
                Map.entry(ModRarity.MASTER, 13)
        );
    }

    public static boolean isLoaded() {
        return GachaAddiction.CONFLUENCE_MAGIC_LIB_LOADED;
    }

    public static @Nullable ModRarity getRarity(ItemStack itemStack) {
        return getRarity(itemStack, false);
    }

    public static Integer getRarityLevel(ModRarity modRarity) {
        return RARITYS.getOrDefault(modRarity, 0);
    }

    public static @Nullable ModRarity getRarity(ItemStack itemStack, boolean prototype) {
        return ModRarity.getModRarity(itemStack, prototype);
    }
}

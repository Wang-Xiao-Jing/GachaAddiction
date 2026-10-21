package xiaojin.gachaaddiction.compat.rarity;

import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import xiaojin.gachaaddiction.api.IGachaRarityProvider;
import org.confluence.lib.common.component.ModRarity;
import org.jetbrains.annotations.Nullable;
import xiaojin.gachaaddiction.util.TranslationKeys;

import java.util.Map;

public class ConfluenceRarityProvider implements IGachaRarityProvider {
    @Override
    public boolean supports(ItemStack stack) {
        return getRarity(stack) != null;
    }

    @Override
    public Component name(ItemStack stack) {
        ModRarity rarity = getRarity(stack);
        return Component.translatableWithFallback(TranslationKeys.RARITY_NAME_PREFIX + rarity.name(), rarity.name());
    }

    @Override
    public int color(ItemStack stack) {
        return getRarity(stack).color();
    }

    @Override
    public int level(ItemStack stack) {
        return getRarityLevel(getRarity(stack));
    }

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

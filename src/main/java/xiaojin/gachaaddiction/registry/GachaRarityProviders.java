package xiaojin.gachaaddiction.registry;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import xiaojin.gachaaddiction.api.IGachaRarityProvider;

import java.util.ArrayList;
import java.util.List;

public class GachaRarityProviders {
    private static final List<IGachaRarityProvider> PROVIDERS = new ArrayList<>();

    public static void register(IGachaRarityProvider provider) {
        PROVIDERS.add(provider);
    }

    public static @Nullable IGachaRarityProvider resolve(ItemStack stack) {
        for (IGachaRarityProvider provider : PROVIDERS) {
            if (provider.supports(stack)) {
                return provider;
            }
        }

        return null;
    }
}

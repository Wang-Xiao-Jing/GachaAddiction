package xiaojin.gachaaddiction.compat;

import xiaojin.gachaaddiction.GachaModPresence;
import xiaojin.gachaaddiction.compat.lootr.LootrContainerAdapter;
import xiaojin.gachaaddiction.registry.GachaContainerAdapters;
import xiaojin.gachaaddiction.registry.GachaRarityProviders;
import xiaojin.gachaaddiction.compat.rarity.ConfluenceRarityProvider;
import xiaojin.gachaaddiction.compat.rarity.RarityCoreProvider;

public class GachaCompatibility {
    public static void initialize() {
        if (GachaModPresence.CONFLUENCE_MAGIC_LIB_LOADED) {
            GachaRarityProviders.register(new ConfluenceRarityProvider());
        }

        if (GachaModPresence.RARITYCORE_LOADED) {
            GachaRarityProviders.register(new RarityCoreProvider());
        }

        if (GachaModPresence.LOOTR_LOADED) {
            GachaContainerAdapters.register(new LootrContainerAdapter());
        }
    }
}

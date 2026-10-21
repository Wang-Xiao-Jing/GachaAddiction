package xiaojin.gachaaddiction.client.compat;

import xiaojin.gachaaddiction.GachaModPresence;
import xiaojin.gachaaddiction.client.compat.colortooltips.ColorTooltipsBorderRenderer;
import xiaojin.gachaaddiction.client.compat.ldlib2.LDLib2PresentationExtension;
import xiaojin.gachaaddiction.registry.client.GachaClientIntegrations;

public class GachaClientCompatibility {
    public static void initialize() {
        if (GachaModPresence.LDLIB2_LOADED) {
            GachaClientIntegrations.registerPresentation(new LDLib2PresentationExtension());
        }

        if (GachaModPresence.COLORTOOLTIPS_LOADED) {
            GachaClientIntegrations.registerItemBorders(new ColorTooltipsBorderRenderer());
        }
    }
}

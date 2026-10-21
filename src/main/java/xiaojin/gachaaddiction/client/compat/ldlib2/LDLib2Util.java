package xiaojin.gachaaddiction.client.compat.ldlib2;

import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import xiaojin.gachaaddiction.GachaAddiction;
import xiaojin.gachaaddiction.client.compat.ldlib2.element.SearchConfigEntryElement;

public class LDLib2Util {
    private static final String CONFIGURATION_FALLBACK_LOG = "Could not open the LDLib2 configuration page; using the native page";
    private static boolean eventListenersRegistered;

    public static IConfigScreenFactory createConfigScreenFactory() {
        registerEventListeners();
        return (modContainer, parent) -> {
            try {
                return LDLib2ConfigScreen.create(modContainer, parent);
            } catch (RuntimeException | LinkageError failure) {
                GachaAddiction.LOGGER.warn(CONFIGURATION_FALLBACK_LOG, failure);
                return new ConfigurationScreen(modContainer, parent);
            }
        };
    }

    private static void registerEventListeners() {
        if (eventListenersRegistered) {
            return;
        }
        NeoForge.EVENT_BUS.addListener(LDLib2Util::onTagsUpdated);
        eventListenersRegistered = true;
    }

    private static void onTagsUpdated(TagsUpdatedEvent event) {
        SearchConfigEntryElement.invalidateCandidates();
    }
}

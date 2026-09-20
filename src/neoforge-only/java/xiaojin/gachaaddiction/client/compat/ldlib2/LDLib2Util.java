package xiaojin.gachaaddiction.client.compat.ldlib2;

import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import xiaojin.gachaaddiction.client.compat.ldlib2.element.SearchConfigEntryElement;

public class LDLib2Util {
    private static boolean eventListenersRegistered;

    public static IConfigScreenFactory createConfigScreenFactory() {
        registerEventListeners();
        LDLib2ConfigScreen.registerResources();
        return LDLib2ConfigScreen::create;
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

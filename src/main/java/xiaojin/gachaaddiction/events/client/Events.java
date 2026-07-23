package xiaojin.gachaaddiction.events.client;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import xiaojin.gachaaddiction.GachaAddiction;
import xiaojin.gachaaddiction.GachaAddictionConfig;
import xiaojin.gachaaddiction.api.GachaType;
import xiaojin.gachaaddiction.init.GachaTypes;
import xiaojin.gachaaddiction.mixed.IAbstractContainerMenu;
import xiaojin.gachaaddiction.api.ItemStackEntry;
import xiaojin.gachaaddiction.registry.GachaaAdictionConfigData;
import xiaojin.gachaaddiction.util.ModUtil;

import java.util.List;

@EventBusSubscriber(modid = GachaAddiction.MODID, value = Dist.CLIENT)
public class Events {
    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening event) {
        Screen screen = event.getScreen();
        if (!(screen instanceof MenuAccess<?> menuAccess)) {
            return;
        }

        IAbstractContainerMenu iMenu = IAbstractContainerMenu.of(menuAccess.getMenu());
        boolean isInit = iMenu.gachaaddiction$isInit();

        if (isInit) {
            return;
        }

        // 接受类型
        GachaType gachaType = GachaTypes.EMPTY;

        if (gachaType.isEmpty()) {
            gachaType = GachaAddictionConfig.CLIENT.getDefaultGachaaType();
            if (gachaType.isEmpty()) {
                return;
            }
        }

        List<ItemStackEntry> entries = iMenu.gachaaddiction$getDisplayEntries();
        if (entries.isEmpty()) {
            return;
        }

        event.setNewScreen(gachaType.getClientData()
                .create(screen, ModUtil.of(iMenu.gachaaddiction$getLootTableKey()), entries));
    }

    @SubscribeEvent
    public static void onRegisterClientReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(GachaaAdictionConfigData.INSTANCE);
    }
}

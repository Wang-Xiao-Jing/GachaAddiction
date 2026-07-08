package xiaojin.gachaaddiction.events.client;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import xiaojin.gachaaddiction.GachaAddiction;
import xiaojin.gachaaddiction.client.gui.screen.GachaScreen;
import xiaojin.gachaaddiction.mixed.IAbstractContainerMenu;

import java.util.List;

@EventBusSubscriber(modid = GachaAddiction.MODID, value = Dist.CLIENT)
public class Events {
    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening event) {
        Screen screen = event.getScreen();
        if (!(screen instanceof MenuAccess<?> menuAccess)) {
            return;
        }

        AbstractContainerMenu menu = menuAccess.getMenu();
        IAbstractContainerMenu iMenu = IAbstractContainerMenu.of(menu);
        boolean isInit = iMenu.gachaaddiction$isInit();

        if (isInit) {
            return;
        }

        var entries = iMenu.gachaaddiction$getDisplayEntries();
        if (entries == null || entries.isEmpty()) {
            return;
        }
        List<ResourceKey<LootTable>> lootTableResourceKey = iMenu.gachaaddiction$getLootTableKey();
        GachaScreen newScreen = new GachaScreen(screen, lootTableResourceKey, entries);
        event.setNewScreen(newScreen);
    }
}

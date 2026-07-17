package xiaojin.gachaaddiction.events.client;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiaojin.gachaaddiction.GachaAddiction;
import xiaojin.gachaaddiction.client.gui.screen.SlotMachineScreen;
import xiaojin.gachaaddiction.mixed.IAbstractContainerMenu;
import xiaojin.gachaaddiction.api.ItemStackEntry;
import xiaojin.gachaaddiction.util.ModUtil;

import java.util.List;
import java.util.Objects;

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

        List<ItemStackEntry> entries = iMenu.gachaaddiction$getDisplayEntries();
        if (entries == null || entries.isEmpty()) {
            return;
        }
        List<@Nullable ResourceKey<LootTable>> lootTableResourceKey = iMenu.gachaaddiction$getLootTableKey();
        List<@Nullable ResourceLocation> locationList = ModUtil.of(lootTableResourceKey);
        SlotMachineScreen newScreen = new SlotMachineScreen(screen, locationList, entries);
        event.setNewScreen(newScreen);
    }
}

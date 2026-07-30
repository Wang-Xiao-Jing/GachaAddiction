package xiaojin.gachaaddiction.util.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import xiaojin.gachaaddiction.GachaAddictionConfig;
import xiaojin.gachaaddiction.client.gui.screen.BasicGachaScreen;
import xiaojin.gachaaddiction.mixin.accessor.ScreenAccessor;

public class ModClientUtil {
    public static ScreenAccessor of(Screen screen) {
        return (ScreenAccessor) screen;
    }

    /**
     * 取容器里属于战利品的物品（排除玩家背包），交给抽奖界面。
     *
     * @param closeIfNoMatch 容器内容确定已到达时传 true：没有可展示的物品就直接关掉抽奖界面。
     *                       内容可能还没到时传 false，交给
     *                       {@code ClientPacketListenerMixin} 之后再处理。
     */
    public static void applyContainerRewards(BasicGachaScreen screen, AbstractContainerMenu menu, boolean closeIfNoMatch) {
        Minecraft minecraft = Minecraft.getInstance();
        NonNullList<ItemStack> menuItems = menu.getItems();

        LocalPlayer localPlayer = minecraft.player;
        if (localPlayer != null) {
            menuItems.removeAll(localPlayer.inventoryMenu.getItems());
        }
        menuItems.removeIf(ItemStack::isEmpty);

        if (menuItems.isEmpty() && !closeIfNoMatch) {
            // 内容包还没到，等它到了再走 handleContainerContent
            return;
        }

        if (menuItems.stream().noneMatch(GachaAddictionConfig.CLIENT::filter)) {
            if (closeIfNoMatch) {
                screen.onClose();
            }
            return;
        }

        screen.updateRewards(menuItems);
    }
}

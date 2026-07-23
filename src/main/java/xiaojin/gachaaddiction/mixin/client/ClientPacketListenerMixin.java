package xiaojin.gachaaddiction.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xiaojin.gachaaddiction.GachaAddictionConfig;
import xiaojin.gachaaddiction.client.gui.screen.BasicGachaScreen;

import java.util.List;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
    @WrapOperation(method = "handleContainerContent",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/AbstractContainerMenu;initializeContents(ILjava/util/List;Lnet/minecraft/world/item/ItemStack;)V"))
    private void gachaaddiction$onHandleContainerContent(AbstractContainerMenu instance, int stateId, List<ItemStack> items, ItemStack carried, Operation<Void> original) {
        original.call(instance, stateId, items, carried);
        Minecraft mc = Minecraft.getInstance();
        Screen screen = mc.screen;
        if (screen == null) {
            return;
        }

        if (!(screen instanceof BasicGachaScreen slotMachineScreen)) {
            return;
        }

        if (!(slotMachineScreen.getOriginalScreen() instanceof MenuAccess<?> menuAccess)) {
            return;
        }

        AbstractContainerMenu menu = menuAccess.getMenu();
        if (menu.containerId != instance.containerId) {
            return;
        }

        NonNullList<ItemStack> menuItems = menu.getItems();
        // TODO 或许可以优化
        // 排除玩家本身的物品
        LocalPlayer localPlayer = mc.player;
        if (localPlayer != null) {
            menuItems.removeAll(localPlayer.inventoryMenu.getItems());
        }
        menuItems.removeIf(ItemStack::isEmpty);

        if (menuItems.stream().noneMatch(GachaAddictionConfig.CLIENT::filter)) {
            slotMachineScreen.onClose();
            return;
        }

        slotMachineScreen.updateRewards(menuItems);
    }
}

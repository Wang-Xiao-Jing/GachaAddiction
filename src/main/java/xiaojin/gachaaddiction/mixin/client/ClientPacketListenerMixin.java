package xiaojin.gachaaddiction.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xiaojin.gachaaddiction.client.gui.screen.GachaScreen;
import xiaojin.gachaaddiction.mixed.IAbstractContainerMenu;

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
        if (screen instanceof GachaScreen gachaScreen) {
            gachaScreen.initializeContents();
        }
    }
}

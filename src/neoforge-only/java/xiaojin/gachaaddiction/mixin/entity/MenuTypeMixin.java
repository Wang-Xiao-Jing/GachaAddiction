package xiaojin.gachaaddiction.mixin.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.IContainerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xiaojin.gachaaddiction.api.GachaaData;
import xiaojin.gachaaddiction.mixed.IAbstractContainerMenu;

import java.util.Optional;

@Mixin(MenuType.class)
public abstract class MenuTypeMixin<T extends AbstractContainerMenu> implements FeatureElement, IMenuTypeExtension<T> {

    @WrapOperation(method = "create(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/network/RegistryFriendlyByteBuf;)Lnet/minecraft/world/inventory/AbstractContainerMenu;",
            at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/network/IContainerFactory;create(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/network/RegistryFriendlyByteBuf;)Lnet/minecraft/world/inventory/AbstractContainerMenu;"))
    private T gachaaddiction$hookNeoForgeFactory$create(IContainerFactory<?> factory, int containerId, Inventory inventory, RegistryFriendlyByteBuf buf, Operation<T> original,
                                                        @Share("processed") LocalBooleanRef processed) {
        processed.set(true);
        Optional<GachaaData> gachaaData = GachaaData.OPTIONAL_STREAM_CODEC.decode(buf);
        T menu = original.call(factory, containerId, inventory, buf);
        gachaaData.ifPresent(gachaaData1 -> IAbstractContainerMenu.of(menu).gachaaddiction$setGachaaData(gachaaData1));
        return menu;
    }

    @WrapOperation(method = "create(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/network/RegistryFriendlyByteBuf;)Lnet/minecraft/world/inventory/AbstractContainerMenu;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/MenuType;create(ILnet/minecraft/world/entity/player/Inventory;)Lnet/minecraft/world/inventory/AbstractContainerMenu;"))
    private T gachaaddiction$hookVanillaFactory$create(MenuType<?> instance, int containerId, Inventory playerInventory, Operation<T> original,
                                                       @Share("processed") LocalBooleanRef processed, @Local(argsOnly = true) RegistryFriendlyByteBuf buf) {
        // 避免重复读取
        if (processed.get()) {
            return original.call(instance, containerId, playerInventory);
        }
        processed.set(true);
        Optional<GachaaData> gachaaData = GachaaData.OPTIONAL_STREAM_CODEC.decode(buf);
        T menu = original.call(instance, containerId, playerInventory);
        gachaaData.ifPresent(gachaaData1 -> IAbstractContainerMenu.of(menu).gachaaddiction$setGachaaData(gachaaData1));
        return menu;
    }
}

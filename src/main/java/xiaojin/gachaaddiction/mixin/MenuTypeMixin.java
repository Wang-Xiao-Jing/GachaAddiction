package xiaojin.gachaaddiction.mixin;

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
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import xiaojin.gachaaddiction.mixed.IAbstractContainerMenu;
import xiaojin.gachaaddiction.api.ItemStackEntry;
import xiaojin.gachaaddiction.util.ModUtil;

@Mixin(MenuType.class)
public abstract class MenuTypeMixin<T extends AbstractContainerMenu> implements FeatureElement, IMenuTypeExtension<T> {

    private record LootFlags(byte raw, boolean isRandomizable, boolean isInit, boolean hasLoot) {
        static LootFlags read(RegistryFriendlyByteBuf buf) {
            byte b = buf.readByte();
            return new LootFlags(b, (b & 0b001) != 0, (b & 0b010) != 0, (b & 0b100) != 0);
        }
    }

    @WrapOperation(method = "create(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/network/RegistryFriendlyByteBuf;)Lnet/minecraft/world/inventory/AbstractContainerMenu;",
            at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/network/IContainerFactory;create(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/network/RegistryFriendlyByteBuf;)Lnet/minecraft/world/inventory/AbstractContainerMenu;"))
    private T gachaaddiction$hookNeoForgeFactory(IContainerFactory<?> factory, int containerId, Inventory inventory, RegistryFriendlyByteBuf buf, Operation<T> original,
                                                 @Share("processed") LocalBooleanRef processed) {
        processed.set(true);
        LootFlags flags = LootFlags.read(buf);
        T menu = original.call(factory, containerId, inventory, buf);
        gachaaddiction$$applyLootData(menu, flags, buf);
        return menu;
    }

    @WrapOperation(method = "create(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/network/RegistryFriendlyByteBuf;)Lnet/minecraft/world/inventory/AbstractContainerMenu;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/MenuType;create(ILnet/minecraft/world/entity/player/Inventory;)Lnet/minecraft/world/inventory/AbstractContainerMenu;"))
    private T gachaaddiction$hookVanillaFactory(MenuType<?> instance, int containerId, Inventory playerInventory, Operation<T> original,
                                                @Share("processed") LocalBooleanRef processed, @Local(argsOnly = true) RegistryFriendlyByteBuf buf) {
        if (processed.get()) {
            return original.call(instance, containerId, playerInventory);
        }
        processed.set(true);
        LootFlags flags = LootFlags.read(buf);
        T menu = original.call(instance, containerId, playerInventory);
        gachaaddiction$$applyLootData(menu, flags, buf);
        return menu;
    }

    @Unique
    private static <T extends AbstractContainerMenu> void gachaaddiction$$applyLootData(T menu, LootFlags flags, RegistryFriendlyByteBuf buf) {
        IAbstractContainerMenu iMenu = IAbstractContainerMenu.of(menu);
        iMenu.gachaaddiction$setIsInit(!flags.isRandomizable() || flags.isInit());

        if (!flags.isRandomizable() || flags.isInit() || !flags.hasLoot()) {
            return;
        }

        iMenu.gachaaddiction$setLootTableKey(ModUtil.LOOT_TABLE_KEY_LIST_STREAM_CODEC.decode(buf));
        iMenu.gachaaddiction$setDisplayEntries(ItemStackEntry.LIST_STREAM_CODEC.decode(buf));
    }
}

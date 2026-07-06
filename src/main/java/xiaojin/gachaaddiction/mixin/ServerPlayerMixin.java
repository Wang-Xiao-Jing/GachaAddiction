package xiaojin.gachaaddiction.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xiaojin.gachaaddiction.mixed.IAbstractContainerMenu;
import xiaojin.gachaaddiction.util.DisplayEntry;
import xiaojin.gachaaddiction.util.LootDisplayCache;
import xiaojin.gachaaddiction.util.LootrUtil;
import xiaojin.gachaaddiction.util.ModUtil;

import java.util.function.Consumer;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {

    public ServerPlayerMixin(Level level, BlockPos pos, float yRot, GameProfile gameProfile) {
        super(level, pos, yRot, gameProfile);
    }

    @Inject(method = "lambda$openMenu$15", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/MenuProvider;writeClientSideData(Lnet/minecraft/world/inventory/AbstractContainerMenu;Lnet/minecraft/network/RegistryFriendlyByteBuf;)V"))
    private static void gachaaddiction$openMenu(MenuProvider menu, AbstractContainerMenu abstractcontainermenu, Consumer<RegistryFriendlyByteBuf> extraDataWriter, RegistryFriendlyByteBuf buffer, CallbackInfo ci) {
        if (!(menu instanceof RandomizableContainer) && !LootrUtil.isInstanceofILootrInventory(menu)) {
            buffer.writeByte(0b000);
            return;
        }

        IAbstractContainerMenu iMenu = IAbstractContainerMenu.of(abstractcontainermenu);
        boolean isInit = iMenu.gachaaddiction$isInit();
        ResourceKey<LootTable> lootTableKey = iMenu.gachaaddiction$getLootTableKey();
        boolean isLootEmpty = lootTableKey == null;
        buffer.writeByte(0b001 | (isInit ? 0b010 : 0b000) | (isLootEmpty ? 0b000 : 0b100));

        if (isInit || isLootEmpty) {
            return;
        }

        ModUtil.LOOT_TABLE_KEY_STREAM_CODEC.encode(buffer, lootTableKey);
        DisplayEntry.LIST_STREAM_CODEC.encode(buffer, LootDisplayCache.get(lootTableKey));
    }

    @WrapOperation(method = "openMenu(Lnet/minecraft/world/MenuProvider;Ljava/util/function/Consumer;)Ljava/util/OptionalInt;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/MenuProvider;createMenu(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/inventory/AbstractContainerMenu;"))
    private AbstractContainerMenu gachaaddiction$openMenu(
            MenuProvider instance,
            int i,
            Inventory inventory,
            Player player,
            Operation<AbstractContainerMenu> original
    ) {
        boolean isInit = true;
        ResourceKey<LootTable> lootTableKey = null;

        if (instance instanceof RandomizableContainer randomizableContainer) {
            lootTableKey = randomizableContainer.getLootTable();
            isInit = lootTableKey == null;
        } else if (LootrUtil.isInstanceofILootrInventory(instance)) {
            lootTableKey = LootrUtil.getInfoLootTable(instance);
            isInit = LootrUtil.isOpen(instance);
        }

        AbstractContainerMenu menu = original.call(instance, i, inventory, player);
        IAbstractContainerMenu iMenu = IAbstractContainerMenu.of(menu);

        iMenu.gachaaddiction$setIsInit(isInit);
        iMenu.gachaaddiction$setLootTableKey(lootTableKey);

        return menu;
    }
}

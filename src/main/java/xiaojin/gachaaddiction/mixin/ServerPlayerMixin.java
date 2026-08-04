package xiaojin.gachaaddiction.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xiaojin.gachaaddiction.api.GachaaData;
import xiaojin.gachaaddiction.mixed.IAbstractContainerMenu;
import xiaojin.gachaaddiction.api.MenuProviderData;
import xiaojin.gachaaddiction.util.LootDisplayCache;
import xiaojin.gachaaddiction.util.ModUtil;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {

    public ServerPlayerMixin(Level level, BlockPos pos, float yRot, GameProfile gameProfile) {
        super(level, pos, yRot, gameProfile);
    }

    @Inject(method = "lambda$openMenu$15", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/MenuProvider;writeClientSideData(Lnet/minecraft/world/inventory/AbstractContainerMenu;Lnet/minecraft/network/RegistryFriendlyByteBuf;)V"))
    private static void gachaaddiction$openMenu$writeClientSideData(MenuProvider menu, AbstractContainerMenu abstractcontainermenu, Consumer<RegistryFriendlyByteBuf> extraDataWriter, RegistryFriendlyByteBuf buffer, CallbackInfo ci) {
        if (!MenuProviderData.isLootrContainer(menu)) {
            // TODO 自定义GachaType
            GachaaData.OPTIONAL_STREAM_CODEC.encode(buffer, Optional.of(new GachaaData(false, Optional.empty(), Optional.empty(), Optional.empty())));
            return;
        }

        IAbstractContainerMenu iMenu = IAbstractContainerMenu.of(abstractcontainermenu);
        GachaaData.OPTIONAL_STREAM_CODEC.encode(buffer, Optional.ofNullable(iMenu.gachaaddiction$getGachaaData()));
    }

    @WrapOperation(method = "openMenu(Lnet/minecraft/world/MenuProvider;Ljava/util/function/Consumer;)Ljava/util/OptionalInt;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/MenuProvider;createMenu(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/inventory/AbstractContainerMenu;"))
    private AbstractContainerMenu gachaaddiction$openMenu$createMenu(
            MenuProvider instance,
            int i,
            Inventory inventory,
            Player player,
            Operation<AbstractContainerMenu> original
    ) {
        if (!MenuProviderData.isLootrContainer(instance)) {
            return original.call(instance, i, inventory, player);
        }
        AbstractContainerMenu menu = original.call(instance, i, inventory, player);
        IAbstractContainerMenu iMenu = IAbstractContainerMenu.of(menu);
        MenuProviderData data = MenuProviderData.create(instance);
        List<@Nullable ResourceKey<LootTable>> lootTableKey = data.lootTable();
        // TODO 自定义GachaType
        iMenu.gachaaddiction$setGachaaData(new GachaaData(data.isOpenGachaaUi(), Optional.empty(), Optional.of(ModUtil.of(lootTableKey)), Optional.of(LootDisplayCache.get(lootTableKey))));
        MenuProviderData.open(instance);
        return menu;
    }
}

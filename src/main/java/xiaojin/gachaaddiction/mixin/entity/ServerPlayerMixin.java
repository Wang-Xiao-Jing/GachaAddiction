package xiaojin.gachaaddiction.mixin.entity;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xiaojin.gachaaddiction.api.GachaaData;
import xiaojin.gachaaddiction.api.MenuProviderData;
import xiaojin.gachaaddiction.mixed.IAbstractContainerMenu;
import xiaojin.gachaaddiction.network.GachaaDataPayload;
import xiaojin.gachaaddiction.util.LootDisplayCache;
import xiaojin.gachaaddiction.util.ModUtil;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {

    public ServerPlayerMixin(Level level, BlockPos pos, float yRot, GameProfile gameProfile) {
        super(level, pos, yRot, gameProfile);
    }

    @Inject(method = "openMenu", at = @At("RETURN"))
    private void gachaaddiction$openMenu(@Nullable MenuProvider menuProvider, CallbackInfoReturnable<OptionalInt> cir) {
        if (menuProvider == null) {
            return;
        }

        OptionalInt result = cir.getReturnValue();
        if (result == null || result.isEmpty()) {
            return;
        }

        AbstractContainerMenu menu = this.containerMenu;
        if (menu == null || menu.containerId != result.getAsInt()) {
            return;
        }

        GachaaData data;
        if (MenuProviderData.isLootrContainer(menuProvider)) {
            MenuProviderData providerData = MenuProviderData.create(menuProvider);
            List<@Nullable ResourceKey<LootTable>> lootTableKey = providerData.lootTable();
            data = new GachaaData(
                    providerData.isOpenGachaaUi(),
                    Optional.empty(),
                    Optional.of(ModUtil.of(lootTableKey)),
                    Optional.of(LootDisplayCache.get(lootTableKey)));
            IAbstractContainerMenu.of(menu).gachaaddiction$setGachaaData(data);
            MenuProviderData.open(menuProvider);
        } else {
            data = new GachaaData(false, Optional.empty(), Optional.empty(), Optional.empty());
        }

        ServerPlayNetworking.send((ServerPlayer) (Object) this, new GachaaDataPayload(menu.containerId, Optional.of(data)));
    }
}

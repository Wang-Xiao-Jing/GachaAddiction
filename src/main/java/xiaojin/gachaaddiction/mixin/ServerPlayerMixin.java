package xiaojin.gachaaddiction.mixin;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
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
import xiaojin.gachaaddiction.network.SyncGachaaDataPayload;
import xiaojin.gachaaddiction.util.LootDisplayCache;
import xiaojin.gachaaddiction.util.ModUtil;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {

    public ServerPlayerMixin(Level level, GameProfile gameProfile) {
        super(level, gameProfile);
    }

    /**
     * 界面打开后补发抽奖数据。原版 26.2 没有随开界面包携带额外数据的通道，
     * 所以在这里单独发一个 {@link SyncGachaaDataPayload}。
     */
    @Inject(method = "openMenu(Lnet/minecraft/world/MenuProvider;)Ljava/util/OptionalInt;", at = @At("RETURN"))
    private void gachaaddiction$openMenu(MenuProvider provider, CallbackInfoReturnable<OptionalInt> cir) {
        OptionalInt result = cir.getReturnValue();
        if (result == null || result.isEmpty()) {
            return;
        }
        if (!MenuProviderData.isLootrContainer(provider)) {
            return;
        }

        AbstractContainerMenu menu = this.containerMenu;
        if (menu == null || menu.containerId != result.getAsInt()) {
            return;
        }

        MenuProviderData data = MenuProviderData.create(provider);
        List<@Nullable ResourceKey<LootTable>> lootTableKey = data.lootTable();
        // TODO 自定义GachaType
        GachaaData gachaaData = new GachaaData(
                data.isOpenGachaaUi(),
                Optional.empty(),
                Optional.of(ModUtil.of(lootTableKey)),
                Optional.of(LootDisplayCache.get(lootTableKey))
        );

        IAbstractContainerMenu.of(menu).gachaaddiction$setGachaaData(gachaaData);
        MenuProviderData.open(provider);

        ServerPlayNetworking.send((ServerPlayer) (Object) this, new SyncGachaaDataPayload(menu.containerId, gachaaData));
    }
}

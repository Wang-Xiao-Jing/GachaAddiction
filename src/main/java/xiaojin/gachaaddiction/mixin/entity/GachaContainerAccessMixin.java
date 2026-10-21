package xiaojin.gachaaddiction.mixin.entity;

import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xiaojin.gachaaddiction.presentation.GachaPresentations;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class GachaContainerAccessMixin {
    @Shadow
    public ServerPlayer player;

    @Inject(method = "handleContainerClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;resetLastActionTime()V"), cancellable = true)
    private void gachaaddiction$denyLockedClick(ServerboundContainerClickPacket packet, CallbackInfo ci) {
        if (!GachaPresentations.mayAccess(player, player.containerMenu) || GachaPresentations.isForced(player)) {
            player.containerMenu.sendAllDataToRemote();
            ci.cancel();
        }
    }

    @Inject(method = "handleContainerClose", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;doCloseContainer()V"), cancellable = true)
    private void gachaaddiction$denyForcedClose(ServerboundContainerClosePacket packet, CallbackInfo ci) {
        if (GachaPresentations.isForced(player)) {
            ci.cancel();
        }
    }
}

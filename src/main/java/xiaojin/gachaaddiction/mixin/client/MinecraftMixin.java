package xiaojin.gachaaddiction.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xiaojin.gachaaddiction.client.gui.screen.GachaScreen;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Inject(method = "setScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;removed()V"))
    private void gachaaddiction$setScreen(
            Screen guiScreen,
            CallbackInfo ci,
            @Local(ordinal = 1) Screen old,
            @Local(ordinal = 0, argsOnly = true) LocalRef<Screen> guiScreen1) {
        if (!(old instanceof GachaScreen gachaScreen)) {
            return;
        }

        Screen originalScreen = gachaScreen.getOriginalScreen();
        if (originalScreen == null) {
            return;
        }

        guiScreen1.set(originalScreen);
    }

//    @WrapOperation(method = "setScreen",
//            at = @At(value = "FIELD",
//                    target = "Lnet/minecraft/client/Minecraft;screen:Lnet/minecraft/client/gui/screens/Screen;",
//                    opcode = Opcodes.PUTFIELD))
//    private void gachaaddiction$setScreen1(
//            Minecraft instance,
//            Screen value,
//            Operation<Void> original,
//            @Local(ordinal = 0, argsOnly = true) LocalRef<Screen> guiScreen) {
//        if (!(value instanceof GachaScreen gachaScreen)) {
//            original.call(instance, value);
//            return;
//        }
//
//        Screen originalScreen = gachaScreen.getOriginalScreen();
//        if (originalScreen == null) {
//            original.call(instance, value);
//            return;
//        }
//
//        guiScreen.set(originalScreen);
//        original.call(instance, originalScreen);
//    }
}

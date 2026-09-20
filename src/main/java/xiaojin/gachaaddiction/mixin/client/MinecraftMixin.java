package xiaojin.gachaaddiction.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xiaojin.gachaaddiction.client.gui.screen.BasicGachaScreen;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow
    public @Nullable Screen screen;

    @ModifyVariable(
            method = "setScreen(Lnet/minecraft/client/gui/screens/Screen;)V",
            at = @At("HEAD"),
            argsOnly = true)
    private Screen gachaaddiction$setScreen(Screen guiScreen) {
        if (!(this.screen instanceof BasicGachaScreen slotMachineScreen)) {
            return guiScreen;
        }

        Screen originalScreen = slotMachineScreen.getOriginalScreen();
        if (originalScreen == null) {
            return guiScreen;
        }

        if (!slotMachineScreen.isReturnOriginalScreen()) {
            return guiScreen;
        }

        return originalScreen;
    }
}

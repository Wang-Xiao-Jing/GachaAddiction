package xiaojin.gachaaddiction.mixin.client;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xiaojin.gachaaddiction.client.gui.screen.BasicGachaScreen;

/**
 * 抽奖界面关闭后回到原来的容器界面。
 * <p>
 * 26.2 把界面管理从 {@code Minecraft} 挪到了 {@link Gui}，
 * 且旧界面从字段 {@code screen} 读取而不再是局部变量，
 * 所以改成在 HEAD 处直接替换入参。
 */
@Mixin(Gui.class)
public abstract class GuiMixin {
    @Shadow
    private Screen screen;

    @ModifyVariable(method = "setScreen", at = @At("HEAD"), argsOnly = true)
    private Screen gachaaddiction$setScreen(Screen newScreen) {
        if (!(this.screen instanceof BasicGachaScreen gachaScreen)) {
            return newScreen;
        }

        Screen originalScreen = gachaScreen.getOriginalScreen();
        if (originalScreen == null) {
            return newScreen;
        }

        if (gachaScreen.isReturnOriginalScreen()) {
            return originalScreen;
        }
        return newScreen;
    }
}

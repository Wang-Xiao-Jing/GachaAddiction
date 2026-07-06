package xiaojin.gachaaddiction.util.client;

import net.minecraft.client.gui.screens.Screen;
import xiaojin.gachaaddiction.mixin.accessor.ScreenAccessor;

public class ModClientUtil {
    public static ScreenAccessor of(Screen screen) {
        return (ScreenAccessor) screen;
    }
}

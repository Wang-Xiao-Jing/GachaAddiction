package xiaojin.gachaaddiction.client.compat.ldlib2;

import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

public final class LDLib2GachaDesignerScreen {

    private LDLib2GachaDesignerScreen() {
    }

    public static Screen create(Screen parent) {
        return LDLib2LocalEditorScreen.openDesigner(parent);
    }

    public static @Nullable Screen parentOf(Screen screen) {
        return screen instanceof LDLib2LocalEditorScreen ? LDLib2LocalEditorScreen.parentOf(screen) : null;
    }

    public static void minimizeIfActive(Screen screen) {
        LDLib2LocalEditorScreen.minimizeIfActive(screen);
    }
}

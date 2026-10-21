package xiaojin.gachaaddiction.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModList;
import xiaojin.gachaaddiction.client.compat.ldlib2.LDLib2LocalEditorScreen;
import xiaojin.gachaaddiction.util.TranslationKeys;

public final class AdminWorkbenchClient {
    private AdminWorkbenchClient() {
    }

    public static void open() {
        Minecraft minecraft = Minecraft.getInstance();
        if (!ModList.get().isLoaded("ldlib2")) {
            SystemToast.add(minecraft.getToasts(), SystemToast.SystemToastId.PACK_LOAD_FAILURE,
                    Component.translatable(TranslationKeys.GUI_LOCAL_PACK_TITLE),
                    Component.translatable(TranslationKeys.GUI_GACHA_DESIGNER_REQUIRES_LDLIB));
            return;
        }
        try {
            minecraft.setScreen(LDLib2LocalEditorScreen.create(minecraft.screen));
        } catch (RuntimeException failure) {
            SystemToast.add(minecraft.getToasts(), SystemToast.SystemToastId.PACK_LOAD_FAILURE,
                    Component.translatable(TranslationKeys.GUI_LOCAL_PACK_TITLE),
                    Component.translatable(TranslationKeys.GUI_LOCAL_OPERATION_FAILED, failure.getMessage()));
        }
    }

}

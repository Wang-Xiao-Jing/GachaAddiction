package xiaojin.gachaaddiction.client.workspace;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import xiaojin.gachaaddiction.registry.client.GachaClientIntegrations;
import xiaojin.gachaaddiction.util.TranslationKeys;

public final class AdminWorkbenchClient {
    private AdminWorkbenchClient() {
    }

    public static void open() {
        Minecraft minecraft = Minecraft.getInstance();
        var extension = GachaClientIntegrations.presentation();
        if (extension == null) {
            SystemToast.add(minecraft.getToasts(), SystemToast.SystemToastId.PACK_LOAD_FAILURE,
                    Component.translatable(TranslationKeys.GUI_LOCAL_PACK_TITLE),
                    Component.translatable(TranslationKeys.GUI_GACHA_DESIGNER_REQUIRES_LDLIB));
            return;
        }
        try {
            minecraft.setScreen(extension.createWorkbench(minecraft.screen));
        } catch (RuntimeException failure) {
            SystemToast.add(minecraft.getToasts(), SystemToast.SystemToastId.PACK_LOAD_FAILURE,
                    Component.translatable(TranslationKeys.GUI_LOCAL_PACK_TITLE),
                    Component.translatable(TranslationKeys.GUI_LOCAL_OPERATION_FAILED, failure.getMessage()));
        }
    }

}

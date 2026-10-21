package xiaojin.gachaaddiction.client.compat.ldlib2;

import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.GachaAddiction;

public class LDLib2AdminUiConstants {
    public static final ResourceLocation WINDOW_ID = ResourceLocation.fromNamespaceAndPath(
            GachaAddiction.MODID, "admin_editor");
    public static final String LDLIB2_TITLE_SUFFIX = " (LDLib2)";
    public static final String LINE_SEPARATOR = "\n";
    public static final String CHANGED_MARKER = " *";
    public static final int CONTENT_GAP = 4;
    public static final int TOP_BUTTON_HEIGHT = 12;
    public static final int DOCUMENT_HEIGHT = 220;
    public static final int DIALOG_WIDTH = 220;
    public static final int ERROR_COLOR = 0xFF5555;

    private LDLib2AdminUiConstants() {
    }
}

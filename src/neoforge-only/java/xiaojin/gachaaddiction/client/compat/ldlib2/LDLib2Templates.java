package xiaojin.gachaaddiction.client.compat.ldlib2;

import com.lowdragmc.lowdraglib2.editor.resource.IResourcePath;

public final class LDLib2Templates {
    public static final IResourcePath CONFIG_SCREEN = path("config_screen.ui.nbt");
    public static final IResourcePath FIELD_CONFIG_ENTRY = path("field_config_entry.ui.nbt");
    public static final IResourcePath LIST_CONFIG_ENTRY = path("list_config_entry.ui.nbt");
    public static final IResourcePath LIST_CONFIG_SCREEN = path("list_config_screen.ui.nbt");
    public static final IResourcePath SEARCH_CONFIG_ENTRY = path("search_config_entry.ui.nbt");
    public static final IResourcePath SELECTOR_CONFIG_ENTRY = path("selector_config_entry.ui.nbt");
    public static final IResourcePath SWITCH_CONFIG_ENTRY = path("switch_config_entry.ui.nbt");

    private static final String ROOT = "file(./ldlib2/assets/gachaaddiction/";

    private LDLib2Templates() {
    }

    private static IResourcePath path(String fileName) {
        return IResourcePath.parse(ROOT + fileName + ")");
    }
}

package xiaojin.gachaaddiction.client.compat.ldlib2;

import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.GachaAddiction;

public class LDLib2GachaUiConstants {
    public static final String GACHA_TYPE_PATH = "ldlib2";
    public static final ResourceLocation DEFAULT_TEMPLATE_ID = ResourceLocation.fromNamespaceAndPath(
            GachaAddiction.MODID, "ui/gacha_screen.ui.nbt");
    public static final ResourceLocation DEFAULT_STYLESHEET_ID = ResourceLocation.fromNamespaceAndPath(
            GachaAddiction.MODID, "lss/gacha_screen.lss");
    public static final String TITLE_ID = "gacha_title";
    public static final String CONTEXT_ID = "gacha_context";
    public static final String REWARDS_ID = "gacha_rewards";
    public static final String PREVIOUS_ID = "gacha_previous";
    public static final String NEXT_ID = "gacha_next";
    public static final String SKIP_ID = "gacha_skip";
    public static final String CLOSE_ID = "gacha_close";
    public static final String REWARDS_ROW_CLASS = "gacha-rewards-row";
    public static final String REWARD_CLASS = "gacha-reward";
    public static final int REWARDS_PER_PAGE = 9;
    public static final int REWARD_SIZE = 32;
    public static final int REWARD_PADDING = 8;
    public static final int MINIMUM_REEL_HEIGHT = 96;
    public static final long MAXIMUM_TEMPLATE_BYTES = 1_048_576;
    public static final int MAXIMUM_STYLESHEET_BYTES = 1_048_576;
    public static final String RESOURCE_TYPE_KEY = "type";
    public static final String RESOURCE_DATA_KEY = "data";
    public static final String RESOURCE_UI_TYPE = "ui";
    public static final String TEMPLATE_MISSING = "Gacha UI template resource is missing: ";
    public static final String TEMPLATE_INVALID = "Gacha UI template resource is invalid: ";
    public static final String TEMPLATE_TOO_LARGE = "Gacha UI template exceeds the resource limit: ";
    public static final String STYLESHEET_MISSING = "Gacha UI stylesheet resource is missing: ";
    public static final String STYLESHEET_TOO_LARGE = "Gacha UI stylesheet exceeds the resource limit: ";
    public static final String FALLBACK_LOG = "Could not open LDLib2 gacha presentation; using the native screen";

    private LDLib2GachaUiConstants() {
    }
}

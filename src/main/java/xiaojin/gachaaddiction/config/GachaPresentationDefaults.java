package xiaojin.gachaaddiction.config;

import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.GachaConstants;

public class GachaPresentationDefaults {
    public static final ResourceLocation DEFAULT_TEMPLATE_ID = ResourceLocation.fromNamespaceAndPath(
            GachaConstants.MODID, "ui/gacha_screen.ui.nbt");
    public static final ResourceLocation DEFAULT_STYLESHEET_ID = ResourceLocation.fromNamespaceAndPath(
            GachaConstants.MODID, "lss/gacha_screen.lss");
}

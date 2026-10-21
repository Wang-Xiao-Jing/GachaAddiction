package xiaojin.gachaaddiction.reward.provenance;

import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.GachaConstants;

public final class GachaRewardOriginTypes {
    public static final ResourceLocation SOURCE = ResourceLocation.fromNamespaceAndPath(GachaConstants.MODID, "source");
    public static final ResourceLocation VANILLA_LOOT_TABLE = ResourceLocation.withDefaultNamespace("loot_table");
    public static final ResourceLocation CUSTOM_TABLE = ResourceLocation.fromNamespaceAndPath(GachaConstants.MODID,
            "custom_table");
    public static final ResourceLocation CUSTOM_ENTRY = ResourceLocation.fromNamespaceAndPath(GachaConstants.MODID,
            "custom_entry");
    public static final ResourceLocation RULE_ACTION = ResourceLocation.fromNamespaceAndPath(GachaConstants.MODID,
            "rule_action");

    private GachaRewardOriginTypes() {
    }
}

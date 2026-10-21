package xiaojin.gachaaddiction.reward.provenance;

import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.GachaAddiction;

public final class GachaRewardOriginTypes {
    public static final ResourceLocation SOURCE = ResourceLocation.fromNamespaceAndPath(GachaAddiction.MODID, "source");
    public static final ResourceLocation VANILLA_LOOT_TABLE = ResourceLocation.withDefaultNamespace("loot_table");
    public static final ResourceLocation CUSTOM_TABLE = ResourceLocation.fromNamespaceAndPath(GachaAddiction.MODID,
            "custom_table");
    public static final ResourceLocation CUSTOM_ENTRY = ResourceLocation.fromNamespaceAndPath(GachaAddiction.MODID,
            "custom_entry");
    public static final ResourceLocation RULE_ACTION = ResourceLocation.fromNamespaceAndPath(GachaAddiction.MODID,
            "rule_action");

    private GachaRewardOriginTypes() {
    }
}

package xiaojin.gachaaddiction.cardpool.transaction;

import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.GachaAddiction;

public final class GachaBuiltinRewardDeliveryTypes {
    public static final ResourceLocation INBOX = ResourceLocation.fromNamespaceAndPath(GachaAddiction.MODID,
            "inbox");
    public static final ResourceLocation RETURN = ResourceLocation.fromNamespaceAndPath(GachaAddiction.MODID,
            "return");
    public static final ResourceLocation INVENTORY = ResourceLocation.fromNamespaceAndPath(GachaAddiction.MODID,
            "inventory");
    public static final ResourceLocation DROP = ResourceLocation.fromNamespaceAndPath(GachaAddiction.MODID,
            "drop");

    private GachaBuiltinRewardDeliveryTypes() {
    }
}

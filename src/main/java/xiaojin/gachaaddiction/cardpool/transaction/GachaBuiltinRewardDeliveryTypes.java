package xiaojin.gachaaddiction.cardpool.transaction;

import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.GachaConstants;

public final class GachaBuiltinRewardDeliveryTypes {
    public static final ResourceLocation INBOX = ResourceLocation.fromNamespaceAndPath(GachaConstants.MODID,
            "inbox");
    public static final ResourceLocation RETURN = ResourceLocation.fromNamespaceAndPath(GachaConstants.MODID,
            "return");
    public static final ResourceLocation INVENTORY = ResourceLocation.fromNamespaceAndPath(GachaConstants.MODID,
            "inventory");
    public static final ResourceLocation DROP = ResourceLocation.fromNamespaceAndPath(GachaConstants.MODID,
            "drop");

    private GachaBuiltinRewardDeliveryTypes() {
    }
}

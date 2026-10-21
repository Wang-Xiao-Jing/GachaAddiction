package xiaojin.gachaaddiction.cardpool;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.cardpool.transaction.GachaBuiltinRewardDeliveryTypes;
import xiaojin.gachaaddiction.cardpool.transaction.GachaTransactionSource;

import java.util.Optional;

public final class GachaCardPoolTransactionSource {
    public static final String PACKAGE_KEY = "package";
    public static final String DELIVERY_HANDLER_KEY = "delivery_handler";

    private GachaCardPoolTransactionSource() {
    }

    public static GachaTransactionSource create(ResourceLocation cardPoolId, ResourceLocation packageId,
                                                String snapshotHash, ResourceLocation deliveryHandler) {
        CompoundTag data = new CompoundTag();
        data.putString(PACKAGE_KEY, packageId.toString());
        data.putString(DELIVERY_HANDLER_KEY, deliveryHandler.toString());
        return new GachaTransactionSource(cardPoolId, Optional.of(snapshotHash), data);
    }

    public static ResourceLocation deliveryHandler(GachaCardPoolDefinition cardPool) {
        return switch (cardPool.deliveryMode()) {
            case INBOX -> GachaBuiltinRewardDeliveryTypes.INBOX;
            case RETURN -> GachaBuiltinRewardDeliveryTypes.RETURN;
            case INVENTORY -> GachaBuiltinRewardDeliveryTypes.INVENTORY;
            case DROP -> GachaBuiltinRewardDeliveryTypes.DROP;
            case EXTERNAL -> cardPool.deliveryHandler().orElseThrow(() ->
                    new IllegalArgumentException("External reward delivery handler is missing"));
        };
    }

    public static ResourceLocation packageId(GachaTransactionSource source) {
        return ResourceLocation.parse(source.data().getString(PACKAGE_KEY));
    }

    public static ResourceLocation deliveryHandler(GachaTransactionSource source) {
        CompoundTag data = source.data();
        return ResourceLocation.parse(data.getString(DELIVERY_HANDLER_KEY));
    }

}

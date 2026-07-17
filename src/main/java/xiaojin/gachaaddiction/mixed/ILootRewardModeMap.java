package xiaojin.gachaaddiction.mixed;

import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.api.GachaType;

public interface ILootRewardModeMap {
    String gachaaddiction$getGachaTypeIdString();

    ResourceLocation gachaaddiction$getGachaTypeId();

    GachaType gachaaddiction$getGachaType();

    void gachaaddiction$setGachaType(GachaType gachaaddiction$gachaType);
}

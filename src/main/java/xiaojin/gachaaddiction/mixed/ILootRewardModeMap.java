package xiaojin.gachaaddiction.mixed;

import net.minecraft.resources.Identifier;
import xiaojin.gachaaddiction.api.GachaType;

public interface ILootRewardModeMap {
    String gachaaddiction$getGachaTypeIdString();

    Identifier gachaaddiction$getGachaTypeId();

    GachaType gachaaddiction$getGachaType();

    void gachaaddiction$setGachaType(GachaType gachaaddiction$gachaType);
}

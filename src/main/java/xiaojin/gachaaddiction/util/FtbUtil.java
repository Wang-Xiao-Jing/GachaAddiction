package xiaojin.gachaaddiction.util;

import dev.ftb.mods.ftblibrary.util.NameMap;
import net.minecraft.resources.Identifier;
import xiaojin.gachaaddiction.api.GachaType;
import xiaojin.gachaaddiction.registry.GachaTypes;

public class FtbUtil {
    public static final NameMap<GachaType> NAME_MAP = NameMap.of(GachaTypes.EMPTY, GachaTypes.getLiat())
            .id(v -> v.getId().toString())
            .nameKey(GachaType::getTranslationKey)
            .create();

    public static GachaType byId(String loot) {
        GachaType gachaType = GachaTypes.get(Identifier.parse(loot));
        if (gachaType == null) {
            return GachaTypes.SLOT_MACHINE;
        }
        return gachaType;
    }
}

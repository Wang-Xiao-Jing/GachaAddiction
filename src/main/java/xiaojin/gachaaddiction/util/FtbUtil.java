package xiaojin.gachaaddiction.util;

import dev.ftb.mods.ftblibrary.config.NameMap;
import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.api.GachaType;
import xiaojin.gachaaddiction.init.GachaTypes;

public class FtbUtil {
    public static final NameMap<GachaType> NAME_MAP = NameMap.of(GachaTypes.EMPTY, GachaTypes.getLiat())
            .id(v -> v.getId().toString())
            .nameKey(v -> "gacha_type." + v.getId().toString())
            .create();

    public static GachaType byId(String loot) {
        GachaType gachaType = GachaTypes.get(ResourceLocation.parse(loot));
        if (gachaType == null) {
            return GachaTypes.EMPTY;
        }
        return gachaType;
    }
}

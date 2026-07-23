package xiaojin.gachaaddiction.init;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;
import xiaojin.gachaaddiction.api.GachaType;
import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.GachaAddiction;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class GachaTypes {
    private static final Map<ResourceLocation, GachaType> GACHA_TYPE_MPA = new HashMap<>();
    private static final List<GachaType> GACHA_TYPE_LIST = new ObjectArrayList<>();
    public static final GachaType EMPTY = register("empty", GachaType::new);
    public static final GachaType SLOT_MACHINE = register("slot_machine", GachaType::new);

    public static <T extends GachaType> T register(ResourceLocation id, Function<ResourceLocation, T> function) {
        T type = function.apply(id);
        GACHA_TYPE_LIST.add(type);
        GACHA_TYPE_MPA.put(id, type);
        return type;
    }

    private static GachaType register(String id, Function<ResourceLocation, GachaType> function) {
        return register(ResourceLocation.fromNamespaceAndPath(GachaAddiction.MODID, id), function);
    }

    public static Map<ResourceLocation, GachaType> getMap() {
        return Collections.unmodifiableMap(GACHA_TYPE_MPA);
    }

    public static List<GachaType> getLiat() {
        return Collections.unmodifiableList(GACHA_TYPE_LIST);
    }

    @Nullable
    public static GachaType get(ResourceLocation id) {
        return GACHA_TYPE_MPA.get(id);
    }

    public static GachaType getDefault(ResourceLocation id) {
        if (!GACHA_TYPE_MPA.containsKey(id)){
            return SLOT_MACHINE;
        }
        return get(id);
    }

    public static void init() {

    }
}

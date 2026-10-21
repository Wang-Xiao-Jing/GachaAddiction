package xiaojin.gachaaddiction.registry;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import xiaojin.gachaaddiction.GachaAddiction;
import xiaojin.gachaaddiction.api.GachaType;
import xiaojin.gachaaddiction.client.compat.ldlib2.LDLib2GachaUiConstants;

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
    public static final GachaType INTERACTIVE_SLOT_MACHINE = register("interactive_slot_machine", GachaType::new);
    public static final GachaType MODERN = register("modern", GachaType::new);
    public static final GachaType MODERN_2 = register("modern_2", GachaType::new);
    public static final GachaType CHEST = register("chest", GachaType::new);
    public static final GachaType CSGO = register("csgo", GachaType::new);
    public static final GachaType WHEEL = register("wheel", GachaType::new);
    public static final GachaType TOTEM = register("totem", GachaType::new);
    public static final GachaType LDLIB2 = register(LDLib2GachaUiConstants.GACHA_TYPE_PATH, GachaType::new);

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
        if (!GACHA_TYPE_MPA.containsKey(id)) {
            return MODERN;
        }
        return get(id);
    }

    public static void init() {

    }
}

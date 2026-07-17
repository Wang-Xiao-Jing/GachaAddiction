package xiaojin.gachaaddiction.init.client;

import xiaojin.gachaaddiction.api.GachaType;
import xiaojin.gachaaddiction.api.client.GachaTypeClient;
import xiaojin.gachaaddiction.client.gui.screen.SlotMachineScreen;
import xiaojin.gachaaddiction.init.GachaTypes;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class GachaTypeClients {
    private static final Map<GachaType, GachaTypeClient<?, ?>> GACHA_TYPE_MPA = new HashMap<>();

    public static final GachaTypeClient<GachaType, ?> SLOT_MACHINE = register(GachaTypes.SLOT_MACHINE,
            gachaType -> new GachaTypeClient<>(gachaType, SlotMachineScreen::new));

    public static <T extends GachaType, C extends GachaTypeClient<T, ?>> C register(T gachaType, Function<T, C> function) {
        C type = function.apply(gachaType);
        GACHA_TYPE_MPA.put(gachaType, type);
        return type;
    }

    public static Map<GachaType, GachaTypeClient<?, ?>> getAll() {
        return Collections.unmodifiableMap(GACHA_TYPE_MPA);
    }

    public static <T extends GachaType> GachaTypeClient<T, ?> get(T gachaType) {
        //noinspection unchecked
        return (GachaTypeClient<T, ?>) GACHA_TYPE_MPA.get(gachaType);
    }

    public static void init() {

    }
}

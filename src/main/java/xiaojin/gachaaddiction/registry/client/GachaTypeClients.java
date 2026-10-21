package xiaojin.gachaaddiction.registry.client;

import xiaojin.gachaaddiction.api.GachaType;
import xiaojin.gachaaddiction.client.api.GachaTypeClient;
import xiaojin.gachaaddiction.client.presentation.GachaPresentationClient;
import xiaojin.gachaaddiction.client.gui.screen.GachaAnimationScreen;
import xiaojin.gachaaddiction.client.gui.screen.ModernGachaScreen;
import xiaojin.gachaaddiction.client.gui.screen.Modern2GachaScreen;
import xiaojin.gachaaddiction.client.gui.screen.SlotMachineScreen;
import xiaojin.gachaaddiction.registry.GachaTypes;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class GachaTypeClients {
    private static final Map<GachaType, GachaTypeClient<?, ?>> GACHA_TYPE_MPA = new HashMap<>();

    public static final GachaTypeClient<GachaType, ?> SLOT_MACHINE = register(GachaTypes.SLOT_MACHINE, gachaType ->
            new GachaTypeClient<>(gachaType, SlotMachineScreen::new));
    public static final GachaTypeClient<GachaType, ?> INTERACTIVE_SLOT_MACHINE = register(GachaTypes.INTERACTIVE_SLOT_MACHINE, gachaType ->
            new GachaTypeClient<>(gachaType, (parent, tables, entries) ->
                    new GachaAnimationScreen(parent, tables, entries, gachaType)));
    public static final GachaTypeClient<GachaType, ?> CHEST = register(GachaTypes.CHEST, gachaType ->
            new GachaTypeClient<>(gachaType, (parent, tables, entries) ->
                    GachaPresentationClient.createSource(parent, tables, entries, gachaType)));
    public static final GachaTypeClient<GachaType, ?> CSGO = register(GachaTypes.CSGO, gachaType ->
            new GachaTypeClient<>(gachaType, (parent, tables, entries) ->
                    new GachaAnimationScreen(parent, tables, entries, gachaType)));
    public static final GachaTypeClient<GachaType, ?> WHEEL = register(GachaTypes.WHEEL, gachaType ->
            new GachaTypeClient<>(gachaType, (parent, tables, entries) ->
                    new GachaAnimationScreen(parent, tables, entries, gachaType)));
    public static final GachaTypeClient<GachaType, ?> TOTEM = register(GachaTypes.TOTEM, gachaType ->
            new GachaTypeClient<>(gachaType, (parent, tables, entries) ->
                    GachaPresentationClient.createSource(parent, tables, entries, gachaType)));
    public static final GachaTypeClient<GachaType, ?> MODERN = register(GachaTypes.MODERN, gachaType ->
            new GachaTypeClient<>(gachaType, ModernGachaScreen::new));
    public static final GachaTypeClient<GachaType, ?> MODERN_2 = register(GachaTypes.MODERN_2, gachaType ->
            new GachaTypeClient<>(gachaType, Modern2GachaScreen::new));
    public static final GachaTypeClient<GachaType, ?> LDLIB2 = register(GachaTypes.LDLIB2, gachaType ->
            new GachaTypeClient<>(gachaType, GachaPresentationClient::createTemplate));

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

package xiaojin.gachaaddiction.registry.item;

import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import xiaojin.gachaaddiction.GachaConstants;
import xiaojin.gachaaddiction.world.item.GachaDrawItem;
import xiaojin.gachaaddiction.registry.GachaTypes;

public class GachaDrawItems {
    public static final DeferredRegister.Items REGISTRY = DeferredRegister.createItems(GachaConstants.MODID);

    public static final DeferredItem<GachaDrawItem> SEALED_CHEST = REGISTRY.registerItem("sealed_chest",
            properties -> new GachaDrawItem(properties, GachaTypes.CHEST));
    public static final DeferredItem<GachaDrawItem> GACHA_KEY = REGISTRY.registerItem("gacha_key",
            properties -> new GachaDrawItem(properties, GachaTypes.CSGO));
    public static final DeferredItem<GachaDrawItem> WHEEL_TICKET = REGISTRY.registerItem("wheel_ticket",
            properties -> new GachaDrawItem(properties, GachaTypes.WHEEL));
    public static final DeferredItem<GachaDrawItem> SLOT_TOKEN = REGISTRY.registerItem("slot_token",
            properties -> new GachaDrawItem(properties, GachaTypes.INTERACTIVE_SLOT_MACHINE));
    public static final DeferredItem<GachaDrawItem> WISH_TOTEM = REGISTRY.registerItem("wish_totem",
            properties -> new GachaDrawItem(properties, GachaTypes.TOTEM));
}

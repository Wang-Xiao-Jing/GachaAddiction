package xiaojin.gachaaddiction.events;

import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import xiaojin.gachaaddiction.GachaConstants;
import xiaojin.gachaaddiction.registry.Payloads;
import xiaojin.gachaaddiction.registry.item.GachaDrawItems;

@EventBusSubscriber(modid = GachaConstants.MODID)
public class GachaModEvents {
    @SubscribeEvent
    public static void creativeContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(CreativeModeTabs.TOOLS_AND_UTILITIES)) {
            event.accept(GachaDrawItems.SEALED_CHEST);
            event.accept(GachaDrawItems.GACHA_KEY);
            event.accept(GachaDrawItems.WHEEL_TICKET);
            event.accept(GachaDrawItems.SLOT_TOKEN);
            event.accept(GachaDrawItems.WISH_TOTEM);
        }
    }

    @SubscribeEvent
    public static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        Payloads.registrar(event);
    }
}

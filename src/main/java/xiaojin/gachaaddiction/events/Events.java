package xiaojin.gachaaddiction.events;

import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import xiaojin.gachaaddiction.GachaAddiction;
import xiaojin.gachaaddiction.util.LootDisplayCache;

@EventBusSubscriber(modid = GachaAddiction.MODID)
public class Events {
    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        ServerLevel level = event.getServer().overworld();
        LootDisplayCache.reload(level);
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        ServerLevel level = event.getPlayerList().getServer().overworld();
        LootDisplayCache.reload(level);
    }

    @SubscribeEvent
    public static void onPlayerContainerOpen(PlayerContainerEvent.Open event) {
    }
}

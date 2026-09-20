package xiaojin.gachaaddiction.event;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.level.ServerLevel;
import xiaojin.gachaaddiction.command.GachaCommand;
import xiaojin.gachaaddiction.util.LootDisplayCache;

public class Events {
    private Events() {
    }

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            ServerLevel level = server.overworld();
            LootDisplayCache.reload(level);
        });

        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
            if (!success) {
                return;
            }
            ServerLevel level = server.overworld();
            LootDisplayCache.reload(level);
        });

        CommandRegistrationCallback.EVENT.register(
                (dispatcher, buildContext, selection) -> dispatcher.register(GachaCommand.register()));
    }
}

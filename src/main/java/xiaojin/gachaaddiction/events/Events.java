package xiaojin.gachaaddiction.events;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.level.ServerLevel;
import xiaojin.gachaaddiction.command.GachaCommand;
import xiaojin.gachaaddiction.util.LootDisplayCache;

public final class Events {
    private Events() {
    }

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            ServerLevel level = server.overworld();
            LootDisplayCache.reload(level);
        });

        // 对应 NeoForge 的 OnDatapackSyncEvent：数据包重载后刷新缓存
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
            if (success) {
                LootDisplayCache.reload(server.overworld());
            }
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(GachaCommand.register()));
    }
}

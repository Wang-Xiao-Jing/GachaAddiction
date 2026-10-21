package xiaojin.gachaaddiction.cardpool;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import xiaojin.gachaaddiction.GachaConstants;
import xiaojin.gachaaddiction.config.GachaAddictionConfig;
import xiaojin.gachaaddiction.data.GachaDefinitionRepository;
import xiaojin.gachaaddiction.registry.runtime.GachaRuntimeRegistries;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public final class GachaServerRuntimeManager {
    public static final String STORAGE_DIRECTORY_NAME = GachaConstants.MODID;

    private static GachaServerRuntime current;

    private GachaServerRuntimeManager() {
    }

    public static synchronized CompletionStage<Void> start(MinecraftServer server) {
        if (current != null) {
            throw new IllegalStateException("Gacha server runtime is already open");
        }
        GachaRuntimeRegistries.CONSUMPTION_HANDLERS.freeze();
        GachaRuntimeRegistries.REWARD_DELIVERY_HANDLERS.freeze();
        Path storageDirectory = server.getWorldPath(LevelResource.ROOT).resolve(STORAGE_DIRECTORY_NAME);
        try {
            current = GachaServerRuntime.open(storageDirectory, server.registryAccess(), server,
                    GachaRuntimeRegistries.PARAMETER_TYPES, GachaRuntimeRegistries.CONSUMPTION_HANDLERS,
                    GachaRuntimeRegistries.REWARD_DELIVERY_HANDLERS,
                    GachaDefinitionRepository.INSTANCE, server, GachaAddictionConfig.SERVER.runtimeSettings());
        } catch (IOException failure) {
            throw new UncheckedIOException("Unable to open gacha server runtime", failure);
        }
        return current.recover();
    }

    public static synchronized Optional<GachaServerRuntime> current() {
        return Optional.ofNullable(current);
    }

    public static synchronized void stop() {
        if (current != null) {
            try {
                current.close();
            } finally {
                current = null;
            }
        }
    }
}

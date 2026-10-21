package xiaojin.gachaaddiction.persistence;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.EntityStorage;
import net.minecraft.world.level.chunk.storage.SimpleRegionStorage;
import xiaojin.gachaaddiction.cardpool.delivery.GachaRewardDropSnapshot;
import xiaojin.gachaaddiction.mixin.accessor.EntityStorageAccessor;
import xiaojin.gachaaddiction.mixin.accessor.PersistentEntitySectionManagerAccessor;
import xiaojin.gachaaddiction.mixin.accessor.ServerLevelAccessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletionException;

public class GachaEntitySaveCheckpoint {
    private static final String POSITION_KEY = "Position";
    private static final String ENTITIES_KEY = "Entities";
    private static final String UUID_KEY = "UUID";
    private static final String ITEM_KEY = "Item";
    private static final String ENTITY_POSITION_KEY = "Pos";

    public static void verifyAfterSave(MinecraftServer server, List<GachaRewardDropSnapshot> drops) throws IOException {
        Objects.requireNonNull(server, "server");
        Objects.requireNonNull(drops, "drops");
        if (!server.isSameThread()) {
            throw new IllegalStateException("Reward entity checkpoint requires the server thread");
        }

        Map<ResourceLocation, List<GachaRewardDropSnapshot>> dimensions = new LinkedHashMap<>();
        for (GachaRewardDropSnapshot drop : drops) {
            dimensions.computeIfAbsent(drop.dimension(), ignored -> new ArrayList<>()).add(drop);
        }

        for (var dimension : dimensions.entrySet()) {
            ServerLevel level = server.getLevel(ResourceKey.create(Registries.DIMENSION, dimension.getKey()));
            if (level == null) {
                throw new IOException("Reward drop dimension is not available: " + dimension.getKey());
            }
            verifyLevel(level, dimension.getValue());
        }
    }

    private static void verifyLevel(ServerLevel level, List<GachaRewardDropSnapshot> drops) throws IOException {
        SimpleRegionStorage storage = entityStorage(level);
        Map<ChunkPos, List<GachaRewardDropSnapshot>> chunks = new LinkedHashMap<>();
        for (GachaRewardDropSnapshot drop : drops) {
            chunks.computeIfAbsent(chunkPosition(drop), ignored -> new ArrayList<>()).add(drop);
        }

        try {
            // 读取既有 IO worker 的原始 NBT，不走实体反序列化队列。
            storage.synchronize(true).join();
            for (var chunk : chunks.entrySet()) {
                CompoundTag saved = storage.read(chunk.getKey()).join().orElseThrow(
                        () -> new IOException("Saved reward entity chunk does not exist"));
                for (GachaRewardDropSnapshot drop : chunk.getValue()) {
                    verifySavedChunk(saved, drop);
                }
            }
        } catch (CompletionException failure) {
            throw new IOException("Unable to read saved reward entity chunk", failure.getCause());
        }
    }

    public static boolean isSaved(MinecraftServer server, GachaRewardDropSnapshot expected) throws IOException {
        Objects.requireNonNull(server, "server");
        Objects.requireNonNull(expected, "expected");
        if (!server.isSameThread()) {
            throw new IllegalStateException("Reward entity lookup requires the server thread");
        }

        ServerLevel level = server.getLevel(ResourceKey.create(Registries.DIMENSION, expected.dimension()));
        if (level == null) {
            throw new IOException("Reward drop dimension is not available: " + expected.dimension());
        }

        SimpleRegionStorage storage = entityStorage(level);
        try {
            storage.synchronize(true).join();
            var saved = storage.read(chunkPosition(expected)).join();
            return saved.isPresent() && containsSavedEntity(saved.get(), expected);
        } catch (CompletionException failure) {
            throw new IOException("Unable to find saved reward entity", failure.getCause());
        }
    }

    public static boolean containsSavedEntity(CompoundTag savedChunk, GachaRewardDropSnapshot expected)
            throws IOException {
        Objects.requireNonNull(savedChunk, "savedChunk");
        Objects.requireNonNull(expected, "expected");
        int[] position = savedChunk.getIntArray(POSITION_KEY);
        ChunkPos chunk = chunkPosition(expected);
        if (position.length != 2 || position[0] != chunk.x || position[1] != chunk.z) {
            throw new IOException("Saved reward entity chunk position does not match");
        }

        ListTag entities = savedChunk.getList(ENTITIES_KEY, Tag.TAG_COMPOUND);
        for (int index = 0; index < entities.size(); index++) {
            CompoundTag entity = entities.getCompound(index);
            if (entity.hasUUID(UUID_KEY) && entity.getUUID(UUID_KEY).equals(expected.entityId())) {
                verifySavedChunk(savedChunk, expected);
                return true;
            }
        }
        return false;
    }

    public static void verifySavedChunk(CompoundTag savedChunk, GachaRewardDropSnapshot expected) throws IOException {
        Objects.requireNonNull(savedChunk, "savedChunk");
        Objects.requireNonNull(expected, "expected");
        ChunkPos chunk = chunkPosition(expected);
        int[] position = savedChunk.getIntArray(POSITION_KEY);
        if (position.length != 2 || position[0] != chunk.x || position[1] != chunk.z) {
            throw new IOException("Saved reward entity chunk position does not match");
        }

        ListTag entities = savedChunk.getList(ENTITIES_KEY, Tag.TAG_COMPOUND);
        CompoundTag found = null;
        for (int index = 0; index < entities.size(); index++) {
            CompoundTag entity = entities.getCompound(index);
            if (entity.hasUUID(UUID_KEY) && entity.getUUID(UUID_KEY).equals(expected.entityId())) {
                if (found != null) {
                    throw new IOException("Saved reward entity identity is duplicated");
                }
                found = entity;
            }
        }
        if (found == null) {
            throw new IOException("Saved reward entity does not exist");
        }

        try {
            GachaRewardDropSnapshot actual = new GachaRewardDropSnapshot(expected.dimension(), found);
            CompoundTag expectedData = expected.entityData();
            if (!actual.receipt().equals(expected.receipt())
                    || !found.getCompound(ITEM_KEY).equals(expectedData.getCompound(ITEM_KEY))
                    || !Objects.equals(found.get(ENTITY_POSITION_KEY), expectedData.get(ENTITY_POSITION_KEY))) {
                throw new IOException("Saved reward entity does not match its delivery plan");
            }
        } catch (IllegalArgumentException failure) {
            throw new IOException("Saved reward entity snapshot is invalid", failure);
        }
    }

    private static ChunkPos chunkPosition(GachaRewardDropSnapshot drop) {
        ListTag position = drop.entityData().getList(ENTITY_POSITION_KEY, Tag.TAG_DOUBLE);
        return new ChunkPos(BlockPos.containing(position.getDouble(0), position.getDouble(1), position.getDouble(2)));
    }

    private static SimpleRegionStorage entityStorage(ServerLevel level) throws IOException {
        if (!(level instanceof ServerLevelAccessor levelAccessor)
                || !(levelAccessor.gachaaddiction$getEntityManager()
                instanceof PersistentEntitySectionManagerAccessor managerAccessor)
                || !(managerAccessor.gachaaddiction$getPermanentStorage() instanceof EntityStorage storage)
                || !(storage instanceof EntityStorageAccessor storageAccessor)) {
            throw new IOException("Reward entity storage accessors are unavailable");
        }

        return storageAccessor.gachaaddiction$getSimpleRegionStorage();
    }
}

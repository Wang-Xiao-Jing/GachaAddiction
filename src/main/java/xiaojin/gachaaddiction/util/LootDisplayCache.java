package xiaojin.gachaaddiction.util;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xiaojin.gachaaddiction.api.ItemStackEntry;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LootDisplayCache {
    private static final Logger log = LoggerFactory.getLogger("GachaDisplayCache");
    private static final Map<ResourceKey<LootTable>, List<ItemStackEntry>> cache = new HashMap<>();

    public static void reload(ServerLevel level) {
        cache.clear();
        Registry<LootTable> registry = level.getServer()
                .reloadableRegistries().get()
                .registryOrThrow(Registries.LOOT_TABLE);
        int count = 0;
        for (var holder : registry.holders().toList()) {
            LootTable table = holder.value();
            if (table == LootTable.EMPTY) continue;
            List<ItemStackEntry> entries = ItemStackEntry.extract(table);
            if (!entries.isEmpty()) {
                cache.put(holder.key(), entries);
                count++;
            }
        }
        log.info("缓存的战利品表数量: {}", count);
    }

    public static List<ItemStackEntry> get(ResourceKey<LootTable> key) {
        return cache.getOrDefault(key, Collections.emptyList());
    }

    public static List<ItemStackEntry> get(List<@Nullable ResourceKey<LootTable>> key) {
        return key.stream().map(LootDisplayCache::get).flatMap(List::stream).toList();
    }
}

package xiaojin.gachaaddiction.util;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.loot.LootTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LootDisplayCache {
    private static final Logger log = LoggerFactory.getLogger("GachaDisplayCache");
    private static final Map<ResourceKey<LootTable>, List<DisplayEntry>> cache = new HashMap<>();

    public static void reload(ServerLevel level) {
        cache.clear();
        Registry<LootTable> registry = level.getServer()
                .reloadableRegistries().get()
                .registryOrThrow(Registries.LOOT_TABLE);
        int count = 0;
        for (var holder : registry.holders().toList()) {
            LootTable table = holder.value();
            if (table == LootTable.EMPTY) continue;
            List<DisplayEntry> entries = DisplayEntry.extract(table);
            if (!entries.isEmpty()) {
                cache.put(holder.key(), entries);
                count++;
            }
        }
        log.info("缓存的战利品表数量: {}", count);
    }

    public static List<DisplayEntry> get(ResourceKey<LootTable> key) {
        return cache.getOrDefault(key, Collections.emptyList());
    }
}

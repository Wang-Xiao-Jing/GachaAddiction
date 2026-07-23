package xiaojin.gachaaddiction.util;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ModUtil {
    public static final StreamCodec<ByteBuf, ResourceKey<LootTable>> LOOT_TABLE_KEY_STREAM_CODEC =
            ResourceKey.streamCodec(Registries.LOOT_TABLE);
    public static final StreamCodec<ByteBuf, List<@Nullable ResourceKey<LootTable>>> LOOT_TABLE_KEY_LIST_STREAM_CODEC =
            LOOT_TABLE_KEY_STREAM_CODEC.apply(ByteBufCodecs.list());
    public static final StreamCodec<ByteBuf, List<ResourceLocation>> RESOURCE_LOCATION_STREAM_CODEC =
            ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list());

    public static @Nullable ResourceLocation of(@Nullable ResourceKey<LootTable> lootTableResourceKey) {
        if (lootTableResourceKey == null) {
            return null;
        }
        return lootTableResourceKey.location();
    }

    public static @NotNull List<ResourceLocation> of(List<@Nullable ResourceKey<LootTable>> lootTableResourceKey) {
        return lootTableResourceKey.stream().map(ModUtil::of).toList();
    }
}

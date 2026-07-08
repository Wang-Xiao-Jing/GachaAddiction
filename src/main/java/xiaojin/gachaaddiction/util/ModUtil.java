package xiaojin.gachaaddiction.util;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.List;

public class ModUtil {
    public static final StreamCodec<ByteBuf, ResourceKey<LootTable>> LOOT_TABLE_KEY_STREAM_CODEC =
            ResourceKey.streamCodec(Registries.LOOT_TABLE);
    public static final StreamCodec<ByteBuf, List<ResourceKey<LootTable>>> LOOT_TABLE_KEY_STREAM_LIST_CODEC =
            LOOT_TABLE_KEY_STREAM_CODEC.apply(ByteBufCodecs.list());
}

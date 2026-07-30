package xiaojin.gachaaddiction.util;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;
import java.util.Objects;

public class ModUtil {
    public static final String OPEN_GACHAA_UI = "GachaAddiction:openGachaaUi";

    public static final StreamCodec<ByteBuf, ResourceKey<LootTable>> LOOT_TABLE_KEY_STREAM_CODEC =
            ResourceKey.streamCodec(Registries.LOOT_TABLE);
    public static final StreamCodec<ByteBuf, List<@Nullable ResourceKey<LootTable>>> LOOT_TABLE_KEY_LIST_STREAM_CODEC =
            LOOT_TABLE_KEY_STREAM_CODEC.apply(ByteBufCodecs.list());
    public static final StreamCodec<ByteBuf, List<Identifier>> RESOURCE_LOCATION_STREAM_CODEC =
            Identifier.STREAM_CODEC.apply(ByteBufCodecs.list());

    public static @Nullable Identifier of(@Nullable ResourceKey<LootTable> lootTableResourceKey) {
        if (lootTableResourceKey == null) {
            return null;
        }
        return lootTableResourceKey.identifier();
    }

    public static @NotNull List<Identifier> of(List<@Nullable ResourceKey<LootTable>> lootTableResourceKey) {
        return lootTableResourceKey.stream().map(ModUtil::of).filter(Objects::nonNull).toList();
    }
}

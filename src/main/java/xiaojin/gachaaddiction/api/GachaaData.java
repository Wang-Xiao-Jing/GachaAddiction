package xiaojin.gachaaddiction.api;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import xiaojin.gachaaddiction.GachaAddictionConfig;
import xiaojin.gachaaddiction.init.GachaTypes;
import xiaojin.gachaaddiction.util.ModUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record GachaaData(
        boolean isOpenGachaaUi,
        Optional<GachaType> gachaType,
        Optional<List<Identifier>> lootTableId,
        Optional<List<ItemStackEntry>> itemStackEntry
) {
    public static final StreamCodec<RegistryFriendlyByteBuf, GachaaData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, GachaaData::isOpenGachaaUi,
            ByteBufCodecs.optional(GachaType.STREAM_CODEC), GachaaData::gachaType,
            ByteBufCodecs.optional(ModUtil.RESOURCE_LOCATION_STREAM_CODEC), GachaaData::lootTableId,
            ByteBufCodecs.optional(ItemStackEntry.LIST_STREAM_CODEC), GachaaData::itemStackEntry,
            GachaaData::new);

    public static final StreamCodec<RegistryFriendlyByteBuf, Optional<GachaaData>> OPTIONAL_STREAM_CODEC = ByteBufCodecs.optional(STREAM_CODEC);

    public GachaType getGachaType(){
        if (gachaType.isPresent()) {
            return gachaType.orElse(GachaTypes.EMPTY);
        }

        return GachaAddictionConfig.CLIENT.getDefaultGachaaType();
    }
}

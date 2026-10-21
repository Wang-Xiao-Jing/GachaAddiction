package xiaojin.gachaaddiction.api;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.config.GachaAddictionConfig;
import xiaojin.gachaaddiction.registry.GachaTypes;
import xiaojin.gachaaddiction.util.ModUtil;

import java.util.List;
import java.util.Optional;

public record GachaData(
        boolean isOpenGachaaUi,
        Optional<GachaType> gachaType,
        Optional<List<ResourceLocation>> lootTableId,
        Optional<List<ItemStackEntry>> itemStackEntry
) {
    public static final StreamCodec<RegistryFriendlyByteBuf, GachaData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, GachaData::isOpenGachaaUi,
            ByteBufCodecs.optional(GachaType.STREAM_CODEC), GachaData::gachaType,
            ByteBufCodecs.optional(ModUtil.RESOURCE_LOCATION_STREAM_CODEC), GachaData::lootTableId,
            ByteBufCodecs.optional(ItemStackEntry.LIST_STREAM_CODEC), GachaData::itemStackEntry,
            GachaData::new);

    public static final StreamCodec<RegistryFriendlyByteBuf, Optional<GachaData>> OPTIONAL_STREAM_CODEC = ByteBufCodecs.optional(STREAM_CODEC);

    public GachaType getGachaType() {
        if (gachaType.isPresent()) {
            return gachaType.orElse(GachaTypes.EMPTY);
        }

        return GachaAddictionConfig.CLIENT.getDefaultGachaaType();
    }
}

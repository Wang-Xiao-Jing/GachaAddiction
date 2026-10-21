package xiaojin.gachaaddiction.network.cts;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import xiaojin.gachaaddiction.GachaConstants;
import xiaojin.gachaaddiction.api.GachaSecondaryType;
import xiaojin.gachaaddiction.api.GachaType;
import xiaojin.gachaaddiction.network.IToServerPayload;
import xiaojin.gachaaddiction.presentation.GachaPresentations;

public record GachaPresentationPreferencesPayload(
        boolean protectLoot,
        GachaType animation,
        GachaSecondaryType secondaryType,
        int choiceCount
) implements IToServerPayload {
    public static final Type<GachaPresentationPreferencesPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(GachaConstants.MODID, "presentation_preferences"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GachaPresentationPreferencesPayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.BOOL, GachaPresentationPreferencesPayload::protectLoot,
                    GachaType.STREAM_CODEC, GachaPresentationPreferencesPayload::animation,
                    GachaSecondaryType.STREAM_CODEC, GachaPresentationPreferencesPayload::secondaryType,
                    ByteBufCodecs.VAR_INT, GachaPresentationPreferencesPayload::choiceCount, GachaPresentationPreferencesPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void work(IPayloadContext context, ServerPlayer player) {
        GachaPresentations.preferences(player, this);
    }
}

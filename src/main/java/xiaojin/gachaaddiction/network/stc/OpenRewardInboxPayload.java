package xiaojin.gachaaddiction.network.stc;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.GachaConstants;
import xiaojin.gachaaddiction.cardpool.delivery.GachaRewardInboxEntry;
import xiaojin.gachaaddiction.network.GachaRewardInboxCodecs;
import xiaojin.gachaaddiction.network.IToClientPayload;

import java.util.List;

public record OpenRewardInboxPayload(List<GachaRewardInboxEntry> entries)
        implements IToClientPayload {
    public static final Type<OpenRewardInboxPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(GachaConstants.MODID, "open_reward_inbox"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenRewardInboxPayload> STREAM_CODEC =
            StreamCodec.composite(GachaRewardInboxCodecs.ENTRY.apply(ByteBufCodecs.list(
                            GachaRewardInboxCodecs.MAXIMUM_INBOX_ENTRIES)),
                    OpenRewardInboxPayload::entries, OpenRewardInboxPayload::new);

    public OpenRewardInboxPayload {
        entries = List.copyOf(entries);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}

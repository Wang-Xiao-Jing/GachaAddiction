package xiaojin.gachaaddiction.network.stc;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import xiaojin.gachaaddiction.GachaAddiction;
import xiaojin.gachaaddiction.cardpool.delivery.GachaRewardInboxEntry;
import xiaojin.gachaaddiction.client.gui.screen.RewardInboxScreen;
import xiaojin.gachaaddiction.network.GachaRewardInboxCodecs;
import xiaojin.gachaaddiction.network.IToClientPayload;

import java.util.List;

public record OpenRewardInboxPayload(List<GachaRewardInboxEntry> entries)
        implements IToClientPayload {
    public static final Type<OpenRewardInboxPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(GachaAddiction.MODID, "open_reward_inbox"));
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

    @Override
    public void work(IPayloadContext context, AbstractClientPlayer player) {
        Minecraft.getInstance().setScreen(new RewardInboxScreen(entries));
    }
}

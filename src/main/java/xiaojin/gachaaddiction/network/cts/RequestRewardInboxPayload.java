package xiaojin.gachaaddiction.network.cts;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import xiaojin.gachaaddiction.GachaConstants;
import xiaojin.gachaaddiction.cardpool.GachaServerRuntimeManager;
import xiaojin.gachaaddiction.network.IToServerPayload;
import xiaojin.gachaaddiction.network.stc.OpenRewardInboxPayload;
import xiaojin.gachaaddiction.util.TranslationKeys;

public record RequestRewardInboxPayload() implements IToServerPayload {
    public static final RequestRewardInboxPayload INSTANCE = new RequestRewardInboxPayload();
    public static final Type<RequestRewardInboxPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(GachaConstants.MODID, "request_reward_inbox"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestRewardInboxPayload> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void work(IPayloadContext context, ServerPlayer player) {
        var runtime = GachaServerRuntimeManager.current().orElse(null);
        if (runtime == null) {
            player.sendSystemMessage(Component.translatable(
                    TranslationKeys.COMMANDS_GACHA_RUNTIME_UNAVAILABLE));
            return;
        }
        PacketDistributor.sendToPlayer(player,
                new OpenRewardInboxPayload(runtime.rewardInbox().pending(player.getUUID())));
    }
}

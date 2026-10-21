package xiaojin.gachaaddiction.network.cts;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import xiaojin.gachaaddiction.GachaAddiction;
import xiaojin.gachaaddiction.cardpool.GachaServerRuntimeManager;
import xiaojin.gachaaddiction.cardpool.delivery.GachaRewardInboxClaim;
import xiaojin.gachaaddiction.cardpool.transaction.GachaTransactionId;
import xiaojin.gachaaddiction.network.IToServerPayload;
import xiaojin.gachaaddiction.network.stc.OpenRewardInboxPayload;
import xiaojin.gachaaddiction.util.TranslationKeys;

import java.util.UUID;

public record ClaimRewardInboxPayload(UUID transactionId) implements IToServerPayload {
    public static final Type<ClaimRewardInboxPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(GachaAddiction.MODID, "claim_reward_inbox"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClaimRewardInboxPayload> STREAM_CODEC =
            StreamCodec.composite(UUIDUtil.STREAM_CODEC, ClaimRewardInboxPayload::transactionId,
                    ClaimRewardInboxPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }


    @Override
    public void work(IPayloadContext context, ServerPlayer player) {
        var runtime = GachaServerRuntimeManager.current().orElse(null);
        if (runtime == null) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.translatable(
                    TranslationKeys.COMMANDS_GACHA_RUNTIME_UNAVAILABLE));
            return;
        }
        try {
            int stackCount = GachaRewardInboxClaim.claim(player, runtime.rewardInbox(),
                    new GachaTransactionId(transactionId));
            player.sendSystemMessage(net.minecraft.network.chat.Component.translatable(
                    TranslationKeys.COMMANDS_GACHA_INBOX_CLAIMED, stackCount, transactionId.toString()));
        } catch (RuntimeException failure) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.translatable(
                    TranslationKeys.COMMANDS_GACHA_INBOX_CLAIM_FAILED, failure.getMessage()));
        }
        PacketDistributor.sendToPlayer(player,
                new OpenRewardInboxPayload(runtime.rewardInbox().pending(player.getUUID())));
    }
}

package xiaojin.gachaaddiction.network.cts;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import xiaojin.gachaaddiction.GachaAddiction;
import xiaojin.gachaaddiction.cardpool.GachaCardPoolTransactionSource;
import xiaojin.gachaaddiction.cardpool.GachaPublicCardPoolEndpoint;
import xiaojin.gachaaddiction.cardpool.GachaServerRuntimeManager;
import xiaojin.gachaaddiction.cardpool.transaction.GachaCommittedTransaction;
import xiaojin.gachaaddiction.network.IToServerPayload;
import xiaojin.gachaaddiction.network.stc.OpenCardPoolResultPayload;
import xiaojin.gachaaddiction.util.GachaPresentations;
import xiaojin.gachaaddiction.util.TranslationKeys;

import java.util.UUID;

public record ConfirmCardPoolDrawPayload(UUID token) implements IToServerPayload {
    public static final Type<ConfirmCardPoolDrawPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(GachaAddiction.MODID, "confirm_card_pool_draw"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ConfirmCardPoolDrawPayload> STREAM_CODEC =
            StreamCodec.composite(UUIDUtil.STREAM_CODEC, ConfirmCardPoolDrawPayload::token,
                    ConfirmCardPoolDrawPayload::new);

    private static void openResult(ServerPlayer player,
                                   GachaCommittedTransaction committed,
                                   Throwable effectFailure) {
        if (effectFailure != null) {
            GachaAddiction.LOGGER.error("Card pool reward delivery failed for transaction {}",
                    committed.context().transactionId().value(), effectFailure);
            player.sendSystemMessage(Component.translatable(TranslationKeys.GUI_GACHA_DRAW_FAILED));
            return;
        }
        var runtime = GachaServerRuntimeManager.current().orElse(null);
        var snapshot = runtime == null ? null : runtime.resolveSnapshot(
                committed.context().source().snapshotHash().orElseThrow(() ->
                        new IllegalStateException("Card pool transaction source has no snapshot"))).orElse(null);
        if (snapshot == null) {
            GachaAddiction.LOGGER.error("Missing presentation snapshot for transaction {}",
                    committed.context().transactionId().value());
            player.sendSystemMessage(Component.translatable(TranslationKeys.GUI_GACHA_DRAW_FAILED));
            return;
        }
        var source = committed.context().source();
        GachaPresentations.beginCommittedRewards(player, snapshot.presentation().icon(), committed.result().rewardRounds().size());
        PacketDistributor.sendToPlayer(player, new OpenCardPoolResultPayload(
                committed.context().transactionId().value(), source.id(),
                GachaCardPoolTransactionSource.packageId(source), snapshot.presentation().resultStyle(),
                committed.result().rewards(), committed.result().rewardRounds()));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private void handleOnServer(IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }
        GachaPublicCardPoolEndpoint.confirm(player, token).whenComplete((committed, failure) -> player.getServer().execute(() -> {
            if (failure != null) {
                GachaAddiction.LOGGER.error("Public card pool draw confirmation failed for player {}",
                        player.getUUID(), failure);
                player.sendSystemMessage(Component.translatable(TranslationKeys.GUI_GACHA_DRAW_FAILED));
                return;
            }
            committed.effectsCompletion().whenComplete((ignored, effectFailure) ->
                    player.getServer().execute(() -> openResult(player, committed, effectFailure)));
        }));
    }

    @Override
    public void work(IPayloadContext context, ServerPlayer player) {
        handleOnServer(context);
    }
}

package xiaojin.gachaaddiction.registry;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.checkerframework.checker.nullness.qual.NonNull;
import xiaojin.gachaaddiction.network.IToPayload;
import xiaojin.gachaaddiction.network.cts.*;
import xiaojin.gachaaddiction.network.stc.*;

public class Payloads {
    private static final String NETWORK_PROTOCOL_VERSION = "4";

    public static void registrar(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(NETWORK_PROTOCOL_VERSION);
        server(registrar);
        client(registrar);
        clientAndServer(registrar);
    }

    private static void clientAndServer(PayloadRegistrar registrar) {
    }

    private static void client(PayloadRegistrar registrar) {
        registrar.optional().playToClient(GachaPresentationPayload.TYPE,
                GachaPresentationPayload.STREAM_CODEC,
                GachaPresentationPayload::handle);
        registrar.optional().playToClient(GachaItemPresentationPayload.TYPE,
                GachaItemPresentationPayload.STREAM_CODEC, GachaItemPresentationPayload::handle);
        registrar.optional().playToClient(GachaAnnouncementSessionPayload.TYPE,
                GachaAnnouncementSessionPayload.STREAM_CODEC, GachaAnnouncementSessionPayload::handle);
        registrar.optional().playToClient(GachaAnnouncementPayload.TYPE,
                GachaAnnouncementPayload.STREAM_CODEC, GachaAnnouncementPayload::handle);
        registrar.optional().playToClient(OpenLocalWorkbenchPayload.TYPE, OpenLocalWorkbenchPayload.STREAM_CODEC,
                OpenLocalWorkbenchPayload::handle);
        registrar.playToClient(OpenGachaScreenPayload.TYPE, OpenGachaScreenPayload.STREAM_CODEC,
                OpenGachaScreenPayload::handle);
        registrar.playToClient(OpenCardPoolResultPayload.TYPE, OpenCardPoolResultPayload.STREAM_CODEC,
                OpenCardPoolResultPayload::handle);
        registrar.playToClient(OpenCardPoolCatalogPayload.TYPE, OpenCardPoolCatalogPayload.STREAM_CODEC,
                OpenCardPoolCatalogPayload::handle);
        registrar.playToClient(OpenCardPoolDrawConfirmationPayload.TYPE,
                OpenCardPoolDrawConfirmationPayload.STREAM_CODEC, OpenCardPoolDrawConfirmationPayload::handle);
        registrar.playToClient(OpenCardPoolProbabilityPreviewPayload.TYPE,
                OpenCardPoolProbabilityPreviewPayload.STREAM_CODEC,
                OpenCardPoolProbabilityPreviewPayload::handle);
        registrar.playToClient(OpenRewardInboxPayload.TYPE, OpenRewardInboxPayload.STREAM_CODEC,
                OpenRewardInboxPayload::handle);
    }

    private static void server(PayloadRegistrar registrar) {
        registrar.optional().playToServer(GachaPresentationControlPayload.TYPE,
                GachaPresentationControlPayload.STREAM_CODEC,
                GachaPresentationControlPayload::handle);
        registrar.optional().playToServer(GachaPresentationPreferencesPayload.TYPE,
                GachaPresentationPreferencesPayload.STREAM_CODEC,
                GachaPresentationPreferencesPayload::handle);
        registrar.optional().playToServer(FinishGachaItemDrawPayload.TYPE,
                FinishGachaItemDrawPayload.STREAM_CODEC, FinishGachaItemDrawPayload::handle);
        registrar.optional().playToServer(RequestGachaAnnouncementPayload.TYPE,
                RequestGachaAnnouncementPayload.STREAM_CODEC, RequestGachaAnnouncementPayload::handle);
        registrar.optional().playToServer(GachaAutoTakePayload.TYPE, GachaAutoTakePayload.STREAM_CODEC,
                GachaAutoTakePayload::handle);
        registrar.playToServer(RequestCardPoolDrawPayload.TYPE, RequestCardPoolDrawPayload.STREAM_CODEC,
                RequestCardPoolDrawPayload::handle);
        registrar.playToServer(RequestCardPoolDrawWithRoundsPayload.TYPE,
                RequestCardPoolDrawWithRoundsPayload.STREAM_CODEC, RequestCardPoolDrawWithRoundsPayload::handle);
        registrar.playToServer(RequestCardPoolProbabilityPreviewPayload.TYPE,
                RequestCardPoolProbabilityPreviewPayload.STREAM_CODEC,
                RequestCardPoolProbabilityPreviewPayload::handle);
        registrar.playToServer(ConfirmCardPoolDrawPayload.TYPE, ConfirmCardPoolDrawPayload.STREAM_CODEC,
                ConfirmCardPoolDrawPayload::handle);
        registrar.playToServer(RequestCardPoolCatalogPayload.TYPE, RequestCardPoolCatalogPayload.STREAM_CODEC,
                RequestCardPoolCatalogPayload::handle);
        registrar.playToServer(RequestRewardInboxPayload.TYPE, RequestRewardInboxPayload.STREAM_CODEC,
                RequestRewardInboxPayload::handle);
        registrar.playToServer(ClaimRewardInboxPayload.TYPE, ClaimRewardInboxPayload.STREAM_CODEC,
                ClaimRewardInboxPayload::handle);
    }

    private static <T extends IToPayload> @NonNull PayloadRegistrar playToServer(
            PayloadRegistrar registrar, CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
        return registrar.playToServer(type, codec, IToPayload::handle);
    }

    private static <T extends IToPayload> @NonNull PayloadRegistrar playToClient(
            PayloadRegistrar registrar, CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
        return registrar.playToClient(type, codec, IToPayload::handle);
    }

    private static <T extends IToPayload> @NonNull PayloadRegistrar playBidirectional(
            PayloadRegistrar registrar, CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
        return registrar.playBidirectional(type, codec, IToPayload::handle);
    }
}

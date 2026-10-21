package xiaojin.gachaaddiction.client.api;

import net.neoforged.neoforge.network.PacketDistributor;
import xiaojin.gachaaddiction.network.cts.RequestCardPoolCatalogPayload;
import xiaojin.gachaaddiction.network.cts.RequestRewardInboxPayload;

public final class GachaCardPoolClientApi {
    private GachaCardPoolClientApi() {
    }

    public static void openCatalog() {
        PacketDistributor.sendToServer(RequestCardPoolCatalogPayload.INSTANCE);
    }

    public static void openRewardInbox() {
        PacketDistributor.sendToServer(RequestRewardInboxPayload.INSTANCE);
    }
}

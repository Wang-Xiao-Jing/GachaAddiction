package xiaojin.gachaaddiction.client.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import xiaojin.gachaaddiction.api.GachaAnnouncement;
import xiaojin.gachaaddiction.api.GachaType;
import xiaojin.gachaaddiction.client.workspace.AdminWorkbenchClient;
import xiaojin.gachaaddiction.client.announcement.GachaAnnouncementClient;
import xiaojin.gachaaddiction.client.presentation.GachaItemPresentationClient;
import xiaojin.gachaaddiction.client.presentation.GachaPresentationClient;
import xiaojin.gachaaddiction.client.gui.screen.CardPoolDrawConfirmationScreen;
import xiaojin.gachaaddiction.client.gui.screen.CardPoolSelectionScreen;
import xiaojin.gachaaddiction.client.gui.screen.RewardInboxScreen;
import xiaojin.gachaaddiction.config.GachaAddictionConfig;
import xiaojin.gachaaddiction.network.IToClientPayload;
import xiaojin.gachaaddiction.network.stc.*;
import xiaojin.gachaaddiction.registry.client.GachaTypeClients;

public class GachaClientPayloadHandler {
    public static void handle(IToClientPayload payload) {
        switch (payload) {
            case GachaAnnouncementPayload message -> GachaAnnouncementClient.INSTANCE.receive(message.announcement());
            case GachaAnnouncementSessionPayload message -> GachaAnnouncementClient.INSTANCE.begin(message.token(), message.menuId());
            case GachaItemPresentationPayload message -> GachaItemPresentationClient.receive(message);
            case GachaPresentationPayload message -> GachaPresentationClient.receive(message);
            case OpenCardPoolCatalogPayload message -> {
                GachaPresentationClient.rememberCardPools(message.cardPools());
                Minecraft.getInstance().setScreen(new CardPoolSelectionScreen(message.cardPools()));
            }
            case OpenCardPoolDrawConfirmationPayload message -> {
                GachaPresentationClient.rememberCardPool(message.cardPoolId(), message.titleTranslationKey());
                Minecraft.getInstance().setScreen(new CardPoolDrawConfirmationScreen(message));
            }
            case OpenCardPoolProbabilityPreviewPayload message -> {
                if (Minecraft.getInstance().screen instanceof CardPoolDrawConfirmationScreen screen) {
                    screen.setProbabilityPreview(message.token(), message.probabilities());
                }
            }
            case OpenCardPoolResultPayload message -> Minecraft.getInstance().setScreen(switch (message.resultStyle()) {
                case SUMMARY -> GachaPresentationClient.createCardPoolResult(message.transactionId(), message.cardPoolId(),
                        message.packageId(), message.rewards(), message.rewardRounds());
            });
            case OpenGachaScreenPayload message -> openGachaScreen(message);
            case OpenLocalWorkbenchPayload message -> AdminWorkbenchClient.open();
            case OpenRewardInboxPayload message -> Minecraft.getInstance().setScreen(new RewardInboxScreen(message.entries()));
            default -> throw new IllegalArgumentException("Unknown client payload: " + payload.type().id());
        }
    }

    private static void openGachaScreen(OpenGachaScreenPayload message) {
        GachaAnnouncementClient.INSTANCE.rewards(GachaAnnouncement.EXPLICIT_REWARDS_MENU, message.rewards());
        GachaType gachaType = message.gachaType().orElseGet(GachaAddictionConfig.CLIENT::getDefaultGachaaType);
        gachaType = GachaPresentationClient.animation(-1, gachaType);
        var client = GachaTypeClients.get(gachaType);
        if (client == null) {
            GachaAnnouncementClient.INSTANCE.presentation(null);
            return;
        }

        var screen = GachaPresentationClient.apply(
                client.create(Minecraft.getInstance().screen, message.lootTableIds(), message.entries()), -1, gachaType);
        Minecraft.getInstance().setScreen(screen);
        NonNullList<ItemStack> items = NonNullList.of(ItemStack.EMPTY, message.rewards().toArray(ItemStack[]::new));
        screen.updateRewards(items);
        GachaAnnouncementClient.INSTANCE.presentation(Minecraft.getInstance().screen);
    }
}

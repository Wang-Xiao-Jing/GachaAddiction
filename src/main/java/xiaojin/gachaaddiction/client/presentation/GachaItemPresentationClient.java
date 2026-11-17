package xiaojin.gachaaddiction.client.presentation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import xiaojin.gachaaddiction.api.GachaSecondaryType;
import xiaojin.gachaaddiction.api.ItemStackEntry;
import xiaojin.gachaaddiction.client.gui.screen.BasicGachaScreen;
import xiaojin.gachaaddiction.client.gui.screen.GachaAnimationScreen;
import xiaojin.gachaaddiction.client.gui.screen.GachaSourceInteractionScreen;
import xiaojin.gachaaddiction.network.cts.FinishGachaItemDrawPayload;
import xiaojin.gachaaddiction.network.stc.GachaItemPresentationPayload;
import xiaojin.gachaaddiction.registry.GachaTypes;
import xiaojin.gachaaddiction.registry.client.GachaTypeClients;

import java.util.List;
import java.util.UUID;

public class GachaItemPresentationClient {
    private static UUID pendingSession;
    private static BasicGachaScreen pendingScreen;
    private static Screen pendingParent;

    public static void clear() {
        pendingSession = null;
        pendingScreen = null;
        pendingParent = null;
    }

    public static void cancelPendingScreen(BasicGachaScreen screen) {
        if (screen != pendingScreen || pendingSession == null) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getConnection() != null && minecraft.getConnection().hasChannel(FinishGachaItemDrawPayload.TYPE)) {
            PacketDistributor.sendToServer(new FinishGachaItemDrawPayload(pendingSession, true));
        }
        clear();
    }

    public static void receive(GachaItemPresentationPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        if (payload.completed()) {
            if (minecraft.screen instanceof GachaSourceInteractionScreen screen && screen.matchesSession(payload.session())) {
                screen.receiveResult(payload.success(), payload.items(), payload.rewardRounds());
            } else if (minecraft.screen instanceof GachaAnimationScreen screen && screen.matchesSession(payload.session())) {
                screen.receiveResult(payload.success(), payload.items());
            } else if (payload.session().equals(pendingSession)) {
                if (pendingScreen != null && minecraft.screen == pendingScreen) {
                    if (payload.success()) {
                        NonNullList<ItemStack> rewards = NonNullList.create();
                        payload.items().forEach(stack -> rewards.add(stack.copy()));
                        pendingScreen.updateRewards(rewards);
                    } else {
                        minecraft.setScreen(pendingParent);
                    }
                }
                clear();
            }
        } else if (minecraft.player != null && minecraft.level != null) {
            var animation = GachaPresentationClient.animation(-1, payload.animation());
            if (animation == GachaTypes.TOTEM || animation == GachaTypes.CHEST
                    && GachaPresentationClient.secondary(-1) != GachaSecondaryType.NONE) {
                minecraft.setScreen(GachaPresentationClient.apply(new GachaSourceInteractionScreen(minecraft.screen,
                        animation, payload.session(), payload.lifetimeMillis()), -1, animation));
            } else if (animation == GachaTypes.CHEST || animation == GachaTypes.CSGO || animation == GachaTypes.WHEEL
                    || animation == GachaTypes.INTERACTIVE_SLOT_MACHINE) {
                minecraft.setScreen(GachaPresentationClient.apply(new GachaAnimationScreen(minecraft.screen,
                        animation, payload.session(), payload.items(), payload.lifetimeMillis()), -1, animation));
            } else {
                List<ItemStack> candidates = animation == GachaTypes.MODERN || animation == GachaTypes.MODERN_2
                        ? payload.possibleItems() : payload.items();
                pendingSession = payload.session();
                pendingParent = minecraft.screen;
                pendingScreen = animation.isEmpty() ? null : GachaPresentationClient.apply(
                        GachaTypeClients.get(animation).create(minecraft.screen, List.of(),
                                candidates.stream().map(stack -> new ItemStackEntry(stack.copy(), 1)).toList()), -1, animation);
                if (pendingScreen != null) {
                    minecraft.setScreen(pendingScreen);
                    UUID session = payload.session();
                    pendingScreen.afterFirstFrame(() -> PacketDistributor.sendToServer(new FinishGachaItemDrawPayload(session, false)));
                } else {
                    PacketDistributor.sendToServer(new FinishGachaItemDrawPayload(payload.session(), false));
                }
            }
        }
    }
}

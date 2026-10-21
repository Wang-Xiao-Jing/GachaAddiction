package xiaojin.gachaaddiction.events.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import xiaojin.gachaaddiction.GachaConstants;
import xiaojin.gachaaddiction.api.GachaType;
import xiaojin.gachaaddiction.api.GachaData;
import xiaojin.gachaaddiction.client.announcement.GachaAnnouncementClient;
import xiaojin.gachaaddiction.client.announcement.GachaAnnouncementTextures;
import xiaojin.gachaaddiction.client.presentation.GachaItemPresentationClient;
import xiaojin.gachaaddiction.client.presentation.GachaPresentationClient;
import xiaojin.gachaaddiction.client.render.*;
import xiaojin.gachaaddiction.mixed.IAbstractContainerMenu;
import xiaojin.gachaaddiction.client.resource.GachaRewardSoundResources;
import xiaojin.gachaaddiction.registry.client.GachaTypeClients;

@EventBusSubscriber(modid = GachaConstants.MODID, value = Dist.CLIENT)
public class GachaClientEvents {
    @SubscribeEvent
    public static void register(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(GachaRewardSoundResources.INSTANCE);
        event.registerReloadListener(GachaAnnouncementTextures.INSTANCE);
        event.registerReloadListener((ResourceManagerReloadListener) manager -> GachaObjectOutline.resourceRevision++);
        event.registerReloadListener((ResourceManagerReloadListener) manager -> GachaReelFade.resourceRevision++);
        event.registerReloadListener((ResourceManagerReloadListener) resourceManager -> ModernGachaEffect.resourceRevision++);
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntity() == Minecraft.getInstance().player) {
            GachaSceneSources.rememberSource(event.getPos());
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getEntity() == Minecraft.getInstance().player) {
            GachaSceneSources.rememberItemSource(event.getEntity().getItemInHand(event.getHand()));
        }
    }

    @SubscribeEvent
    public static void onRightClickEntity(PlayerInteractEvent.EntityInteract event) {
        if (event.getEntity() == Minecraft.getInstance().player) {
            GachaSceneSources.rememberEntitySource(event.getTarget());
        }
    }

    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening event) {
        Screen screen = event.getScreen();
        if (!(screen instanceof MenuAccess<?> menuAccess)) {
            return;
        }

        @Nullable GachaData gachaaData = IAbstractContainerMenu.of(menuAccess.getMenu())
                .gachaaddiction$getGachaaData();
        if (gachaaData == null || !gachaaData.isOpenGachaaUi()) {
            return;
        }

        GachaType gachaType = gachaaData.getGachaType();
        gachaType = GachaPresentationClient.animation(menuAccess.getMenu().containerId, gachaType);
        if (!gachaType.isEmpty()) {
            event.setNewScreen(GachaPresentationClient.apply(
                    GachaTypeClients.get(gachaType).create(screen, gachaaData), menuAccess.getMenu().containerId, gachaType));
        }
    }

    @SubscribeEvent
    public static void onScreenRenderPre(ScreenEvent.Render.Pre event) {
        GachaAnnouncementClient.INSTANCE.startFrame();
    }

    @SubscribeEvent
    public static void onScreenRenderPost(ScreenEvent.Render.Post event) {
        GachaAnnouncementClient.INSTANCE.render(event.getGuiGraphics(), event.getMouseX(), event.getMouseY());
    }

    @SubscribeEvent
    public static void onGuiRender(RenderGuiEvent.Post event) {
        if (Minecraft.getInstance().screen == null) {
            GachaAnnouncementClient.INSTANCE.render(event.getGuiGraphics(), -1, -1);
        }
    }

    @SubscribeEvent
    public static void onItemTooltip(RenderTooltipEvent.Pre event) {
        GachaAnnouncementClient.INSTANCE.tooltip(event.getItemStack());
    }

    @SubscribeEvent
    public static void onContainerBackground(ContainerScreenEvent.Render.Background event) {
        GachaAnnouncementClient.INSTANCE.containerBackground(event.getContainerScreen(), event.getGuiGraphics());
    }

    @SubscribeEvent
    public static void onKeyPressed(ScreenEvent.KeyPressed.Pre event) {
        if ((event.getKeyCode() == GLFW.GLFW_KEY_LEFT_ALT || event.getKeyCode() == GLFW.GLFW_KEY_RIGHT_ALT)
                && GachaAnnouncementClient.INSTANCE.manual(event.getScreen())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onKeyReleased(ScreenEvent.KeyReleased.Pre event) {
        if (event.getKeyCode() == GLFW.GLFW_KEY_LEFT_ALT || event.getKeyCode() == GLFW.GLFW_KEY_RIGHT_ALT) {
            GachaAnnouncementClient.INSTANCE.releaseManualKey();
        }
    }

    @SubscribeEvent
    public static void onLoggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        GachaPresentationClient.clear();
        GachaItemPresentationClient.clear();
        GachaSceneSources.clearSource();
        GachaAnnouncementClient.INSTANCE.clear();
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        GachaPresentationClient.tick();
    }

}

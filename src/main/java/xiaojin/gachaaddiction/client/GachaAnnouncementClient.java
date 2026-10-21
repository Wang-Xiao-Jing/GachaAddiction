package xiaojin.gachaaddiction.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackLinkedSet;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import xiaojin.gachaaddiction.GachaAddiction;
import xiaojin.gachaaddiction.api.GachaAnnouncement;
import xiaojin.gachaaddiction.api.IGachaAnnouncementApi;
import xiaojin.gachaaddiction.client.gui.screen.BasicGachaScreen;
import xiaojin.gachaaddiction.config.GachaAddictionConfig;
import xiaojin.gachaaddiction.mixed.IAbstractContainerMenu;
import xiaojin.gachaaddiction.network.cts.RequestGachaAnnouncementPayload;
import xiaojin.gachaaddiction.util.GachaAnnouncements;
import xiaojin.gachaaddiction.util.RarityUtil;
import xiaojin.gachaaddiction.util.TranslationKeys;
import xiaojin.gachaaddiction.util.client.GachaRewardClientUtil;

import java.util.*;

public class GachaAnnouncementClient implements IGachaAnnouncementApi {
    public static final GachaAnnouncementClient INSTANCE = new GachaAnnouncementClient();
    public static final int ELIGIBLE_BORDER_COLOR = 0x4D90EE90;
    private static final int MARGIN = 8;
    private static final int ROW_HEIGHT = Toast.SLOT_HEIGHT;
    private static final int ROW_GAP = 4;
    private static final int TOAST_WIDTH = 320;
    private static final int BANNER_TOP = 36;
    private static final int ITEM_SIZE = 16;
    private static final int TEXT_PADDING = 6;
    private static final int MINIMUM_NOTICE_WIDTH = ITEM_SIZE + TEXT_PADDING * 3 + 1;
    private static final int TEXT_COLOR = 0xFFFFFF;
    private static final int OVERLAY_DEPTH = 2000;
    private static final int ELIGIBLE_SLOT_PADDING = 1;
    private static final int MILLISECONDS_PER_SECOND = 1000;
    private static final int HOVER_VALID_MILLIS = 250;
    private static final float SCROLL_PADDING_FRACTION = 0.2F;
    private static final ResourceLocation TOAST_BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(GachaAddiction.MODID, "announcement/toast");
    private static final ResourceLocation BANNER_BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(GachaAddiction.MODID, "announcement/banner");
    private UUID sessionToken;
    private int menuId = GachaAnnouncement.EXPLICIT_REWARDS_MENU;
    private boolean rewardsKnown;
    private long sessionStartedAt;
    private boolean manualKeyDown;
    private List<ItemStack> sessionItems = List.of();
    private final Set<ItemStack> sessionTypes = ItemStackLinkedSet.createTypeAndComponentsSet();
    private @Nullable Screen presentationScreen;
    private @Nullable Screen tooltipScreen;
    private ItemStack tooltipItem = ItemStack.EMPTY;
    private long tooltipAt;
    private boolean renderingNotices;
    private boolean pointerOverNotice;
    private final List<Notice> toasts = new ArrayList<>();
    private final AnnouncementToast[] toastViews = new AnnouncementToast[GachaAnnouncement.MAXIMUM_ITEMS];
    private final List<Notice> banners = new ArrayList<>();

    public void begin(UUID token, int sourceMenuId) {
        sessionToken = token;
        menuId = sourceMenuId;
        rewardsKnown = false;
        manualKeyDown = false;
        var level = Minecraft.getInstance().level;
        sessionStartedAt = level == null ? 0 : level.getGameTime();
        sessionItems = List.of();
        sessionTypes.clear();
        presentationScreen = null;
        tooltipItem = ItemStack.EMPTY;
        tooltipScreen = null;
    }

    public void containerContents(AbstractContainerMenu menu) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || menu.containerId != menuId) {
            return;
        }
        List<ItemStack> items = new ArrayList<>();
        for (var slot : menu.slots) {
            if (slot.container != minecraft.player.getInventory() && !slot.getItem().isEmpty()) {
                items.add(slot.getItem().copy());
            }
        }
        rewards(menu.containerId, items);
        if (rewardsKnown && presentationScreen == null) {
            presentationScreen = minecraft.screen;
        }
    }

    /**
     * 使用原有容器或奖励包里的实际结果，整个开箱只筛选一次。
     */
    public void rewards(int sourceMenuId, List<ItemStack> items) {
        if (sessionToken == null || menuId != sourceMenuId || rewardsKnown) {
            return;
        }
        rewardsKnown = true;
        sessionItems = List.copyOf(GachaAnnouncement.mergeItems(items));
        sessionTypes.addAll(sessionItems);
        if (!GachaAddictionConfig.CLIENT.automaticAnnouncements.get()) {
            return;
        }
        List<ItemStack> selected = sessionItems.stream().filter(GachaRewardClientUtil::isEligible)
                .sorted(Comparator.comparingInt(RarityUtil::getRarityLevel).reversed())
                .limit(GachaAnnouncement.MAXIMUM_ITEMS).toList();
        send(selected, Trigger.AUTOMATIC);
    }

    public void presentation(@Nullable Screen screen) {
        if (menuId == GachaAnnouncement.EXPLICIT_REWARDS_MENU && rewardsKnown) {
            if (screen == null) {
                closeSource();
            } else {
                presentationScreen = screen;
            }
        }
    }

    public boolean canAnnounce(Screen screen, ItemStack item) {
        Minecraft minecraft = Minecraft.getInstance();
        if (sessionToken == null || !rewardsKnown || screen != presentationScreen
                || minecraft.screen != presentationScreen
                || minecraft.player == null || minecraft.getConnection() == null
                || minecraft.level == null
                || minecraft.level.getGameTime() - sessionStartedAt > GachaAnnouncement.SESSION_LIFETIME_TICKS
                || !minecraft.getConnection().hasChannel(RequestGachaAnnouncementPayload.TYPE)
                || item.isEmpty() || !sessionTypes.contains(item)) {
            return false;
        }
        if (menuId == GachaAnnouncement.EXPLICIT_REWARDS_MENU) {
            return true;
        }
        if (minecraft.player.containerMenu == minecraft.player.inventoryMenu
                || minecraft.player.containerMenu.containerId != menuId) {
            return false;
        }
        var data = IAbstractContainerMenu.of(minecraft.player.containerMenu).gachaaddiction$getGachaaData();
        if (data == null || !data.isOpenGachaaUi()) {
            return false;
        }
        Screen source = screen instanceof BasicGachaScreen gacha ? gacha.getOriginalScreen() : screen;
        return source instanceof MenuAccess<?> access && access.getMenu().containerId == menuId;
    }

    public void startFrame() {
        screenChanged(Minecraft.getInstance().screen);
        tooltipItem = ItemStack.EMPTY;
        tooltipScreen = null;
        pointerOverNotice = false;
        long window = Minecraft.getInstance().getWindow().getWindow();
        if (!InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_ALT)
                && !InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_ALT)) {
            manualKeyDown = false;
        }
    }

    public void containerBackground(AbstractContainerScreen<?> screen, GuiGraphics graphics) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        for (var slot : screen.getMenu().slots) {
            if (slot.isActive() && slot.container != minecraft.player.getInventory()
                    && canAnnounce(screen, slot.getItem())) {
                int left = screen.getGuiLeft() + slot.x - ELIGIBLE_SLOT_PADDING;
                int top = screen.getGuiTop() + slot.y - ELIGIBLE_SLOT_PADDING;
                int right = left + ITEM_SIZE + ELIGIBLE_SLOT_PADDING * 2;
                int bottom = top + ITEM_SIZE + ELIGIBLE_SLOT_PADDING * 2;
                graphics.fill(left, top, right, top + ELIGIBLE_SLOT_PADDING, ELIGIBLE_BORDER_COLOR);
                graphics.fill(left, bottom - ELIGIBLE_SLOT_PADDING, right, bottom, ELIGIBLE_BORDER_COLOR);
                graphics.fill(left, top + ELIGIBLE_SLOT_PADDING, left + ELIGIBLE_SLOT_PADDING,
                        bottom - ELIGIBLE_SLOT_PADDING, ELIGIBLE_BORDER_COLOR);
                graphics.fill(right - ELIGIBLE_SLOT_PADDING, top + ELIGIBLE_SLOT_PADDING, right,
                        bottom - ELIGIBLE_SLOT_PADDING, ELIGIBLE_BORDER_COLOR);
            }
        }
    }

    public void tooltip(ItemStack stack) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!renderingNotices && minecraft.screen != null && canAnnounce(minecraft.screen, stack)) {
            tooltipItem = stack.copy();
            tooltipScreen = minecraft.screen;
            tooltipAt = Util.getMillis();
        }
    }

    public boolean manual(Screen screen) {
        if (manualKeyDown) {
            return false;
        }
        manualKeyDown = true;
        if (pointerOverNotice) {
            return false;
        }
        ItemStack selected = tooltipItem;
        if (screen instanceof AbstractContainerScreen<?> containerScreen) {
            var player = Minecraft.getInstance().player;
            var slot = containerScreen.getSlotUnderMouse();
            if (player == null || slot == null || !slot.isActive() || slot.container == player.getInventory()) {
                return false;
            }
            selected = slot.getItem();
        } else if (screen != tooltipScreen || Util.getMillis() - tooltipAt > HOVER_VALID_MILLIS) {
            return false;
        }
        if (!canAnnounce(screen, selected)) {
            return false;
        }
        request(selected, Trigger.MANUAL);
        return true;
    }

    public void releaseManualKey() {
        manualKeyDown = false;
    }

    @Override
    public void request(ItemStack item, Trigger trigger) {
        Screen screen = Minecraft.getInstance().screen;
        if (trigger == Trigger.MANUAL && (screen == null || !canAnnounce(screen, item))) {
            return;
        }
        send(List.of(item), trigger);
    }

    private void send(List<ItemStack> items, Trigger trigger) {
        Minecraft minecraft = Minecraft.getInstance();
        if (sessionToken != null && !items.isEmpty() && minecraft.getConnection() != null
                && minecraft.getConnection().hasChannel(RequestGachaAnnouncementPayload.TYPE)) {
            PacketDistributor.sendToServer(new RequestGachaAnnouncementPayload(
                    sessionToken, trigger == Trigger.MANUAL, items));
        }
    }

    @Override
    public void broadcast(ServerPlayer player, ItemStack item, Trigger trigger) {
        GachaAnnouncements.broadcast(player, item, trigger);
    }

    @Override
    public void receive(GachaAnnouncement announcement) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || announcement.playerId().equals(minecraft.player.getUUID())) {
            return;
        }
        var config = GachaAddictionConfig.CLIENT;
        long now = Util.getMillis();
        for (ItemStack item : announcement.items()) {
            Component message = message(announcement, item);
            if (config.announcementChat.get()) {
                minecraft.gui.getChat().addMessage(message);
            }
            Notice notice = new Notice(message, item, RarityUtil.getRarityLevel(item), now, new BackgroundSelection());
            if (config.announcementToast.get()) {
                toasts.add(notice);
            }
            if (config.announcementBanner.get()) {
                banners.add(notice);
            }
        }
        rankAndLimit(toasts);
        trim(banners, now);
        syncToasts();
    }

    private Component message(GachaAnnouncement announcement, ItemStack item) {
        var source = announcement.source();
        Component locationName = source.structure().map(this::structureName).orElse(source.dimensionName());
        Component containerName = source.customName().<Component>map(custom ->
                        Component.translatable(TranslationKeys.ANNOUNCEMENT_RENAMED, source.containerName(), custom))
                .orElse(source.containerName());
        var itemName = item.getHoverName().copy().withStyle(style -> style.withColor(RarityUtil.getRarityColor(item)));
        if (GachaAddictionConfig.CLIENT.announcementTooltips.get()) {
            itemName.withStyle(style -> style.withHoverEvent(
                    new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(item))));
        }
        return Component.translatable(TranslationKeys.ANNOUNCEMENT_FORMAT, announcement.playerName(),
                locationName, containerName, RarityUtil.getRarityName(item), itemName);
    }

    private Component structureName(ResourceLocation location) {
        String standardKey = location.toLanguageKey(TranslationKeys.STRUCTURE_NAME_PREFIX);
        if (I18n.exists(standardKey)) {
            return Component.translatable(standardKey);
        }
        String fallbackKey = location.toLanguageKey(TranslationKeys.STRUCTURE_FALLBACK_NAME_PREFIX);
        return Component.translatableWithFallback(fallbackKey, location.toString());
    }

    private void trim(List<Notice> notices, long now) {
        long duration = (long) GachaAddictionConfig.CLIENT.announcementSeconds.get() * MILLISECONDS_PER_SECOND;
        notices.removeIf(notice -> now - notice.createdAt >= duration);
        rankAndLimit(notices);
    }

    private void rankAndLimit(List<Notice> notices) {
        notices.sort(Comparator.comparingInt(Notice::rarity).reversed()
                .thenComparing(Comparator.comparingLong(Notice::createdAt).reversed()));
        if (notices.size() > GachaAnnouncement.MAXIMUM_ITEMS) {
            notices.subList(GachaAnnouncement.MAXIMUM_ITEMS, notices.size()).clear();
        }
    }

    private int toastWidth() {
        return Math.clamp(Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2 - MARGIN,
                MINIMUM_NOTICE_WIDTH, TOAST_WIDTH);
    }

    private void syncToasts() {
        Minecraft minecraft = Minecraft.getInstance();
        if (!GachaAddictionConfig.CLIENT.announcementToast.get()) {
            toasts.clear();
        }
        for (int index = 0; index < toasts.size(); index++) {
            Notice notice = toasts.get(index);
            AnnouncementToast active = minecraft.getToasts().getToast(AnnouncementToast.class, index);
            if (active != null) {
                active.update(notice);
                toastViews[index] = active;
            } else if (toastViews[index] == null || toastViews[index].notice != notice) {
                // 每个槽位只提交当前消息一次，同时遵守原版 ToastAddEvent 的取消结果。
                AnnouncementToast toast = new AnnouncementToast(this, index, notice);
                toastViews[index] = toast;
                minecraft.getToasts().addToast(toast);
                if (minecraft.getToasts().getToast(AnnouncementToast.class, index) == null) {
                    toasts.remove(index);
                    index--;
                }
            }
        }
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui) {
            return;
        }
        long now = Util.getMillis();
        syncToasts();
        trim(banners, now);
        var config = GachaAddictionConfig.CLIENT;
        renderingNotices = true;
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, OVERLAY_DEPTH);
        try {
            if (config.announcementBanner.get()) {
                int width = Math.max(MINIMUM_NOTICE_WIDTH, graphics.guiWidth() - MARGIN * 4);
                long duration = (long) config.announcementSeconds.get() * MILLISECONDS_PER_SECOND;
                for (int index = 0; index < banners.size(); index++) {
                    Notice notice = banners.get(index);
                    float progress = Math.clamp((float) (now - notice.createdAt) / duration, 0.0F, 1.0F);
                    drawNotice(graphics, notice, (graphics.guiWidth() - width) / 2,
                            BANNER_TOP + index * (ROW_HEIGHT + ROW_GAP), width, true, mouseX, mouseY, progress);
                }
            }
        } finally {
            graphics.pose().popPose();
            renderingNotices = false;
        }
    }

    private void drawNotice(GuiGraphics graphics, Notice notice, int x, int y, int width, boolean scrolling,
                            int mouseX, int mouseY, float progress) {
        Minecraft minecraft = Minecraft.getInstance();
        if (mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + ROW_HEIGHT) {
            pointerOverNotice = true;
            tooltipItem = ItemStack.EMPTY;
            tooltipScreen = null;
        }
        var backgrounds = notice.backgrounds.resolve(notice.item);
        graphics.blitSprite(scrolling ? backgrounds.banner() : backgrounds.toast(), x, y, width, ROW_HEIGHT);
        int iconX = x + width - TEXT_PADDING - ITEM_SIZE;
        int iconY = y + (ROW_HEIGHT - ITEM_SIZE) / 2;
        int textLeft = x + TEXT_PADDING;
        int textRight = iconX - TEXT_PADDING;
        int available = Math.max(1, textRight - textLeft);
        FormattedCharSequence sequence = notice.message.getVisualOrderText();
        int textWidth = minecraft.font.width(sequence);
        int textX = scrolling ? textRight - (int) ((available + textWidth) * progress) : textLeft;
        if (!scrolling && textWidth > available) {
            float travel = Math.clamp((progress - SCROLL_PADDING_FRACTION)
                    / (1.0F - SCROLL_PADDING_FRACTION * 2), 0.0F, 1.0F);
            textX -= (int) ((textWidth - available) * travel);
        }
        int textY = y + (ROW_HEIGHT - minecraft.font.lineHeight) / 2;
        int screenOffsetX = Math.round(graphics.pose().last().pose().m30());
        int screenOffsetY = Math.round(graphics.pose().last().pose().m31());
        graphics.enableScissor(textLeft + screenOffsetX, y + screenOffsetY,
                textRight + screenOffsetX, y + ROW_HEIGHT + screenOffsetY);
        try {
            graphics.drawString(minecraft.font, sequence, textX, textY, TEXT_COLOR, true);
        } finally {
            graphics.disableScissor();
        }
        graphics.renderFakeItem(notice.item, iconX, iconY);
        graphics.renderItemDecorations(minecraft.font, notice.item, iconX, iconY);
        if (minecraft.screen == null || !GachaAddictionConfig.CLIENT.announcementTooltips.get()
                || mouseY < y || mouseY >= y + ROW_HEIGHT) {
            return;
        }
        boolean itemHovered = mouseX >= iconX && mouseX < iconX + ITEM_SIZE;
        Style style = null;
        if (!itemHovered && mouseX >= textLeft && mouseX < textRight && mouseX >= textX && mouseX < textX + textWidth) {
            style = minecraft.font.getSplitter().componentStyleAtWidth(sequence, mouseX - textX);
        }
        if (!itemHovered && (style == null || style.getHoverEvent() == null)) {
            return;
        }
        // 原版 Toast 在平移后的局部坐标中绘制，悬浮提示需要回到屏幕坐标以正确裁边。
        int screenMouseX = mouseX + screenOffsetX;
        int screenMouseY = mouseY + screenOffsetY;
        graphics.pose().pushPose();
        graphics.pose().setIdentity();
        graphics.pose().translate(0, 0, OVERLAY_DEPTH);
        try {
            if (itemHovered) {
                graphics.renderTooltip(minecraft.font, notice.item, screenMouseX, screenMouseY);
            } else {
                graphics.renderComponentHoverEffect(minecraft.font, style, screenMouseX, screenMouseY);
            }
        } finally {
            graphics.pose().popPose();
        }
    }

    public void close(Screen screen) {
        if (screen == presentationScreen) {
            closeSource();
        }
    }

    public void screenChanged(@Nullable Screen screen) {
        if (presentationScreen == null || screen == presentationScreen) {
            return;
        }
        if (menuId != GachaAnnouncement.EXPLICIT_REWARDS_MENU
                && isCurrentContainerView(presentationScreen) && isCurrentContainerView(screen)) {
            // 同一菜单的动画与原容器切换属于一次开箱，切换完成后再绑定实际显示的界面。
            presentationScreen = screen;
            tooltipItem = ItemStack.EMPTY;
            tooltipScreen = null;
            pointerOverNotice = false;
            return;
        }
        closeSource();
    }

    private boolean isCurrentContainerView(@Nullable Screen screen) {
        var player = Minecraft.getInstance().player;
        Screen source = screen instanceof BasicGachaScreen gacha ? gacha.getOriginalScreen() : screen;
        return player != null && player.containerMenu != player.inventoryMenu
                && player.containerMenu.containerId == menuId
                && source instanceof MenuAccess<?> access && access.getMenu() == player.containerMenu;
    }

    private void closeSource() {
        var connection = Minecraft.getInstance().getConnection();
        if (sessionToken != null && connection != null && connection.hasChannel(RequestGachaAnnouncementPayload.TYPE)) {
            PacketDistributor.sendToServer(new RequestGachaAnnouncementPayload(sessionToken, false, List.of()));
        }
        clearSource();
    }

    private void clearSource() {
        sessionToken = null;
        rewardsKnown = false;
        manualKeyDown = false;
        pointerOverNotice = false;
        sessionItems = List.of();
        sessionTypes.clear();
        presentationScreen = null;
        tooltipScreen = null;
        tooltipItem = ItemStack.EMPTY;
    }

    public void clear() {
        clearSource();
        toasts.clear();
        banners.clear();
        for (int index = 0; index < toastViews.length; index++) {
            AnnouncementToast toast = Minecraft.getInstance().getToasts().getToast(AnnouncementToast.class, index);
            if (toast != null) {
                toast.notice = null;
            }
            toastViews[index] = null;
        }
    }

    private static class AnnouncementToast implements Toast {
        private final GachaAnnouncementClient owner;
        private final int slot;
        private @Nullable Notice notice;
        private boolean changed = true;
        private long lastChanged;

        private AnnouncementToast(GachaAnnouncementClient owner, int slot, Notice notice) {
            this.owner = owner;
            this.slot = slot;
            this.notice = notice;
        }

        private void update(Notice notice) {
            if (this.notice != notice) {
                this.notice = notice;
                changed = true;
            }
        }

        @Override
        public Object getToken() {
            return slot;
        }

        @Override
        public int width() {
            return owner.toastWidth();
        }

        @Override
        public Visibility render(GuiGraphics graphics, ToastComponent component, long timeSinceLastVisible) {
            Minecraft minecraft = component.getMinecraft();
            if (notice == null || minecraft.player == null) {
                return Visibility.HIDE;
            }
            if (changed) {
                lastChanged = timeSinceLastVisible;
                changed = false;
            }
            double duration = Math.max(1.0, GachaAddictionConfig.CLIENT.announcementSeconds.get()
                    * (double) MILLISECONDS_PER_SECOND * component.getNotificationDisplayTimeMultiplier());
            long elapsed = timeSinceLastVisible - lastChanged;
            boolean expired = elapsed >= duration;
            boolean selected = slot < owner.toasts.size() && owner.toasts.get(slot) == notice;
            if (expired && selected) {
                owner.toasts.remove(slot);
            }
            Visibility visibility = !expired && GachaAddictionConfig.CLIENT.announcementToast.get()
                    && selected ? Visibility.SHOW : Visibility.HIDE;
            int mouseX = -1;
            int mouseY = -1;
            if (minecraft.screen != null) {
                int screenMouseX = (int) (minecraft.mouseHandler.xpos() * minecraft.getWindow().getGuiScaledWidth()
                        / minecraft.getWindow().getScreenWidth());
                int screenMouseY = (int) (minecraft.mouseHandler.ypos() * minecraft.getWindow().getGuiScaledHeight()
                        / minecraft.getWindow().getScreenHeight());
                mouseX = screenMouseX - Math.round(graphics.pose().last().pose().m30());
                mouseY = screenMouseY - Math.round(graphics.pose().last().pose().m31());
            }
            float progress = (float) Math.clamp(elapsed / duration, 0.0, 1.0);
            boolean wasRenderingNotices = owner.renderingNotices;
            owner.renderingNotices = true;
            try {
                owner.drawNotice(graphics, notice, 0, 0, width(), false, mouseX, mouseY, progress);
            } finally {
                owner.renderingNotices = wasRenderingNotices;
            }
            return visibility;
        }
    }

    private static class BackgroundSelection {
        private long revision;
        private @Nullable GachaAnnouncementTextures.Backgrounds backgrounds;

        private GachaAnnouncementTextures.Backgrounds resolve(ItemStack item) {
            var textures = GachaAnnouncementTextures.INSTANCE;
            if (backgrounds == null || revision != textures.revision()) {
                backgrounds = textures.select(item, TOAST_BACKGROUND, BANNER_BACKGROUND);
                revision = textures.revision();
            }
            return backgrounds;
        }
    }

    private record Notice(Component message, ItemStack item, int rarity, long createdAt,
                          BackgroundSelection backgrounds) {
    }
}

package xiaojin.gachaaddiction.client.gui.screen;

import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import xiaojin.gachaaddiction.api.ItemStackEntry;
import xiaojin.gachaaddiction.api.RewardData;
import xiaojin.gachaaddiction.client.render.GachaAnimationRenderer;
import xiaojin.gachaaddiction.client.render.ModernGachaEffect;
import xiaojin.gachaaddiction.client.render.ModernGachaEntrance;
import xiaojin.gachaaddiction.util.TranslationKeys;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Modern2GachaScreen extends GachaSummaryScreen {
    private static final int MINIMUM_WIDTH = 64;
    private static final int ITEM_GAP = 8;
    private static final float REGION_WIDTH = 0.8F;
    private static final float REGION_HEIGHT = 0.6F;
    private static final int VERTICAL_MARGIN = 48;
    private static final int LABEL_INSET = 4;
    private static final int MAXIMUM_NAME_LINES = 2;
    private static final int FOOTER_MARGIN = 20;
    private static final int PAGE_LABEL_MARGIN = 34;
    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final int LABEL_BACKGROUND = 0xA0080A10;
    private static final int NATIVE_ITEM_SIZE = 16;
    private static final int OVERLAY_DEPTH = 1000;
    private static final int DEFAULT_COLOR = 0xD0D0D0;
    private static final long REVEAL_MILLIS = 350;
    private static final float MILLIS_PER_SECOND = 1000.0F;
    private final ModernGachaEffect pageEffect = new ModernGachaEffect();
    private final ModernGachaEntrance pageEntrance = new ModernGachaEntrance();
    private final Set<RewardData> revealedRewards = new HashSet<>();
    private final Set<Integer> pressedKeys = new HashSet<>();
    private int page;
    private boolean ready;
    private boolean revealing;
    private long revealStartedAt;
    private long effectStartedAt;

    public Modern2GachaScreen(@Nullable Screen parent, List<@Nullable ResourceLocation> tables,
                              List<ItemStackEntry> entries) {
        super(parent, tables, entries);
    }

    @Override
    protected void refreshWidgets() {
        ready = true;
        page = 0;
        effectStartedAt = 0;
        pageEntrance.reset(false);
        if (rewards.isEmpty()) {
            finishPages();
        }
    }

    private PageLayout pageLayout() {
        int available = Math.max(1, (int) (width * REGION_WIDTH));
        int capacity = Math.max(1, Math.min(rewards.size(), (available + ITEM_GAP) / (MINIMUM_WIDTH + ITEM_GAP)));
        int cardWidth = Math.max(MINIMUM_WIDTH, (available - (capacity - 1) * ITEM_GAP) / capacity);
        int cardHeight = Math.max(1, Math.min((int) (height * REGION_HEIGHT), height - VERTICAL_MARGIN * 2));
        int pages = Math.max(1, (rewards.size() + capacity - 1) / capacity);
        page = Math.clamp(page, 0, pages - 1);
        int first = page * capacity;
        int count = Math.min(capacity, rewards.size() - first);
        int rowWidth = count * cardWidth + Math.max(0, count - 1) * ITEM_GAP;
        return new PageLayout((width - rowWidth) / 2, (height - cardHeight) / 2,
                cardWidth, cardHeight, first, count, pages);
    }

    private boolean pageRevealed(PageLayout layout) {
        return rewards.subList(layout.first, layout.first + layout.count).stream().allMatch(revealedRewards::contains);
    }

    private float revealProgress(PageLayout layout) {
        if (pageRevealed(layout)) {
            return 1.0F;
        }
        return revealing ? GachaAnimationRenderer.smoothProgress((float) (Util.getMillis() - revealStartedAt) / REVEAL_MILLIS) : 0.0F;
    }

    private void completePage(PageLayout layout) {
        for (int index = layout.first; index < layout.first + layout.count; index++) {
            RewardData reward = rewards.get(index);
            if (revealedRewards.add(reward)) {
                completePresentedReward(reward, true);
            }
        }
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (!ready || isShowingSummary()) {
            renderSummaryScreen(graphics, mouseX, mouseY, partialTick);
            return;
        }

        PageLayout layout = pageLayout();
        long now = Util.getMillis();
        if (effectStartedAt == 0) {
            effectStartedAt = now;
        }
        ModernGachaEntrance.Frame entry = pageEntrance.sample(now);
        float reveal = revealProgress(layout);
        if (reveal >= 1.0F) {
            completePage(layout);
        }
        int color = rewards.subList(layout.first, layout.first + layout.count).stream()
                .max(Comparator.comparingInt(RewardData::getRarityLevel)).map(RewardData::getRarityColor).orElse(DEFAULT_COLOR);
        float seconds = (now - effectStartedAt) / MILLIS_PER_SECOND;
        if (!pageEffect.renderItems(graphics, color, seconds, reveal,
                target -> renderPageItems(target, layout, 1.0F, 1.0F, entry), entry)) {
            graphics.fill(0, 0, width, height, GachaAnimationRenderer.backgroundColor());
            renderPageItems(graphics, layout, reveal, entry.opacity(), entry);
        }
        renderLabels(graphics, layout, mouseX, mouseY, reveal >= 1.0F && pageEntrance.isComplete(), entry);
        renderSourceTitle(graphics);
        graphics.drawCenteredString(font, Component.translatable(TranslationKeys.GUI_SECONDARY_PAGE, page + 1, layout.pages),
                width / 2, height - PAGE_LABEL_MARGIN, TEXT_COLOR);
        graphics.drawCenteredString(font, Component.translatable(reveal >= 1.0F
                        ? TranslationKeys.GUI_MODERN_NEXT_HINT : TranslationKeys.GUI_MODERN_REVEAL_HINT),
                width / 2, height - FOOTER_MARGIN, TEXT_COLOR);
    }

    private void renderPageItems(GuiGraphics graphics, PageLayout layout, float brightness, float opacity,
                                 ModernGachaEntrance.Frame entry) {
        int size = Math.max(layout.width, layout.height);
        for (int local = 0; local < layout.count; local++) {
            int left = layout.left + local * (layout.width + ITEM_GAP);
            graphics.enableScissor(left, layout.top, left + layout.width, layout.top + layout.height);
            graphics.pose().pushPose();
            try {
                entry.apply(graphics, left + layout.width / 2.0F, layout.top + layout.height / 2.0F);
                graphics.pose().translate(left + (layout.width - size) / 2.0F, layout.top + (layout.height - size) / 2.0F, 0.0F);
                float scale = size / (float) NATIVE_ITEM_SIZE;
                graphics.pose().scale(scale, scale, 1.0F);
                graphics.setColor(brightness, brightness, brightness, opacity);
                graphics.renderFakeItem(rewards.get(layout.first + local).getItemStack(), 0, 0);
                graphics.flush();
            } finally {
                graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                graphics.pose().popPose();
                graphics.disableScissor();
            }
        }
    }

    private void renderLabels(GuiGraphics graphics, PageLayout layout, int mouseX, int mouseY, boolean revealed,
                               ModernGachaEntrance.Frame entry) {
        ItemStack hovered = ItemStack.EMPTY;
        for (int local = 0; local < layout.count; local++) {
            RewardData reward = rewards.get(layout.first + local);
            int left = layout.left + local * (layout.width + ITEM_GAP);
            graphics.enableScissor(left, layout.top, left + layout.width, layout.top + layout.height);
            graphics.pose().pushPose();
            try {
                graphics.pose().translate(0.0F, 0.0F, OVERLAY_DEPTH);
                int borderAlpha = (int) (255 * entry.opacity() * entry.rim());
                graphics.renderOutline(left, layout.top, layout.width, layout.height,
                        reward.getRarityColor() & 0xFFFFFF | borderAlpha << 24);
                if (revealed) {
                    var lines = font.split(reward.getItemStack().getHoverName(), Math.max(1, layout.width - LABEL_INSET * 2));
                    int count = Math.min(MAXIMUM_NAME_LINES, lines.size());
                    graphics.fill(left, layout.top, left + layout.width, layout.top + count * font.lineHeight + LABEL_INSET * 2, LABEL_BACKGROUND);
                    for (int line = 0; line < count; line++) {
                        graphics.drawString(font, lines.get(line), left + LABEL_INSET, layout.top + LABEL_INSET + line * font.lineHeight, TEXT_COLOR, true);
                    }
                    Component amount = Component.translatable(TranslationKeys.GUI_MODERN_COUNT, reward.getCount());
                    graphics.drawString(font, amount, left + layout.width - font.width(amount) - LABEL_INSET,
                            layout.top + layout.height - font.lineHeight - LABEL_INSET, TEXT_COLOR, true);
                }
            } finally {
                graphics.pose().popPose();
                graphics.disableScissor();
            }
            if (revealed && mouseX >= left && mouseX < left + layout.width && mouseY >= layout.top && mouseY < layout.top + layout.height) {
                hovered = reward.getCountItemStack();
            }
        }
        if (!hovered.isEmpty()) {
            renderSummaryTooltip(graphics, hovered, mouseX, mouseY);
        }
    }

    private void finishPages() {
        beginSummary();
    }

    private void advance(boolean instant) {
        if (!pageEntrance.isComplete()) {
            if (instant && !isForceWatch()) {
                pageEntrance.finish();
            } else {
                return;
            }
        }
        PageLayout layout = pageLayout();
        if (revealProgress(layout) < 1.0F) {
            if (!revealing) {
                revealing = true;
                revealStartedAt = Util.getMillis();
            }
            if (instant && !isForceWatch()) {
                completePage(layout);
            }
            return;
        }

        completePage(layout);
        if (page + 1 >= layout.pages) {
            finishPages();
        } else {
            changePage(page + 1);
        }
    }

    private void changePage(int target) {
        PageLayout layout = pageLayout();
        int nextPage = Math.clamp(target, 0, layout.pages - 1);
        if (nextPage == page || !pageEntrance.isComplete()) {
            return;
        }
        page = nextPage;
        revealing = false;
        effectStartedAt = 0;
        pageEntrance.reset(true);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!ready || isShowingSummary() || button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        advance(false);
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!ready || isShowingSummary()) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        double direction = scrollY != 0.0 ? scrollY : scrollX;
        PageLayout layout = pageLayout();
        if (direction != 0.0 && (!isForceWatch() || pageRevealed(layout))) {
            changePage(page - (int) Math.signum(direction));
        }
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!ready || isShowingSummary()) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        if (keyCode == GLFW.GLFW_KEY_SPACE || keyCode == GLFW.GLFW_KEY_LEFT_SHIFT || keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT
                || keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_RIGHT) {
            if (!pressedKeys.add(keyCode)) {
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_SPACE || keyCode == GLFW.GLFW_KEY_RIGHT) {
                advance(keyCode == GLFW.GLFW_KEY_SPACE);
            } else if (keyCode == GLFW.GLFW_KEY_LEFT) {
                if (!isForceWatch() || pageRevealed(pageLayout())) {
                    changePage(page - 1);
                }
            } else if (!isForceWatch()) {
                finishPages();
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return pressedKeys.remove(keyCode) || super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public void removed() {
        pageEffect.close();
        pressedKeys.clear();
        super.removed();
    }

    private record PageLayout(int left, int top, int width, int height, int first, int count, int pages) {
    }
}

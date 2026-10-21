package xiaojin.gachaaddiction.client.gui.screen;

import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import xiaojin.gachaaddiction.GachaAddiction;
import xiaojin.gachaaddiction.api.ItemStackEntry;
import xiaojin.gachaaddiction.api.RewardData;
import xiaojin.gachaaddiction.client.compat.colortooltips.ColorTooltipsBorderRenderer;
import xiaojin.gachaaddiction.client.render.ModernGachaEffect;
import xiaojin.gachaaddiction.client.render.ModernGachaEntrance;
import xiaojin.gachaaddiction.registry.GachaTypes;
import xiaojin.gachaaddiction.registry.GachaaAdictionConfigData;
import xiaojin.gachaaddiction.util.TranslationKeys;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ModernGachaScreen extends BasicGachaScreen {
    private static final int SINGLE_RARITY_THRESHOLD = 0;
    private static final float HERO_FRACTION = 0.8F;
    private static final float GRID_FRACTION = 0.8F;
    private static final int GRID_GAP = 1;
    private static final float NATIVE_SLOT_SIZE = 18.0F;
    private static final int NATIVE_ITEM_HALF = 8;
    private static final int NATIVE_BORDER_HALF = 9;
    private static final int GRID_VERTICAL_MARGIN = 48;
    private static final int FOOTER_MARGIN = 20;
    private static final int OVERLAY_DEPTH = 1000;
    private static final int GRID_BORDER_DEPTH = 200;
    private static final int OPAQUE_ALPHA = 0xFF000000;
    private static final int TEXT_COLOR = 0xFFFFFF;
    private static final int SUMMARY_BACKGROUND = 0xCC080A10;
    private static final int FALLBACK_BACKGROUND = 0xFF08050D;
    private static final float FALLBACK_TINT_STRENGTH = 0.35F;
    private static final float LABEL_SCALE_DENOMINATOR = 144.0F;
    private static final float MINIMUM_LABEL_SCALE = 0.75F;
    private static final float MAXIMUM_LABEL_SCALE = 2.0F;
    private static final int MAXIMUM_NAME_LINES = 2;
    private static final long REVEAL_MILLIS = 350;
    private static final long SUMMARY_ITEM_INTERVAL_MILLIS = 100;
    private static final long FAST_SUMMARY_ITEM_INTERVAL_MILLIS = 25;
    private static final String COLOR_TOOLTIP_WARNING =
            "Cannot render ColorTooltips item borders; using rarity borders for this screen";
    private static final float MILLISECONDS_PER_SECOND = 1000.0F;
    private static final int DEFAULT_SUMMARY_COLOR = 0xD0D0D0;
    private static final float SUMMARY_ENTRY_HEIGHT_FRACTION = 0.08F;
    private final ModernGachaEffect effect = new ModernGachaEffect();
    private final ModernGachaEntrance entrance = new ModernGachaEntrance();
    private final Set<RewardData> completedRewards = new HashSet<>();
    private final Set<Integer> pressedKeys = new HashSet<>();
    private List<RewardData> singleRewards = List.of();
    private Stage stage = Stage.WAITING;
    private int currentItem;
    private int firstGridRow;
    private boolean receivedResults;
    private boolean skipReveals;
    private long revealStartedAt;
    private long effectStartedAt;
    private boolean instantReveal;
    private long summaryStartedAt;
    private long summaryIntervalMillis = SUMMARY_ITEM_INTERVAL_MILLIS;
    private int summaryRevealBase;
    private boolean colorTooltipsFailed;
    private boolean summaryPreviewPrepared;
    private int summaryColor = DEFAULT_SUMMARY_COLOR;

    public ModernGachaScreen(@Nullable Screen originalScreen, List<@Nullable ResourceLocation> lootTableId,
                             List<ItemStackEntry> itemStackEntries) {
        super(Component.translatable(GachaTypes.MODERN.getTranslationKey()), originalScreen, lootTableId, itemStackEntries);
    }

    @Override
    public void updateRewards(NonNullList<ItemStack> items) {
        updateRewards(items, false);
    }

    public void updateRewards(NonNullList<ItemStack> items, boolean summaryOnly) {
        // 本次展示固定使用第一次完整奖励快照，自动拿取后的容器重同步不重开揭晓。
        if (receivedResults) {
            return;
        }

        receivedResults = true;
        for (ItemStack item : items) {
            if (!item.isEmpty()) {
                rewardReceived(item, false);
            }
        }

        if (summaryOnly) {
            showSummary();
        } else {
            refreshWidgets();
        }
    }

    public boolean hasReceivedResults() {
        return receivedResults;
    }

    @Override
    protected void refreshWidgets() {
        singleRewards = rewards.stream().filter(reward -> reward.getRarityLevel() > SINGLE_RARITY_THRESHOLD).toList();
        currentItem = 0;
        if (singleRewards.isEmpty()) {
            showSummary();
            return;
        }

        showCurrentItem();
    }

    private void showCurrentItem() {
        effectStartedAt = 0;
        entrance.reset(currentItem > 0);
        instantReveal = skipReveals;
        stage = skipReveals ? Stage.REVEALED : Stage.SILHOUETTE;
        if (skipReveals) {
            entrance.finish();
            completeReward(singleRewards.get(currentItem), true);
        }
    }

    private void revealCurrent(boolean instant) {
        stage = Stage.REVEALED;
        revealStartedAt = Util.getMillis();
        instantReveal = instant;
        if (instant) {
            completeReward(singleRewards.get(currentItem), true);
        }
    }

    private void nextItem() {
        completeReward(singleRewards.get(currentItem), true);
        currentItem++;
        if (currentItem >= singleRewards.size()) {
            showSummary();
            return;
        }

        showCurrentItem();
    }

    private void showSummary() {
        stage = Stage.SUMMARY;
        entrance.reset(effectStartedAt != 0);
        for (RewardData reward : rewards) {
            completeReward(reward, false);
        }

        summaryColor = rewards.stream().max(Comparator.comparingInt(RewardData::getRarityLevel))
                .map(RewardData::getRarityColor).orElse(DEFAULT_SUMMARY_COLOR);
        if (effectStartedAt == 0) {
            effectStartedAt = Util.getMillis();
        }
        if (!summaryPreviewPrepared) {
            prepareSummaryReveal();
        }
        summaryPreviewPrepared = false;
    }

    protected void beginSummary() {
        showSummary();
    }

    private void prepareSummaryReveal() {
        firstGridRow = 0;
        summaryRevealBase = rewards.size();
        summaryIntervalMillis = SUMMARY_ITEM_INTERVAL_MILLIS;
        summaryStartedAt = Util.getMillis();
    }

    protected void resetSummaryPreview() {
        summaryPreviewPrepared = false;
    }

    protected void renderSummaryPreview(GuiGraphics graphics, float partialTick, float progress) {
        renderSummaryPreview(graphics, partialTick, progress, true);
    }

    protected void renderSummaryPreview(GuiGraphics graphics, float partialTick, float progress, boolean heading) {
        if (!summaryPreviewPrepared) {
            prepareSummaryReveal();
            summaryPreviewPrepared = true;
        }

        graphics.pose().pushPose();
        try {
            graphics.pose().translate(0.0F, (1.0F - progress) * height * SUMMARY_ENTRY_HEIGHT_FRACTION, 0.0F);
            renderSummary(graphics, -1, -1, partialTick, false, heading);
        } finally {
            graphics.pose().popPose();
        }
    }

    protected void completePresentedReward(RewardData reward, boolean playSound) {
        completeReward(reward, playSound);
    }

    private int summaryVisibleCount() {
        long elapsed = Math.max(0, Util.getMillis() - summaryStartedAt);
        return (int) Math.min(rewards.size(), summaryRevealBase + elapsed / summaryIntervalMillis);
    }

    private void accelerateSummary() {
        if (summaryIntervalMillis != FAST_SUMMARY_ITEM_INTERVAL_MILLIS) {
            summaryRevealBase = summaryVisibleCount();
            summaryStartedAt = Util.getMillis();
            summaryIntervalMillis = FAST_SUMMARY_ITEM_INTERVAL_MILLIS;
        }
    }

    private void completeReward(RewardData reward, boolean playSound) {
        if (!completedRewards.add(reward)) {
            return;
        }

        onRewardRevealed(reward);
        var soundFunction = GachaaAdictionConfigData.getSoundEventsFunction();
        if (playSound && clientConfig.rewardSoundEffects.get() && soundFunction != null) {
            getMinecraft().getSoundManager().play(soundFunction.apply(reward.getItemStack()));
        }
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (stage == Stage.WAITING) {
            graphics.fill(0, 0, width, height, SUMMARY_BACKGROUND);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        if (stage == Stage.WAITING) {
            renderSourceTitle(graphics);
            return;
        }

        if (stage == Stage.SUMMARY) {
            ModernGachaEntrance.Frame entry = entrance.sample(Util.getMillis());
            if (!effect.renderBackdrop(graphics, summaryColor,
                    (Util.getMillis() - effectStartedAt) / MILLISECONDS_PER_SECOND, entry)) {
                int tint = OPAQUE_ALPHA | (int) ((summaryColor >> 16 & 255) * FALLBACK_TINT_STRENGTH) << 16
                        | (int) ((summaryColor >> 8 & 255) * FALLBACK_TINT_STRENGTH) << 8
                        | (int) ((summaryColor & 255) * FALLBACK_TINT_STRENGTH);
                graphics.fillGradient(0, 0, width, height, tint, FALLBACK_BACKGROUND);
            }
            renderSummary(graphics, mouseX, mouseY, partialTick);
            return;
        }

        RewardData reward = singleRewards.get(currentItem);
        long now = Util.getMillis();
        if (effectStartedAt == 0) {
            effectStartedAt = now;
        }
        ModernGachaEntrance.Frame entry = entrance.sample(now);
        float reveal = stage == Stage.SILHOUETTE ? 0.0F
                : instantReveal ? 1.0F : Math.clamp((float) (now - revealStartedAt) / REVEAL_MILLIS, 0.0F, 1.0F);
        if (reveal >= 1.0F) {
            completeReward(reward, true);
        }

        int size = Math.max(1, (int) (Math.min(width, height) * HERO_FRACTION));
        int left = (width - size) / 2;
        int top = (height - size) / 2;
        float seconds = (now - effectStartedAt) / MILLISECONDS_PER_SECOND;
        if (!effect.render(graphics, reward, left, top, size, seconds, reveal, entry)) {
            renderFallback(graphics, reward, left, top, size, reveal, entry);
        }

        if (stage == Stage.REVEALED && entrance.isComplete()) {
            renderItemLabels(graphics, reward, left, top, size, mouseX, mouseY);
        }

        renderHint(graphics, stage == Stage.SILHOUETTE
                ? TranslationKeys.GUI_MODERN_REVEAL_HINT : TranslationKeys.GUI_MODERN_NEXT_HINT);
        renderSourceTitle(graphics);
    }

    private void renderFallback(GuiGraphics graphics, RewardData reward, int left, int top, int size, float reveal,
                                 ModernGachaEntrance.Frame entry) {
        int color = reward.getRarityColor();
        float strength = FALLBACK_TINT_STRENGTH * entry.backdrop();
        int tint = OPAQUE_ALPHA | (int) ((color >> 16 & 255) * strength) << 16
                | (int) ((color >> 8 & 255) * strength) << 8
                | (int) ((color & 255) * strength);
        graphics.fillGradient(0, 0, width, height, tint, FALLBACK_BACKGROUND);
        graphics.flush();
        graphics.enableScissor(left, top, left + size, top + size);
        graphics.pose().pushPose();
        try {
            graphics.pose().translate(left, top, 0);
            entry.apply(graphics, size / 2.0F, size / 2.0F);
            float scale = size / (float) (NATIVE_ITEM_HALF * 2);
            graphics.pose().scale(scale, scale, 1.0F);
            graphics.setColor(reveal, reveal, reveal, entry.opacity());
            graphics.renderFakeItem(reward.getItemStack(), 0, 0);
            graphics.flush();
        } finally {
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            graphics.pose().popPose();
            graphics.disableScissor();
        }
    }

    private void renderItemLabels(GuiGraphics graphics, RewardData reward, int left, int top, int size,
                                  int mouseX, int mouseY) {
        float scale = Math.clamp(size / LABEL_SCALE_DENOMINATOR, MINIMUM_LABEL_SCALE, MAXIMUM_LABEL_SCALE);
        int lineWidth = Math.max(1, (int) (size / scale));
        var nameLines = font.split(reward.getItemStack().getHoverName(), lineWidth);
        int shownLines = Math.min(MAXIMUM_NAME_LINES, nameLines.size());
        Component count = Component.translatable(TranslationKeys.GUI_MODERN_COUNT, reward.getCount());
        int countWidth = font.width(count);
        graphics.pose().pushPose();
        try {
            graphics.pose().translate(left, top, OVERLAY_DEPTH);
            graphics.pose().scale(scale, scale, 1.0F);
            for (int line = 0; line < shownLines; line++) {
                graphics.drawString(font, nameLines.get(line), 0, line * font.lineHeight,
                        reward.getRarityColor() | OPAQUE_ALPHA, true);
            }

            graphics.drawString(font, count, (int) (size / scale) - countWidth,
                    (int) (size / scale) - font.lineHeight, TEXT_COLOR, true);
        } finally {
            graphics.pose().popPose();
        }

        boolean overName = mouseX >= left && mouseX < left + size
                && mouseY >= top && mouseY < top + shownLines * font.lineHeight * scale;
        boolean overCount = mouseX >= left + size - countWidth * scale && mouseX < left + size
                && mouseY >= top + size - font.lineHeight * scale && mouseY < top + size;
        if (overName || overCount) {
            graphics.renderTooltip(font, reward.getCountItemStack(), mouseX, mouseY);
        }
    }

    private GridLayout gridLayout() {
        int regionWidth = Math.max(1, (int) (width * GRID_FRACTION));
        int pitch = (int) NATIVE_SLOT_SIZE + GRID_GAP;
        int columns = Math.max(1, (regionWidth + GRID_GAP) / pitch);
        int totalRows = (rewards.size() + columns - 1) / columns;
        int rowCapacity = Math.max(1, (height - GRID_VERTICAL_MARGIN * 2 + GRID_GAP) / pitch);
        int visibleRows = Math.max(1, Math.min(totalRows, rowCapacity));
        int revealedRows = (summaryVisibleCount() + columns - 1) / columns;
        firstGridRow = Math.clamp(firstGridRow, 0, Math.max(0, revealedRows - visibleRows));
        int usedColumns = Math.max(1, Math.min(columns, rewards.size()));
        return new GridLayout((width - usedColumns * pitch + GRID_GAP) / 2,
                (height - visibleRows * pitch + GRID_GAP) / 2,
                columns, pitch, visibleRows, totalRows);
    }

    private void renderSummary(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderSummary(graphics, mouseX, mouseY, partialTick, true);
    }

    protected void renderSummaryItems(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderSummary(graphics, mouseX, mouseY, partialTick, true, false);
    }

    private void renderSummary(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, boolean completed) {
        renderSummary(graphics, mouseX, mouseY, partialTick, completed, true);
    }

    private void renderSummary(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, boolean completed, boolean heading) {
        ModernGachaEntrance.Frame entry = entrance.sample(Util.getMillis());
        if (completed && summaryVisibleCount() >= rewards.size() && entrance.isComplete()) {
            completePresentation();
        }
        GridLayout grid = gridLayout();
        if (heading) {
            renderSourceTitle(graphics);
        }
        ItemStack hovered = ItemStack.EMPTY;
        int first = firstGridRow * grid.columns;
        int last = Math.min(summaryVisibleCount(), first + grid.visibleRows * grid.columns);
        float slotSize = NATIVE_SLOT_SIZE;
        float borderFlow = 0.0F;
        if (GachaAddiction.COLORTOOLTIPS_LOADED && !colorTooltipsFailed) {
            try {
                borderFlow = ColorTooltipsBorderRenderer.updateFlow(partialTick);
            } catch (RuntimeException | LinkageError failure) {
                disableColorTooltips(failure);
            }
        }

        for (int index = first; index < last; index++) {
            RewardData reward = rewards.get(index);
            int localIndex = index - first;
            int row = localIndex / grid.columns;
            int centerX = grid.left + localIndex % grid.columns * grid.pitch + NATIVE_BORDER_HALF;
            int centerY = grid.top + row * grid.pitch + NATIVE_BORDER_HALF;
            graphics.enableScissor(centerX - NATIVE_BORDER_HALF, centerY - NATIVE_BORDER_HALF,
                    centerX + NATIVE_BORDER_HALF, centerY + NATIVE_BORDER_HALF);
            graphics.pose().pushPose();
            try {
                graphics.pose().translate(centerX, centerY, 0);
                graphics.pose().pushPose();
                try {
                    entry.apply(graphics, 0.0F, 0.0F);
                    graphics.setColor(1.0F, 1.0F, 1.0F, entry.opacity());
                    graphics.renderFakeItem(reward.getCountItemStack(), -NATIVE_ITEM_HALF, -NATIVE_ITEM_HALF);
                    graphics.renderItemDecorations(font, reward.getCountItemStack(), -NATIVE_ITEM_HALF, -NATIVE_ITEM_HALF);
                    graphics.flush();
                } finally {
                    graphics.pose().popPose();
                }
                graphics.setColor(1.0F, 1.0F, 1.0F, entry.opacity() * entry.rim());
                graphics.pose().translate(0, 0, GRID_BORDER_DEPTH);
                if (!renderColorTooltipBorder(graphics, reward.getCountItemStack(), borderFlow)) {
                    int color = reward.getRarityColor() | OPAQUE_ALPHA;
                    graphics.hLine(-NATIVE_BORDER_HALF, NATIVE_BORDER_HALF - 1, -NATIVE_BORDER_HALF, color);
                    graphics.hLine(-NATIVE_BORDER_HALF, NATIVE_BORDER_HALF - 1, NATIVE_BORDER_HALF - 1, color);
                    graphics.vLine(-NATIVE_BORDER_HALF, -NATIVE_BORDER_HALF, NATIVE_BORDER_HALF - 1, color);
                    graphics.vLine(NATIVE_BORDER_HALF - 1, -NATIVE_BORDER_HALF, NATIVE_BORDER_HALF - 1, color);
                }
                graphics.flush();
            } finally {
                graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                graphics.pose().popPose();
                graphics.disableScissor();
            }

            if (entrance.isComplete() && mouseX >= centerX - slotSize / 2 && mouseX < centerX + slotSize / 2
                    && mouseY >= centerY - slotSize / 2 && mouseY < centerY + slotSize / 2) {
                hovered = reward.getCountItemStack();
            }
        }

        if (!completed) {
            return;
        }
        if (summaryVisibleCount() < rewards.size()) {
            renderHint(graphics, TranslationKeys.GUI_MODERN_SUMMARY_REVEAL_HINT);
        } else {
            renderHint(graphics, grid.totalRows > grid.visibleRows
                    ? TranslationKeys.GUI_MODERN_SUMMARY_SCROLL_HINT : TranslationKeys.GUI_MODERN_SUMMARY_HINT);
        }
        if (!hovered.isEmpty()) {
            renderSummaryTooltip(graphics, hovered, mouseX, mouseY);
        }
    }

    protected void renderSummaryTooltip(GuiGraphics graphics, ItemStack item, int mouseX, int mouseY) {
        graphics.flush();
        graphics.pose().pushPose();
        try {
            graphics.pose().translate(0, 0, OVERLAY_DEPTH);
            graphics.renderTooltip(font, item, mouseX, mouseY);
        } finally {
            graphics.pose().popPose();
        }
    }

    private void renderHint(GuiGraphics graphics, String key) {
        if (isForceWatch()) {
            key = TranslationKeys.GUI_ANIMATION_FORCED;
        }
        graphics.drawCenteredString(font, Component.translatable(key), width / 2, height - FOOTER_MARGIN, TEXT_COLOR);
    }

    private boolean renderColorTooltipBorder(GuiGraphics graphics, ItemStack stack, float flow) {
        if (!GachaAddiction.COLORTOOLTIPS_LOADED || colorTooltipsFailed) {
            return false;
        }

        try {
            return ColorTooltipsBorderRenderer.render(graphics, stack,
                    -NATIVE_BORDER_HALF, -NATIVE_BORDER_HALF, (int) NATIVE_SLOT_SIZE, flow);
        } catch (RuntimeException | LinkageError failure) {
            disableColorTooltips(failure);
            return false;
        }
    }

    private void disableColorTooltips(Throwable failure) {
        colorTooltipsFailed = true;
        GachaAddiction.LOGGER.warn(COLOR_TOOLTIP_WARNING, failure);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT || stage == Stage.WAITING) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        if (!entrance.isComplete()) {
            return true;
        }

        switch (stage) {
            case SILHOUETTE -> revealCurrent(false);
            case REVEALED -> {
                if (!isForceWatch() || Util.getMillis() - revealStartedAt >= REVEAL_MILLIS) {
                    nextItem();
                }
            }
            case SUMMARY -> {
                if (summaryVisibleCount() < rewards.size()) {
                    if (!isForceWatch()) {
                        accelerateSummary();
                    }
                } else {
                    onClose();
                }
            }
        }
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (isForceWatch() && (keyCode == GLFW.GLFW_KEY_SPACE || keyCode == GLFW.GLFW_KEY_LEFT_SHIFT
                || keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT)) {
            return true;
        }
        if (keyCode != GLFW.GLFW_KEY_SPACE && keyCode != GLFW.GLFW_KEY_LEFT_SHIFT
                && keyCode != GLFW.GLFW_KEY_RIGHT_SHIFT) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        if (!pressedKeys.add(keyCode) || stage == Stage.WAITING) {
            return true;
        }

        if (stage == Stage.SUMMARY) {
            if (keyCode == GLFW.GLFW_KEY_SPACE) {
                summaryRevealBase = rewards.size();
                entrance.finish();
            }
            return true;
        }

        entrance.finish();
        if (keyCode == GLFW.GLFW_KEY_LEFT_SHIFT || keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            showSummary();
            return true;
        }

        skipReveals = true;
        if (stage == Stage.SILHOUETTE) {
            revealCurrent(true);
        } else if (!instantReveal && Util.getMillis() - revealStartedAt < REVEAL_MILLIS) {
            instantReveal = true;
            completeReward(singleRewards.get(currentItem), true);
        } else {
            nextItem();
        }
        return true;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (pressedKeys.remove(keyCode)) {
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (stage != Stage.SUMMARY || scrollY == 0.0) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }

        GridLayout grid = gridLayout();
        int revealedRows = (summaryVisibleCount() + grid.columns - 1) / grid.columns;
        int nextRow = Math.clamp(firstGridRow - (int) Math.signum(scrollY), 0,
                Math.max(0, revealedRows - grid.visibleRows));
        if (nextRow != firstGridRow && entrance.isComplete()) {
            firstGridRow = nextRow;
            entrance.reset(true);
        }
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void removed() {
        effect.close();
        pressedKeys.clear();
        super.removed();
    }

    private enum Stage {
        WAITING, SILHOUETTE, REVEALED, SUMMARY
    }

    private record GridLayout(int left, int top, int columns, int pitch, int visibleRows, int totalRows) {
    }
}

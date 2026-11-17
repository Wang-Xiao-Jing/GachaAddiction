package xiaojin.gachaaddiction.client.gui.screen;

import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import xiaojin.gachaaddiction.GachaConstants;
import xiaojin.gachaaddiction.GachaModPresence;
import xiaojin.gachaaddiction.api.RewardData;
import xiaojin.gachaaddiction.client.render.ModernGachaEffect;
import xiaojin.gachaaddiction.client.render.ModernGachaEntrance;
import xiaojin.gachaaddiction.registry.client.GachaClientIntegrations;
import xiaojin.gachaaddiction.util.TranslationKeys;

import java.util.Comparator;
import java.util.List;

import static xiaojin.gachaaddiction.client.gui.screen.GachaSummaryScreen.FOOTER_MARGIN;
import static xiaojin.gachaaddiction.client.gui.screen.GachaSummaryScreen.OPAQUE_ALPHA;
import static xiaojin.gachaaddiction.client.gui.screen.GachaSummaryScreen.TEXT_COLOR;
import static xiaojin.gachaaddiction.client.gui.screen.GachaSummaryScreen.FALLBACK_BACKGROUND;
import static xiaojin.gachaaddiction.client.gui.screen.GachaSummaryScreen.FALLBACK_TINT_STRENGTH;

public class GachaSummaryView implements AutoCloseable {
    private static final float GRID_FRACTION = 0.8F;
    private static final int GRID_GAP = 1;
    private static final float NATIVE_SLOT_SIZE = 18.0F;
    private static final int NATIVE_ITEM_HALF = 8;
    private static final int NATIVE_BORDER_HALF = 9;
    private static final int GRID_VERTICAL_MARGIN = 48;
    private static final int GRID_BORDER_DEPTH = 200;
    private static final float MILLISECONDS_PER_SECOND = 1000.0F;
    private static final int DEFAULT_SUMMARY_COLOR = 0xD0D0D0;
    private static final float SUMMARY_ENTRY_HEIGHT_FRACTION = 0.08F;
    private static final String COLOR_TOOLTIP_WARNING =
            "Cannot render ColorTooltips item borders; using rarity borders for this screen";
    private final GachaSummaryScreen screen;
    private ModernGachaEntrance entrance = new ModernGachaEntrance();
    private @Nullable ModernGachaEffect effect;
    private boolean borrowedEffect;
    private int firstGridRow;
    private boolean previewPrepared;
    private boolean colorTooltipsFailed;
    private int summaryColor = DEFAULT_SUMMARY_COLOR;
    private long effectStartedAt;

    public GachaSummaryView(GachaSummaryScreen screen) {
        this.screen = screen;
    }

    public void begin() {
        begin(null, null, effectStartedAt);
    }

    public long begin(@Nullable ModernGachaEffect sharedEffect, @Nullable ModernGachaEntrance sharedEntrance,
                      long startedAt) {
        if (sharedEntrance != null) {
            entrance = sharedEntrance;
        }
        entrance.reset(startedAt != 0);
        for (RewardData reward : screen.rewards) {
            screen.completePresentedReward(reward, false);
        }
        summaryColor = screen.rewards.stream().max(Comparator.comparingInt(RewardData::getRarityLevel))
                .map(RewardData::getRarityColor).orElse(DEFAULT_SUMMARY_COLOR);
        effectStartedAt = startedAt == 0 ? Util.getMillis() : startedAt;
        if (sharedEffect != null && effect != sharedEffect) {
            if (effect != null && !borrowedEffect) {
                effect.close();
            }
            effect = sharedEffect;
            borrowedEffect = true;
        }
        if (!previewPrepared) {
            firstGridRow = 0;
        }
        previewPrepared = false;
        return effectStartedAt;
    }

    public void resetPreview() {
        previewPrepared = false;
    }

    public void renderPreview(GuiGraphics graphics, float partialTick, float progress, boolean heading) {
        if (!previewPrepared) {
            firstGridRow = 0;
            previewPrepared = true;
        }
        graphics.pose().pushPose();
        try {
            graphics.pose().translate(0.0F, (1.0F - progress) * screen.height * SUMMARY_ENTRY_HEIGHT_FRACTION, 0.0F);
            renderItems(graphics, -1, -1, partialTick, false, heading);
        } finally {
            graphics.pose().popPose();
        }
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ModernGachaEntrance.Frame entry = entrance.sample(Util.getMillis());
        if (effect == null || !effect.renderBackdrop(graphics, summaryColor,
                (Util.getMillis() - effectStartedAt) / MILLISECONDS_PER_SECOND, entry)) {
            int tint = OPAQUE_ALPHA | (int) ((summaryColor >> 16 & 255) * FALLBACK_TINT_STRENGTH) << 16
                    | (int) ((summaryColor >> 8 & 255) * FALLBACK_TINT_STRENGTH) << 8
                    | (int) ((summaryColor & 255) * FALLBACK_TINT_STRENGTH);
            graphics.fillGradient(0, 0, screen.width, screen.height, tint, FALLBACK_BACKGROUND);
        }
        renderItems(graphics, mouseX, mouseY, partialTick, true, true);
    }

    private GridLayout gridLayout() {
        int width = screen.width;
        int height = screen.height;
        List<RewardData> rewards = screen.rewards;
        int regionWidth = Math.max(1, (int) (width * GRID_FRACTION));
        int pitch = (int) NATIVE_SLOT_SIZE + GRID_GAP;
        int columns = Math.max(1, (regionWidth + GRID_GAP) / pitch);
        int totalRows = (rewards.size() + columns - 1) / columns;
        int rowCapacity = Math.max(1, (height - GRID_VERTICAL_MARGIN * 2 + GRID_GAP) / pitch);
        int visibleRows = Math.max(1, Math.min(totalRows, rowCapacity));
        int revealedRows = (rewards.size() + columns - 1) / columns;
        firstGridRow = Math.clamp(firstGridRow, 0, Math.max(0, revealedRows - visibleRows));
        int usedColumns = Math.max(1, Math.min(columns, rewards.size()));
        return new GridLayout((width - usedColumns * pitch + GRID_GAP) / 2,
                (height - visibleRows * pitch + GRID_GAP) / 2,
                columns, pitch, visibleRows, totalRows);
    }

    public void renderItems(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, boolean completed, boolean heading) {
        List<RewardData> rewards = screen.rewards;
        var font = screen.summaryFont();
        ModernGachaEntrance.Frame entry = entrance.sample(Util.getMillis());
        if (completed && entrance.isComplete()) {
            screen.completePresentation();
        }
        GridLayout grid = gridLayout();
        if (heading) {
            screen.renderSourceTitle(graphics);
        }
        ItemStack hovered = ItemStack.EMPTY;
        int first = firstGridRow * grid.columns;
        int last = Math.min(rewards.size(), first + grid.visibleRows * grid.columns);
        float slotSize = NATIVE_SLOT_SIZE;
        float borderFlow = 0.0F;
        if (GachaModPresence.COLORTOOLTIPS_LOADED && !colorTooltipsFailed) {
            try {
                borderFlow = GachaClientIntegrations.itemBorders().updateFlow(partialTick);
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
            enableItemScissor(graphics, centerX, centerY);
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
        renderHint(graphics, grid.totalRows > grid.visibleRows
                ? TranslationKeys.GUI_MODERN_SUMMARY_SCROLL_HINT : TranslationKeys.GUI_MODERN_SUMMARY_HINT);
        if (!hovered.isEmpty()) {
            screen.renderSummaryTooltip(graphics, hovered, mouseX, mouseY);
        }
    }

    private void enableItemScissor(GuiGraphics graphics, int centerX, int centerY) {
        var pose = graphics.pose().last().pose();
        Vector3f minimum = pose.transformPosition(new Vector3f(centerX - NATIVE_BORDER_HALF, centerY - NATIVE_BORDER_HALF, 0.0F));
        Vector3f maximum = pose.transformPosition(new Vector3f(centerX + NATIVE_BORDER_HALF, centerY + NATIVE_BORDER_HALF, 0.0F));
        graphics.enableScissor((int) Math.floor(Math.min(minimum.x, maximum.x)), (int) Math.floor(Math.min(minimum.y, maximum.y)),
                (int) Math.ceil(Math.max(minimum.x, maximum.x)), (int) Math.ceil(Math.max(minimum.y, maximum.y)));
    }

    private void renderHint(GuiGraphics graphics, String key) {
        if (screen.isForceWatch()) {
            key = TranslationKeys.GUI_ANIMATION_FORCED;
        }
        graphics.drawCenteredString(screen.summaryFont(), Component.translatable(key), screen.width / 2, screen.height - FOOTER_MARGIN, TEXT_COLOR);
    }

    private boolean renderColorTooltipBorder(GuiGraphics graphics, ItemStack stack, float flow) {
        if (!GachaModPresence.COLORTOOLTIPS_LOADED || colorTooltipsFailed) {
            return false;
        }

        try {
            return GachaClientIntegrations.itemBorders().render(graphics, stack,
                    -NATIVE_BORDER_HALF, -NATIVE_BORDER_HALF, (int) NATIVE_SLOT_SIZE, flow);
        } catch (RuntimeException | LinkageError failure) {
            disableColorTooltips(failure);
            return false;
        }
    }

    private void disableColorTooltips(Throwable failure) {
        colorTooltipsFailed = true;
        GachaConstants.LOGGER.warn(COLOR_TOOLTIP_WARNING, failure);
    }

    public boolean isEntryComplete() {
        return entrance.isComplete();
    }

    public void finishEntry() {
        entrance.finish();
    }

    public boolean scroll(double scrollY) {
        GridLayout grid = gridLayout();
        int nextRow = Math.clamp(firstGridRow - (int) Math.signum(scrollY), 0,
                Math.max(0, grid.totalRows - grid.visibleRows));
        if (nextRow != firstGridRow && entrance.isComplete()) {
            firstGridRow = nextRow;
            entrance.reset(true);
        }
        return true;
    }

    @Override
    public void close() {
        if (effect != null && !borrowedEffect) {
            effect.close();
            effect = null;
        }
    }

    private record GridLayout(int left, int top, int columns, int pitch, int visibleRows, int totalRows) {
    }
}

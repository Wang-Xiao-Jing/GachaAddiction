package xiaojin.gachaaddiction.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import xiaojin.gachaaddiction.GachaAddiction;
import xiaojin.gachaaddiction.api.ItemStackEntry;
import xiaojin.gachaaddiction.api.RewardData;
import xiaojin.gachaaddiction.client.compat.colortooltips.ColorTooltipsBorderRenderer;
import xiaojin.gachaaddiction.config.ClientConfig;

import java.util.*;

public class LoopingSlotMachineRenderer {
    private static final float WIDTH_FRACTION = 2.0F / 3.0F;
    private static final float HEIGHT_FRACTION = 0.34F;
    private static final int REEL_PITCH = 32;
    private static final int ITEM_HALF = 8;
    private static final int BORDER_HALF = 9;
    private static final int BORDER_SIZE = 18;
    private static final int BORDER_DEPTH = 500;
    private static final int NEUTRAL_BORDER = 0xFF9DA6BE;
    private static final int WINDOW_PADDING = 2;
    private static final int MINIMUM_STOP_STEPS = 4;
    private static final int MAXIMUM_STOP_STEPS = 24;
    private static final long MINIMUM_STOP_MILLIS = 850;
    private static final long MAXIMUM_STOP_MILLIS = 3200;
    private static final float STOP_DURATION_FACTOR = 1.5F;
    private static final float MILLISECONDS_PER_SECOND = 1000.0F;
    private static final float MAXIMUM_FRAME_SECONDS = 0.1F;
    private static final float MINIMUM_SPEED = 0.1F;
    private static final String BORDER_WARNING = "Cannot render ColorTooltips borders in looping slot machine";
    private static final List<Item> FALLBACK_ITEMS = List.of(Items.IRON_INGOT, Items.GOLD_INGOT, Items.DIAMOND,
            Items.EMERALD, Items.ENDER_PEARL, Items.APPLE);
    private final List<Reel> reels = new ArrayList<>();
    private ClientConfig.SlotMachineConfig config;
    private long previousFrame;
    private boolean stopping;
    private boolean passed;
    private boolean colorTooltipsFailed;

    public void reset(List<RewardData> rewards, List<ItemStackEntry> entries,
                      ClientConfig.SlotMachineConfig config, int width, long now) {
        this.config = config;
        previousFrame = now;
        stopping = false;
        reels.clear();
        List<ItemStack> candidates = candidates(entries);
        int columns = Math.max(1, (int) (width * WIDTH_FRACTION) / REEL_PITCH);
        int count = Math.min(columns, rewards.size());
        for (int index = 0; index < count; index++) {
            reels.add(new Reel(rewards.get(index), candidates, config));
        }
    }

    private List<ItemStack> candidates(List<ItemStackEntry> entries) {
        Set<Item> seen = new HashSet<>();
        List<ItemStack> result = new ArrayList<>();
        for (ItemStackEntry entry : entries) {
            if (entry.weight() > 0 && !entry.stack().isEmpty() && seen.add(entry.stack().getItem())) {
                result.add(entry.stack().copyWithCount(1));
            }
        }
        if (result.isEmpty()) {
            FALLBACK_ITEMS.forEach(item -> result.add(item.getDefaultInstance()));
        }
        return List.copyOf(result);
    }

    public void stop(long now) {
        if (stopping) {
            return;
        }
        stopping = true;
        for (int index = 0; index < reels.size(); index++) {
            reels.get(index).stop(index, now, config);
        }
    }

    public void finishNow() {
        for (Reel reel : reels) {
            reel.finishNow();
        }
    }

    public boolean isStopping() {
        return stopping;
    }

    public boolean isComplete() {
        return stopping && reels.stream().allMatch(reel -> reel.complete);
    }

    public List<RewardData> update(long now) {
        float elapsed = Math.clamp((now - previousFrame) / MILLISECONDS_PER_SECOND, 0.0F, MAXIMUM_FRAME_SECONDS);
        previousFrame = now;
        passed = false;
        List<RewardData> completed = new ArrayList<>();
        for (Reel reel : reels) {
            passed |= reel.update(now, elapsed);
            if (reel.complete && !reel.notified) {
                reel.notified = true;
                completed.add(reel.result);
            }
        }
        return completed;
    }

    public boolean passedAny() {
        return passed;
    }

    public ItemStack render(GuiGraphics graphics, int width, int height, int mouseX, int mouseY, float partialTick) {
        if (reels.isEmpty()) {
            return ItemStack.EMPTY;
        }
        int visibleReels = Math.min(reels.size(), Math.max(1, (int) (width * WIDTH_FRACTION) / REEL_PITCH));
        int pitch = REEL_PITCH;
        int left = (width - visibleReels * pitch) / 2;
        int centerY = height / 2;
        float spacing = config.itemSpacing.get().floatValue();
        int halfHeight = Math.max(BORDER_HALF, (int) Math.min(height * HEIGHT_FRACTION,
                spacing * config.itemVisibleCount.get() / 2.0F));
        ItemStack hovered = ItemStack.EMPTY;
        float flow = 0.0F;
        if (GachaAddiction.COLORTOOLTIPS_LOADED && !colorTooltipsFailed) {
            try {
                flow = ColorTooltipsBorderRenderer.updateFlow(partialTick);
            } catch (RuntimeException | LinkageError failure) {
                disableColorTooltips(failure);
            }
        }
        for (int column = 0; column < visibleReels; column++) {
            Reel reel = reels.get(column);
            int centerX = left + column * pitch + pitch / 2;
            graphics.enableScissor(centerX - pitch / 2, centerY - halfHeight,
                    centerX + pitch / 2, centerY + halfHeight);
            try {
                for (int row = 0; row < reel.window.size(); row++) {
                    float offset = (row - reel.centerIndex - reel.fraction) * spacing;
                    float distance = Math.abs(offset) / halfHeight;
                    if (distance > 1.0F) {
                        continue;
                    }
                    float maximumAlpha = config.itemMaxAlpha.get().floatValue();
                    float alpha = maximumAlpha
                            - (maximumAlpha - config.itemMinAlpha.get().floatValue()) * distance;
                    ItemStack item = reel.complete && row == reel.centerIndex
                            ? reel.result.getCountItemStack() : reel.window.get(row);
                    GuiItemRenderer.renderFakeItem(graphics, item, centerX - ITEM_HALF,
                            centerY + Math.round(offset) - ITEM_HALF, alpha);
                    if (reel.complete && row == reel.centerIndex) {
                        graphics.renderItemDecorations(Minecraft.getInstance().font, item,
                                centerX - ITEM_HALF, centerY - ITEM_HALF);
                    }
                }
            } finally {
                graphics.disableScissor();
            }
            graphics.pose().pushPose();
            try {
                graphics.pose().translate(0, 0, BORDER_DEPTH);
                ItemStack result = reel.complete ? reel.result.getCountItemStack() : ItemStack.EMPTY;
                if (!border(graphics, result, centerX - BORDER_HALF, centerY - BORDER_HALF, flow)) {
                    int color = reel.complete ? reel.result.getRarityColor() | 0xFF000000 : NEUTRAL_BORDER;
                    graphics.hLine(centerX - BORDER_HALF, centerX + BORDER_HALF - 1, centerY - BORDER_HALF, color);
                    graphics.hLine(centerX - BORDER_HALF, centerX + BORDER_HALF - 1, centerY + BORDER_HALF - 1, color);
                    graphics.vLine(centerX - BORDER_HALF, centerY - BORDER_HALF, centerY + BORDER_HALF - 1, color);
                    graphics.vLine(centerX + BORDER_HALF - 1, centerY - BORDER_HALF, centerY + BORDER_HALF - 1, color);
                }
            } finally {
                graphics.pose().popPose();
            }
            if (reel.complete && mouseX >= centerX - BORDER_HALF && mouseX < centerX + BORDER_HALF
                    && mouseY >= centerY - BORDER_HALF && mouseY < centerY + BORDER_HALF) {
                hovered = reel.result.getCountItemStack();
            }
        }
        return hovered;
    }

    private boolean border(GuiGraphics graphics, ItemStack item, int x, int y, float flow) {
        if (item.isEmpty() || !GachaAddiction.COLORTOOLTIPS_LOADED || colorTooltipsFailed) {
            return false;
        }
        try {
            return ColorTooltipsBorderRenderer.render(graphics, item, x, y, BORDER_SIZE, flow);
        } catch (RuntimeException | LinkageError failure) {
            disableColorTooltips(failure);
            return false;
        }
    }

    private void disableColorTooltips(Throwable failure) {
        colorTooltipsFailed = true;
        GachaAddiction.LOGGER.warn(BORDER_WARNING, failure);
    }

    private static class Reel {
        private final RewardData result;
        private final List<ItemStack> bag;
        private final List<ItemStack> window = new ArrayList<>();
        private final RandomSource random = RandomSource.create();
        private final int centerIndex;
        private int bagIndex;
        private Item previousItem;
        private final float speed;
        private float fraction;
        private boolean stopping;
        private boolean complete;
        private boolean notified;
        private long stopStarted;
        private long stopDuration;
        private int stopSteps;
        private int advancedSteps;
        private int injectAt;
        private float stopStartFraction;
        private float previousTravel;

        private Reel(RewardData result, List<ItemStack> candidates, ClientConfig.SlotMachineConfig config) {
            this.result = result;
            bag = new ArrayList<>(candidates);
            bagIndex = bag.size();
            int windowSize = config.itemVisibleCount.get() + WINDOW_PADDING;
            centerIndex = windowSize / 2;
            for (int index = 0; index < windowSize; index++) {
                window.add(next());
            }
            float minimum = config.speedVarianceMin.get().floatValue();
            float multiplier = minimum + random.nextFloat() * (config.speedVarianceMax.get().floatValue() - minimum);
            speed = Math.max(MINIMUM_SPEED, config.itemsPerSecond.get().floatValue() * multiplier);
        }

        private ItemStack next() {
            if (bagIndex >= bag.size()) {
                for (int index = bag.size() - 1; index > 0; index--) {
                    Collections.swap(bag, index, random.nextInt(index + 1));
                }
                if (bag.size() > 1 && bag.getFirst().getItem() == previousItem) {
                    Collections.swap(bag, 0, 1);
                }
                bagIndex = 0;
                if (bag.size() == 1 && previousItem != null) {
                    previousItem = null;
                    return ItemStack.EMPTY;
                }
            }
            ItemStack item = bag.get(bagIndex++);
            previousItem = item.getItem();
            return item;
        }

        private void stop(int column, long now, ClientConfig.SlotMachineConfig config) {
            stopping = true;
            stopStarted = now;
            stopStartFraction = fraction;
            stopSteps = window.size() + Math.clamp((int) Math.ceil(config.decelZone.get()),
                    MINIMUM_STOP_STEPS, MAXIMUM_STOP_STEPS) + column;
            injectAt = centerIndex + stopSteps - window.size() + 1;
            stopDuration = (long) Math.clamp((stopSteps - fraction) / speed
                    * MILLISECONDS_PER_SECOND * STOP_DURATION_FACTOR, MINIMUM_STOP_MILLIS, MAXIMUM_STOP_MILLIS);
        }

        private boolean update(long now, float elapsed) {
            if (complete) {
                return false;
            }
            float distance;
            float progress = 0.0F;
            if (stopping) {
                progress = Math.clamp((float) (now - stopStarted) / stopDuration, 0.0F, 1.0F);
                float travel = (stopSteps - stopStartFraction)
                        * (1.0F - (1.0F - progress) * (1.0F - progress) * (1.0F - progress));
                distance = travel - previousTravel;
                previousTravel = travel;
            } else {
                distance = elapsed * speed;
            }
            boolean passed = advance(distance);
            if (stopping && progress >= 1.0F) {
                finishNow();
            }
            return passed;
        }

        private boolean advance(float distance) {
            fraction += Math.max(0.0F, distance);
            boolean passed = false;
            while (fraction >= 1.0F) {
                fraction -= 1.0F;
                advanceItem();
                passed = true;
            }
            return passed;
        }

        private void advanceItem() {
            window.removeFirst();
            if (stopping && ++advancedSteps == injectAt) {
                window.add(result.getItemStack().copy());
            } else {
                window.add(next());
            }
        }

        private void finishNow() {
            if (!stopping || complete) {
                return;
            }
            while (advancedSteps < stopSteps) {
                advanceItem();
            }
            fraction = 0.0F;
            complete = true;
        }
    }
}

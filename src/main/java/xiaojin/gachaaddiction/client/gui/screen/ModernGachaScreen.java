package xiaojin.gachaaddiction.client.gui.screen;

import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import xiaojin.gachaaddiction.api.GachaRandomType;
import xiaojin.gachaaddiction.api.ItemStackEntry;
import xiaojin.gachaaddiction.api.RewardData;
import xiaojin.gachaaddiction.client.render.ModernGachaEffect;
import xiaojin.gachaaddiction.client.render.ModernGachaEntrance;
import xiaojin.gachaaddiction.client.render.ModernGachaSilhouette;
import xiaojin.gachaaddiction.registry.GachaTypes;
import xiaojin.gachaaddiction.util.TranslationKeys;

import java.util.List;

public class ModernGachaScreen extends GachaSummaryScreen {
    private static final float HERO_FRACTION = 0.8F;
    private static final int NATIVE_ITEM_HALF = 8;
    private static final float LABEL_SCALE_DENOMINATOR = 144.0F;
    private static final float MINIMUM_LABEL_SCALE = 0.75F;
    private static final float MAXIMUM_LABEL_SCALE = 2.0F;
    private static final int MAXIMUM_NAME_LINES = 2;
    private static final long REVEAL_MILLIS = 350;
    private static final float MILLISECONDS_PER_SECOND = 1000.0F;
    private final ModernGachaEffect effect = new ModernGachaEffect();
    private final ModernGachaEntrance entrance = new ModernGachaEntrance();
    private final ModernGachaSilhouette silhouette = new ModernGachaSilhouette();
    private List<RewardData> singleRewards = List.of();
    private Stage stage = Stage.WAITING;
    private int currentItem;
    private boolean skipReveals;
    private long revealStartedAt;
    private long effectStartedAt;
    private boolean instantReveal;

    public ModernGachaScreen(@Nullable Screen originalScreen, List<@Nullable ResourceLocation> lootTableId,
                             List<ItemStackEntry> itemStackEntries) {
        super(Component.translatable(GachaTypes.MODERN.getTranslationKey()), originalScreen, lootTableId, itemStackEntries);
    }

    @Override
    protected void refreshWidgets() {
        singleRewards = rewards.stream().filter(reward -> reward.getRarityLevel() > SINGLE_RARITY_THRESHOLD).toList();
        currentItem = 0;
        if (singleRewards.isEmpty()) {
            beginSummary();
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
            completePresentedReward(singleRewards.get(currentItem), true);
        }
    }

    private void revealCurrent(boolean instant) {
        stage = Stage.REVEALED;
        revealStartedAt = Util.getMillis();
        instantReveal = instant;
        if (instant) {
            completePresentedReward(singleRewards.get(currentItem), true);
        }
    }

    private void nextItem() {
        completePresentedReward(singleRewards.get(currentItem), true);
        currentItem++;
        if (currentItem >= singleRewards.size()) {
            beginSummary();
            return;
        }

        showCurrentItem();
    }

    @Override
    protected void beginSummary() {
        effectStartedAt = beginSummary(effect, entrance, effectStartedAt);
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (!isShowingSummary() && stage == Stage.WAITING) {
            graphics.fill(0, 0, width, height, SUMMARY_BACKGROUND);
        }
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (isShowingSummary()) {
            renderSummaryScreen(graphics, mouseX, mouseY, partialTick);
            return;
        }
        super.render(graphics, mouseX, mouseY, partialTick);
        if (stage == Stage.WAITING) {
            renderSourceTitle(graphics);
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
            completePresentedReward(reward, true);
        }

        int size = Math.max(1, (int) (Math.min(width, height) * HERO_FRACTION));
        int left = (width - size) / 2;
        int top = (height - size) / 2;
        float seconds = (now - effectStartedAt) / MILLISECONDS_PER_SECOND;
        boolean concealed = stage == Stage.SILHOUETTE && getRandomType() != GachaRandomType.NONE;
        RewardData displayed = concealed
                ? new RewardData(silhouette.item(reward, getRandomType(), itemStackEntries), 1) : reward;
        int color = concealed ? ModernGachaSilhouette.HIDDEN_COLOR : reward.getRarityColor();
        if (!effect.render(graphics, displayed, color, left, top, size, seconds, reveal, entry)) {
            renderFallback(graphics, displayed, color, left, top, size, reveal, entry);
        }
        if (concealed) {
            silhouette.renderQuestion(graphics, font, left, top, size, size, entry);
        }

        if (stage == Stage.REVEALED && entrance.isComplete()) {
            renderItemLabels(graphics, reward, left, top, size, mouseX, mouseY);
        }

        renderHint(graphics, stage == Stage.SILHOUETTE
                ? TranslationKeys.GUI_MODERN_REVEAL_HINT : TranslationKeys.GUI_MODERN_NEXT_HINT);
        renderSourceTitle(graphics);
    }

    private void renderFallback(GuiGraphics graphics, RewardData reward, int color, int left, int top, int size, float reveal,
                                ModernGachaEntrance.Frame entry) {
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

    private void renderHint(GuiGraphics graphics, String key) {
        if (isForceWatch()) {
            key = TranslationKeys.GUI_ANIMATION_FORCED;
        }
        graphics.drawCenteredString(font, Component.translatable(key), width / 2, height - FOOTER_MARGIN, TEXT_COLOR);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isShowingSummary()) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
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
        }
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (isShowingSummary()) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        if (isForceWatch() && (keyCode == GLFW.GLFW_KEY_SPACE || keyCode == GLFW.GLFW_KEY_LEFT_SHIFT
                || keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT)) {
            return true;
        }
        if (keyCode != GLFW.GLFW_KEY_SPACE && keyCode != GLFW.GLFW_KEY_LEFT_SHIFT
                && keyCode != GLFW.GLFW_KEY_RIGHT_SHIFT) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        if (!pressPresentationKey(keyCode) || stage == Stage.WAITING) {
            return true;
        }

        entrance.finish();
        if (keyCode == GLFW.GLFW_KEY_LEFT_SHIFT || keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            beginSummary();
            return true;
        }

        skipReveals = true;
        if (stage == Stage.SILHOUETTE) {
            revealCurrent(true);
        } else if (!instantReveal && Util.getMillis() - revealStartedAt < REVEAL_MILLIS) {
            instantReveal = true;
            completePresentedReward(singleRewards.get(currentItem), true);
        } else {
            nextItem();
        }
        return true;
    }

    @Override
    public void removed() {
        effect.close();
        super.removed();
    }

    private enum Stage {
        WAITING, SILHOUETTE, REVEALED
    }

}

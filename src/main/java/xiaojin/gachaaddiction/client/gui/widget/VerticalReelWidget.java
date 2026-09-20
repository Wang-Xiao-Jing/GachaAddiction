package xiaojin.gachaaddiction.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2fStack;
import xiaojin.gachaaddiction.config.GachaAddictionConfig;
import xiaojin.gachaaddiction.api.RewardData;
import xiaojin.gachaaddiction.api.client.ReelHost;
import xiaojin.gachaaddiction.config.ClientConfig;
import xiaojin.gachaaddiction.registry.GachaaAdictionConfigData;

import java.util.List;

public class VerticalReelWidget extends AbstractWidget {
    public static final int RAIL_WIDTH = 32;
    public static final int RAIL_STEP = 44;

    private static final int RAIL_FILL = 0xD00E0E14;
    private static final int RAIL_BORDER = 0xFF2E2E3C;
    private static final int RAIL_INNER_SHADOW = 0x400B0B10;
    private static final int WINDOW_TINT = 0x14FFFFFF;
    private static final int FADE_OPAQUE = 0xF00E0E14;
    private static final int FADE_CLEAR = 0x000E0E14;

    private final ClientConfig clientConfig = GachaAddictionConfig.CLIENT;
    private final ClientConfig.SlotMachineConfig slotMachineConfig = clientConfig.slotMachineConfig;
    private final ReelHost host;
    private final RewardData result;
    private final int resultColor;
    private final int resultLevel;
    private final List<ItemStack> decoys;
    private final int resultIndex;

    private float visualOffset;
    private boolean complete;
    private int lastPassedIndex;
    private boolean soundPlayed;
    private boolean exitStarted;
    private float exitProgress;
    private float landProgress;
    private final float speedMultiplier;
    private final float decelZone;
    private final float minSpeed;
    private final RandomSource randomSource;

    public VerticalReelWidget(ReelHost host, RewardData result, List<ItemStack> decoys, int resultIndex) {
        super(0, 0, RAIL_WIDTH, RAIL_WIDTH, Component.empty());
        this.host = host;
        this.result = result;
        this.decoys = decoys;
        this.resultIndex = resultIndex;
        this.resultColor = result.getRarityColor() | 0xFF000000;
        this.resultLevel = result.getRarityLevel();
        this.randomSource = RandomSource.create();

        double speedVarianceMin = slotMachineConfig.speedVarianceMin.get();
        double decelZone = slotMachineConfig.decelZone.get();
        double decelZoneVarianceMin = slotMachineConfig.decelZoneVarianceMin.get();
        double decelZoneVarianceMax = slotMachineConfig.decelZoneVarianceMax.get();
        double minSpeed = slotMachineConfig.decelZoneMinSpeed.get();
        double minSpeedVarianceMax = slotMachineConfig.minSpeedVarianceMax.get();
        double minSpeedVarianceMin = slotMachineConfig.minSpeedVarianceMin.get();

        this.speedMultiplier = (float) (speedVarianceMin + randomSource.nextFloat() * (slotMachineConfig.speedVarianceMax.get() - speedVarianceMin));
        this.decelZone = (float) Math.max(1,
                decelZone * decelZoneVarianceMin + randomSource.nextFloat() * (decelZoneVarianceMax - decelZoneVarianceMin));
        this.minSpeed = (float) Math.max(minSpeed,
                minSpeed * minSpeedVarianceMin + randomSource.nextFloat() * (minSpeedVarianceMax - minSpeedVarianceMin));
    }

    private float slotHeight() {
        return slotMachineConfig.itemSpacing.get().floatValue();
    }

    private int railHeight() {
        return Math.round(slotHeight() * slotMachineConfig.itemVisibleCount.get());
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.isHovered = isIsHovered(guiGraphics, mouseX, mouseY);

        tickAnimation(partialTick);

        int centerX = getX();
        int centerY = getY();
        int halfWidth = RAIL_WIDTH / 2;
        int halfHeight = railHeight() / 2;

        Matrix3x2fStack poseStack = guiGraphics.pose();
        poseStack.pushMatrix();
        poseStack.translate(centerX, centerY);
        renderRail(guiGraphics, halfWidth, halfHeight);
        poseStack.popMatrix();

        guiGraphics.enableScissor(centerX - halfWidth, centerY - halfHeight, centerX + halfWidth, centerY + halfHeight);
        poseStack.pushMatrix();
        poseStack.translate(centerX, centerY);
        renderItems(guiGraphics, poseStack);
        poseStack.popMatrix();
        guiGraphics.disableScissor();

        poseStack.pushMatrix();
        poseStack.translate(centerX, centerY);
        renderEdgeFade(guiGraphics, halfWidth, halfHeight);
        renderWindow(guiGraphics, halfWidth);
        renderExitAnimation(guiGraphics, poseStack);
        poseStack.popMatrix();

        renderItemTooltip(guiGraphics, mouseX, mouseY);
    }

    private boolean isIsHovered(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int halfWidth = RAIL_WIDTH / 2;
        int halfSlot = Math.round(slotHeight() / 2f);
        return guiGraphics.containsPointInScissor(mouseX, mouseY) &&
                mouseX >= this.getX() - halfWidth &&
                mouseY >= this.getY() - halfSlot &&
                mouseX < this.getX() + halfWidth &&
                mouseY < this.getY() + halfSlot;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        double halfWidth = RAIL_WIDTH / 2.0;
        double halfSlot = slotHeight() / 2.0;
        return this.active &&
                this.visible &&
                mouseX >= this.getX() - halfWidth &&
                mouseY >= this.getY() - halfSlot &&
                mouseX < this.getX() + halfWidth &&
                mouseY < this.getY() + halfSlot;
    }

    private void tickAnimation(float partialTick) {
        if (exitStarted) {
            exitProgress = Math.min(exitProgress + partialTick * slotMachineConfig.exitSpeed.get().floatValue(), 1f);
        }

        if (complete) {
            landProgress = Math.min(landProgress + partialTick * 0.2f, 1f);
            return;
        }

        float target = -resultIndex + (slotMachineConfig.itemVisibleCount.get() - 1) / 2f;
        float remaining = target - visualOffset;

        if (remaining >= 0f) {
            visualOffset = target;
            complete = true;
        } else {
            float dist = -remaining;
            float speedMul;
            if (dist > decelZone) {
                speedMul = 1f;
            } else {
                float t = dist / decelZone;
                speedMul = Math.max(minSpeed, t * t * (3f - 2f * t));
            }
            float move = partialTick * slotMachineConfig.itemsPerSecond.get().floatValue() * speedMul * speedMultiplier / 20f;
            visualOffset = Math.max(target, visualOffset - move);
        }

        if (slotMachineConfig.passSoundEffects.get()) {
            playPassSound();
        }
        if (clientConfig.rewardSoundEffects.get()) {
            playRewardSound(result.getCountItemStack());
        }
        if (complete && soundPlayed && !exitStarted) {
            exitStarted = true;
        }
    }

    private void playPassSound() {
        int passedIndex = (int) Math.floor(-visualOffset);
        if (passedIndex == lastPassedIndex) return;
        lastPassedIndex = passedIndex;
        if (!complete && host.tryClaimPassSound()) {
            getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
    }

    private void playRewardSound(ItemStack result) {
        if (!complete || soundPlayed) return;
        soundPlayed = true;
        if (resultLevel < host.getCurrentRenderHighestNewCompleteLevel()) {
            return;
        }
        if (!host.tryClaimCompleteSound()) {
            return;
        }
        getSoundManager().play(GachaaAdictionConfigData.getSoundEventsFunction().apply(result));
    }

    private void renderRail(GuiGraphics guiGraphics, int halfWidth, int halfHeight) {
        guiGraphics.fill(-halfWidth, -halfHeight, halfWidth, halfHeight, RAIL_FILL);
        guiGraphics.fill(-halfWidth, -halfHeight, -halfWidth + 1, halfHeight, RAIL_INNER_SHADOW);
        guiGraphics.fill(halfWidth - 1, -halfHeight, halfWidth, halfHeight, RAIL_INNER_SHADOW);
        guiGraphics.hLine(-halfWidth, halfWidth - 1, -halfHeight, RAIL_BORDER);
        guiGraphics.hLine(-halfWidth, halfWidth - 1, halfHeight - 1, RAIL_BORDER);
        guiGraphics.vLine(-halfWidth, -halfHeight, halfHeight - 1, RAIL_BORDER);
        guiGraphics.vLine(halfWidth - 1, -halfHeight, halfHeight - 1, RAIL_BORDER);
    }

    private void renderItems(GuiGraphics guiGraphics, Matrix3x2fStack poseStack) {
        int visibleCount = slotMachineConfig.itemVisibleCount.get();
        float spacing = slotHeight();
        float halfSpan = visibleCount * spacing / 2f;

        for (int i = 0; i < decoys.size(); i++) {
            float value = (i + visualOffset) - ((visibleCount - 1) / 2f);
            float y = value * spacing;
            if (y < -halfSpan - spacing || y > halfSpan + spacing) continue;

            float distance = Math.min(Math.abs(y) / Math.max(halfSpan, 1f), 1f);
            float scale = 1.25f - 0.55f * distance * distance;

            poseStack.pushMatrix();
            poseStack.translate(0f, y);
            poseStack.scale(scale, scale);
            if (i == resultIndex && complete) {
                guiGraphics.renderItem(result.getCountItemStack(), -8, -8);
                guiGraphics.renderItemDecorations(getFont(), result.getCountItemStack(), -8, -8);
            } else {
                guiGraphics.renderFakeItem(decoys.get(i), -8, -8);
            }
            poseStack.popMatrix();
        }
    }

    private void renderEdgeFade(GuiGraphics guiGraphics, int halfWidth, int halfHeight) {
        float maxAlpha = slotMachineConfig.itemMaxAlpha.get().floatValue();
        float minAlpha = slotMachineConfig.itemMinAlpha.get().floatValue();
        float strength = Math.clamp(maxAlpha - minAlpha, 0f, 1f);
        if (strength <= 0f) {
            return;
        }

        int opaque = scaleAlpha(FADE_OPAQUE, strength);
        int halfSlot = Math.round(slotHeight() / 2f);

        guiGraphics.fillGradient(-halfWidth, -halfHeight, halfWidth, -halfSlot, opaque, FADE_CLEAR);
        guiGraphics.fillGradient(-halfWidth, halfSlot, halfWidth, halfHeight, FADE_CLEAR, opaque);
    }

    private void renderWindow(GuiGraphics guiGraphics, int halfWidth) {
        int halfSlot = Math.round(slotHeight() / 2f);
        guiGraphics.fill(-halfWidth + 1, -halfSlot, halfWidth - 1, halfSlot, WINDOW_TINT);

        if (complete) {
            float pulse = 1f - landProgress;
            for (int ring = 3; ring >= 1; ring--) {
                float ringAlpha = (0.16f + 0.34f * pulse) / ring;
                int glow = scaleAlpha(resultColor, ringAlpha);
                drawOutline(guiGraphics, -halfWidth - ring, -halfSlot - ring, halfWidth + ring, halfSlot + ring, glow);
            }
        }

        drawOutline(guiGraphics, -halfWidth, -halfSlot, halfWidth, halfSlot, complete ? resultColor : 0x80FFFFFF);
    }

    private void drawOutline(GuiGraphics guiGraphics, int left, int top, int right, int bottom, int color) {
        guiGraphics.hLine(left, right - 1, top, color);
        guiGraphics.hLine(left, right - 1, bottom - 1, color);
        guiGraphics.vLine(left, top, bottom - 1, color);
        guiGraphics.vLine(right - 1, top, bottom - 1, color);
    }

    private int scaleAlpha(int color, float factor) {
        int alpha = (int) (((color >>> 24) & 0xFF) * Math.clamp(factor, 0f, 1f));
        return (alpha << 24) | (color & 0xFFFFFF);
    }

    private void renderExitAnimation(GuiGraphics guiGraphics, Matrix3x2fStack poseStack) {
        if (!exitStarted) {
            return;
        }
        float scale = 1.0f + slotMachineConfig.exitMaxScale.get().floatValue() * exitProgress;
        float alpha = 1.0f - exitProgress;
        if (scale <= 0 || alpha <= 0) {
            return;
        }

        poseStack.pushMatrix();
        poseStack.scale(scale, scale);
        guiGraphics.renderFakeItem(result.getCountItemStack(), -8, -8);
        int veil = scaleAlpha(FADE_OPAQUE, 1.0f - alpha);
        if (veil != 0) {
            guiGraphics.fill(-9, -9, 9, 9, veil);
        }
        poseStack.popMatrix();
    }

    @Override
    protected boolean isValidClickButton(MouseButtonInfo button) {
        return false;
    }

    private void renderItemTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!isHovered || !complete) {
            return;
        }
        guiGraphics.setTooltipForNextFrame(getFont(), result.getCountItemStack(), mouseX, mouseY);
    }

    private @NotNull SoundManager getSoundManager() {
        return getMinecraft().getSoundManager();
    }

    private @NotNull Font getFont() {
        return getMinecraft().font;
    }

    private @NotNull Minecraft getMinecraft() {
        return host.getMinecraft();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
    }

    public void skipToEnd() {
        visualOffset = -resultIndex + (slotMachineConfig.itemVisibleCount.get() - 1) / 2f;
        complete = true;
        playPassSound();
        playRewardSound(result.getCountItemStack());
        if (soundPlayed && !exitStarted) {
            exitStarted = true;
        }
    }

    public boolean isComplete() {
        return complete;
    }

    public boolean hasPlayedSound() {
        return soundPlayed;
    }

    public int getResultLevel() {
        return resultLevel;
    }

    public RewardData getResult() {
        return result;
    }
}

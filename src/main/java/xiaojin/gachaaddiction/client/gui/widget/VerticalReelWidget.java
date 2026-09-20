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
    public static final int RAIL_WIDTH = 36;
    public static final int RAIL_STEP = 52;

    private static final int MIN_SLOT_HEIGHT = 26;
    private static final int BASE_SCALE_PERCENT = 125;
    private static final int CAP_HEIGHT = 3;

    private static final int SHADOW = 0x66000000;
    private static final int FRAME_OUTER = 0xFF3C3C4E;
    private static final int FRAME_CAP = 0xFF2A2A38;
    private static final int FRAME_CAP_LINE = 0xFF12121A;
    private static final int RAIL_EDGE = 0xFF1C1C28;
    private static final int RAIL_CORE = 0xFF0A0A10;
    private static final int RAIL_LIGHT = 0x26FFFFFF;
    private static final int RAIL_SHADE = 0x1E000000;
    private static final int WINDOW_TINT = 0x12FFFFFF;
    private static final int WINDOW_SPLIT = 0xFF05050A;
    private static final int IDLE_FRAME = 0x90FFFFFF;

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
    private float age;
    private final float speedMultiplier;
    private final float decelZone;
    private final float minSpeed;
    private final RandomSource randomSource;

    public VerticalReelWidget(ReelHost host, RewardData result, List<ItemStack> decoys, int resultIndex,
                              float speedMultiplier) {
        super(0, 0, RAIL_WIDTH, RAIL_WIDTH, Component.empty());
        this.host = host;
        this.result = result;
        this.decoys = decoys;
        this.resultIndex = resultIndex;
        this.resultColor = result.getRarityColor() | 0xFF000000;
        this.resultLevel = result.getRarityLevel();
        this.randomSource = RandomSource.create();

        double decelZone = slotMachineConfig.decelZone.get();
        double decelZoneVarianceMin = slotMachineConfig.decelZoneVarianceMin.get();
        double decelZoneVarianceMax = slotMachineConfig.decelZoneVarianceMax.get();
        double minSpeed = slotMachineConfig.decelZoneMinSpeed.get();
        double minSpeedVarianceMax = slotMachineConfig.minSpeedVarianceMax.get();
        double minSpeedVarianceMin = slotMachineConfig.minSpeedVarianceMin.get();

        this.speedMultiplier = Math.max(0.05f, speedMultiplier);
        this.decelZone = (float) Math.max(1,
                decelZone * decelZoneVarianceMin + randomSource.nextFloat() * (decelZoneVarianceMax - decelZoneVarianceMin));
        this.minSpeed = (float) Math.max(minSpeed,
                minSpeed * minSpeedVarianceMin + randomSource.nextFloat() * (minSpeedVarianceMax - minSpeedVarianceMin));
    }

    private float slotHeight() {
        return Math.max(slotMachineConfig.itemSpacing.get().floatValue(), MIN_SLOT_HEIGHT);
    }

    private int slotHalf() {
        return Math.round(slotHeight() / 2f);
    }

    private int railHeight() {
        return Math.round(slotHeight() * slotMachineConfig.itemVisibleCount.get());
    }

    private float pop() {
        return exitStarted ? Math.clamp(exitProgress, 0f, 1f) : 0f;
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
        renderEdgeFade(guiGraphics, halfWidth, halfHeight);
        renderBeam(guiGraphics, halfWidth, halfHeight);
        renderSparks(guiGraphics, halfWidth, halfHeight);
        poseStack.popMatrix();
        guiGraphics.disableScissor();

        poseStack.pushMatrix();
        poseStack.translate(centerX, centerY);
        renderWindow(guiGraphics, halfWidth);
        renderBurst(guiGraphics, halfWidth);
        poseStack.popMatrix();

        renderItemTooltip(guiGraphics, mouseX, mouseY);
    }

    private boolean isIsHovered(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int halfWidth = RAIL_WIDTH / 2;
        int halfSlot = slotHalf();
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
        age += partialTick;

        if (exitStarted) {
            exitProgress = Math.min(exitProgress + partialTick * slotMachineConfig.exitSpeed.get().floatValue(), 1f);
        }

        if (!complete) {
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
        guiGraphics.fill(-halfWidth - 2, -halfHeight - 2, halfWidth + 2, halfHeight + 2, SHADOW);
        guiGraphics.fill(-halfWidth - 1, -halfHeight - 1, halfWidth + 1, halfHeight + 1,
                complete ? blend(FRAME_OUTER, resultColor, 0.45f) : FRAME_OUTER);

        guiGraphics.fillGradient(-halfWidth, -halfHeight, halfWidth, 0, RAIL_EDGE, RAIL_CORE);
        guiGraphics.fillGradient(-halfWidth, 0, halfWidth, halfHeight, RAIL_CORE, RAIL_EDGE);

        guiGraphics.fill(-halfWidth, -halfHeight, -halfWidth + 1, halfHeight, RAIL_LIGHT);
        guiGraphics.fill(halfWidth - 1, -halfHeight, halfWidth, halfHeight, RAIL_SHADE);

        guiGraphics.fill(-halfWidth, -halfHeight, halfWidth, -halfHeight + CAP_HEIGHT, FRAME_CAP);
        guiGraphics.fill(-halfWidth, halfHeight - CAP_HEIGHT, halfWidth, halfHeight, FRAME_CAP);
        guiGraphics.hLine(-halfWidth, halfWidth - 1, -halfHeight + CAP_HEIGHT, FRAME_CAP_LINE);
        guiGraphics.hLine(-halfWidth, halfWidth - 1, halfHeight - CAP_HEIGHT - 1, FRAME_CAP_LINE);
    }

    private void renderItems(GuiGraphics guiGraphics, Matrix3x2fStack poseStack) {
        int visibleCount = slotMachineConfig.itemVisibleCount.get();
        float spacing = slotHeight();
        float halfSpan = visibleCount * spacing / 2f;
        float baseScale = BASE_SCALE_PERCENT / 100f;

        for (int i = 0; i < decoys.size(); i++) {
            float value = (i + visualOffset) - ((visibleCount - 1) / 2f);
            float y = value * spacing;
            if (y < -halfSpan - spacing || y > halfSpan + spacing) continue;

            float distance = Math.min(Math.abs(y) / Math.max(halfSpan, 1f), 1f);
            float scale = baseScale - 0.55f * distance * distance;
            boolean isResult = i == resultIndex && complete;
            if (isResult) {
                float p = pop();
                float bump = (float) Math.sin(p * Math.PI) * (1f - p);
                scale = baseScale * (1f + slotMachineConfig.exitMaxScale.get().floatValue() * bump);
            }

            poseStack.pushMatrix();
            poseStack.translate(0f, y);
            poseStack.scale(scale, scale);
            if (isResult) {
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

        int opaque = scaleAlpha(RAIL_EDGE, strength);
        int clear = RAIL_EDGE & 0xFFFFFF;
        int halfSlot = slotHalf();

        guiGraphics.fillGradient(-halfWidth, -halfHeight, halfWidth, -halfSlot, opaque, clear);
        guiGraphics.fillGradient(-halfWidth, halfSlot, halfWidth, halfHeight, clear, opaque);
    }

    private void renderBeam(GuiGraphics guiGraphics, int halfWidth, int halfHeight) {
        if (!complete || resultLevel < 2) {
            return;
        }
        float breathe = 0.6f + 0.4f * (float) Math.sin(age * 0.12f);
        int beam = scaleAlpha(resultColor, 0.20f * breathe * (0.4f + 0.6f * pop()));
        int clear = resultColor & 0xFFFFFF;
        int halfSlot = slotHalf();

        guiGraphics.fillGradient(-halfWidth + 1, -halfHeight + CAP_HEIGHT, halfWidth - 1, -halfSlot, clear, beam);
        guiGraphics.fillGradient(-halfWidth + 1, halfSlot, halfWidth - 1, halfHeight - CAP_HEIGHT, beam, clear);
    }

    private void renderSparks(GuiGraphics guiGraphics, int halfWidth, int halfHeight) {
        if (!complete || resultLevel < 2) {
            return;
        }
        int count = (resultLevel - 1) * 4;
        int halfSlot = slotHalf();
        float travel = halfHeight - CAP_HEIGHT - halfSlot;
        if (travel <= 0) {
            return;
        }

        for (int i = 0; i < count; i++) {
            float seedX = hash(i * 2);
            float seedT = hash(i * 2 + 1);
            float phase = ((age * 0.014f) + seedT) % 1f;
            float fade = 1f - phase;
            int alphaColor = scaleAlpha(resultColor, 0.75f * fade * fade);
            if ((alphaColor >>> 24) == 0) continue;

            int x = Math.round((seedX * 2f - 1f) * (halfWidth - 4));
            int up = Math.round(halfSlot + phase * travel);
            int size = seedX > 0.6f ? 2 : 1;

            guiGraphics.fill(x, -up, x + size, -up + size, alphaColor);
            guiGraphics.fill(-x - size, up - size, -x, up, alphaColor);
        }
    }

    private void renderWindow(GuiGraphics guiGraphics, int halfWidth) {
        int halfSlot = slotHalf();
        float p = pop();
        float settle = complete ? 1f - p : 0f;

        guiGraphics.fill(-halfWidth, -halfSlot, halfWidth, halfSlot,
                complete ? scaleAlpha(resultColor, 0.10f) : WINDOW_TINT);
        guiGraphics.hLine(-halfWidth, halfWidth - 1, -halfSlot - 1, WINDOW_SPLIT);
        guiGraphics.hLine(-halfWidth, halfWidth - 1, halfSlot, WINDOW_SPLIT);

        if (complete) {
            int rings = 1 + resultLevel;
            for (int ring = rings; ring >= 1; ring--) {
                float ringAlpha = (0.14f + 0.40f * settle) / ring;
                drawOutline(guiGraphics, -halfWidth - ring, -halfSlot - ring, halfWidth + ring, halfSlot + ring,
                        scaleAlpha(resultColor, ringAlpha));
            }
        }

        drawOutline(guiGraphics, -halfWidth, -halfSlot, halfWidth, halfSlot, complete ? resultColor : IDLE_FRAME);

        if (complete) {
            int notchColor = scaleAlpha(resultColor, 0.55f + 0.45f * settle);
            for (int k = 0; k < 4; k++) {
                guiGraphics.fill(-halfWidth - 4 + k, -k, -halfWidth - 3 + k, k + 1, notchColor);
                guiGraphics.fill(halfWidth + 3 - k, -k, halfWidth + 4 - k, k + 1, notchColor);
            }
        }
    }

    private void renderBurst(GuiGraphics guiGraphics, int halfWidth) {
        if (!exitStarted) {
            return;
        }
        float p = pop();
        float fade = 1f - p;
        if (fade <= 0f) {
            return;
        }
        int halfSlot = slotHalf();

        int flash = scaleAlpha(0xFFFFFFFF, 0.45f * fade * fade);
        if ((flash >>> 24) > 0) {
            guiGraphics.fill(-halfWidth, -halfSlot, halfWidth, halfSlot, flash);
        }

        int waves = 1 + resultLevel;
        for (int wave = 0; wave < waves; wave++) {
            float offset = wave * 0.18f;
            float wp = p - offset;
            if (wp <= 0f || wp >= 1f) continue;
            int spread = Math.round(wp * (6 + 5 * resultLevel));
            int ringColor = scaleAlpha(resultColor, 0.60f * (1f - wp) * (1f - wp));
            if ((ringColor >>> 24) == 0) continue;
            drawOutline(guiGraphics, -halfWidth - spread, -halfSlot - spread,
                    halfWidth + spread, halfSlot + spread, ringColor);
        }
    }

    private void drawOutline(GuiGraphics guiGraphics, int left, int top, int right, int bottom, int color) {
        guiGraphics.hLine(left, right - 1, top, color);
        guiGraphics.hLine(left, right - 1, bottom - 1, color);
        guiGraphics.vLine(left, top, bottom - 1, color);
        guiGraphics.vLine(right - 1, top, bottom - 1, color);
    }

    private static float hash(int i) {
        int h = i * 374761393 + 668265263;
        h = (h ^ (h >>> 13)) * 1274126177;
        return ((h ^ (h >>> 16)) & 0xFFFF) / 65535f;
    }

    private static int scaleAlpha(int color, float factor) {
        int alpha = (int) (((color >>> 24) & 0xFF) * Math.clamp(factor, 0f, 1f));
        return (alpha << 24) | (color & 0xFFFFFF);
    }

    private static int blend(int base, int other, float amount) {
        float t = Math.clamp(amount, 0f, 1f);
        int a = (base >>> 24) & 0xFF;
        int r = Math.round((((base >> 16) & 0xFF) * (1f - t)) + (((other >> 16) & 0xFF) * t));
        int g = Math.round((((base >> 8) & 0xFF) * (1f - t)) + (((other >> 8) & 0xFF) * t));
        int b = Math.round(((base & 0xFF) * (1f - t)) + ((other & 0xFF) * t));
        return (a << 24) | (r << 16) | (g << 8) | b;
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

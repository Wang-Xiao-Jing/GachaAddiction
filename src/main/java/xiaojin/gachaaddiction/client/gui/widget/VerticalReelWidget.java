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
    private final float speedMultiplier;
    private final float decelZone;
    private float minSpeed;
    private final RandomSource randomSource;

    public VerticalReelWidget(ReelHost host, RewardData result, List<ItemStack> decoys, int resultIndex) {
        super(0, 0, 32, 32, Component.empty());
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

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.isHovered = isIsHovered(guiGraphics, mouseX, mouseY);

        tickAnimation(partialTick);

        Matrix3x2fStack poseStack = guiGraphics.pose();
        poseStack.pushMatrix();
        poseStack.translate(getX(), getY());
        renderItems(guiGraphics, poseStack);
        renderBox(guiGraphics, poseStack, result.getCountItemStack());
        renderExitAnimation(guiGraphics, poseStack);
        poseStack.popMatrix();
        renderItemTooltip(guiGraphics, mouseX, mouseY);
    }

    private boolean isIsHovered(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        return guiGraphics.containsPointInScissor(mouseX, mouseY) &&
                mouseX >= this.getX() - getWidth() / 2 &&
                mouseY >= this.getY() - getHeight() / 2 &&
                mouseX < this.getX() + getWidth() / 2 &&
                mouseY < this.getY() + getHeight() / 2;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return this.active &&
                this.visible &&
                mouseX >= this.getX() - (double) getWidth() / 2 &&
                mouseY >= this.getY() - (double) getHeight() / 2 &&
                mouseX < this.getX() + (double) getWidth() / 2 &&
                mouseY < this.getY() + (double) getHeight() / 2;
    }

    private void tickAnimation(float partialTick) {
        if (exitStarted) {
        exitProgress = Math.min(exitProgress + partialTick * slotMachineConfig.exitSpeed.get().floatValue(), 1f);
        }

        if (complete) {
            return;
        }

        float target = -resultIndex + (slotMachineConfig.itemVisibleCount.get() - 1) / 2f;
        float remaining = target - visualOffset;

        if (remaining >= 0f) {
            visualOffset = target;
            complete = true;
        } else {
            float dist = -remaining;
            float speedMul = dist > decelZone ? 1f
                    : Math.max(minSpeed, dist / decelZone);
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

    private void renderItems(GuiGraphics guiGraphics, Matrix3x2fStack poseStack) {
        int visibleCount = slotMachineConfig.itemVisibleCount.get();
        float itemSpacing = slotMachineConfig.itemSpacing.get().floatValue();

        poseStack.pushMatrix();
        for (int i = 0; i < decoys.size(); i++) {
            float value = (i + visualOffset) - ((visibleCount - 1) / 2f);
            float clamped = Math.clamp(value, -visibleCount, visibleCount);
            float normalized = Math.abs(clamped) / visibleCount;
            if (normalized >= 1.1f) continue;

            float scale = 1.0f + 0.25f * (1.0f - Math.min(normalized / 0.20f, 1.0f));
            if (scale <= 0f) continue;

            poseStack.pushMatrix();
            poseStack.translate(0f, itemSpacing * clamped);
            poseStack.scale(scale, scale);
            if (i == resultIndex && complete) {
                guiGraphics.renderItem(result.getCountItemStack(), -8, -8);
                guiGraphics.renderItemDecorations(getFont(), result.getCountItemStack(), -8, -8);
            } else {
                guiGraphics.renderFakeItem(decoys.get(i), -8, -8);
            }
            poseStack.popMatrix();
        }
        poseStack.popMatrix();

        renderEdgeFade(guiGraphics);
    }

    private void renderEdgeFade(GuiGraphics guiGraphics) {
        float maxAlpha = slotMachineConfig.itemMaxAlpha.get().floatValue();
        float minAlpha = slotMachineConfig.itemMinAlpha.get().floatValue();
        int strength = (int) (Math.clamp(maxAlpha - minAlpha, 0f, 1f) * 255f);
        if (strength <= 0) {
            return;
        }

        int halfHeight = getHeight() / 2;
        int halfWidth = getWidth() / 2;
        int opaque = strength << 24;

        guiGraphics.fillGradient(-halfWidth, -halfHeight, halfWidth, 0, opaque, 0);
        guiGraphics.fillGradient(-halfWidth, 0, halfWidth, halfHeight, 0, opaque);
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
        renderBox(guiGraphics, poseStack, result.getCountItemStack());
        int veil = (int) ((1.0f - alpha) * 255f) << 24;
        if (veil != 0) {
            guiGraphics.fill(-12, -12, 12, 12, veil);
        }
        poseStack.popMatrix();
    }

    private void renderBox(GuiGraphics guiGraphics, Matrix3x2fStack poseStack, ItemStack stack) {
        poseStack.pushMatrix();
        poseStack.scale(1.25f, 1.25f);
        int i = 18 / 2;
        int color;
        if (complete) {
            color = resultColor;
        } else {
            color = 0xffffffff;
        }
        guiGraphics.hLine(-i, i - 1, -i, color);
        guiGraphics.hLine(-i, i - 1, i - 1, color);
        guiGraphics.vLine(-i, -i, i - 1, color);
        guiGraphics.vLine(i - 1, -i, i - 1, color);
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

package xiaojin.gachaaddiction.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import xiaojin.gachaaddiction.GachaAddictionConfig;
import xiaojin.gachaaddiction.api.RewardData;
import xiaojin.gachaaddiction.client.gui.screen.SlotMachineScreen;
import xiaojin.gachaaddiction.config.ClientConfig;
import xiaojin.gachaaddiction.registry.GachaaAdictionConfigData;

import java.util.List;

public class VerticalReelWidget extends AbstractWidget {
    private final ClientConfig clientConfig = GachaAddictionConfig.CLIENT;
    private final ClientConfig.SlotMachineConfig slotMachineConfig = clientConfig.slotMachineConfig;
    private final SlotMachineScreen slotMachineScreen;
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

    public VerticalReelWidget(SlotMachineScreen slotMachineScreen, RewardData result, List<ItemStack> decoys, int resultIndex) {
        super(0, 0, 32, 32, Component.empty());
        this.slotMachineScreen = slotMachineScreen;
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
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.isHovered = isIsHovered(guiGraphics, mouseX, mouseY);

        tickAnimation(partialTick);

        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(getX(), getY(), 0);
        renderItems(guiGraphics, poseStack);
        renderBox(guiGraphics, poseStack, result.getCountItemStack());
        renderExitAnimation(guiGraphics, poseStack);
        poseStack.popPose();
        renderItemTooltip(guiGraphics, poseStack, mouseX, mouseY);
        guiGraphics.flush();
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
        if (!complete && slotMachineScreen.tryClaimPassSound()) {
            getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
    }

    private void playRewardSound(ItemStack result) {
        if (!complete || soundPlayed) return;
        soundPlayed = true;
        if (resultLevel < slotMachineScreen.getCurrentRenderHighestNewCompleteLevel()) {
            return;
        }
        if (!slotMachineScreen.tryClaimCompleteSound()) {
            return;
        }
        getSoundManager().play(GachaaAdictionConfigData.getSoundEventsFunction().apply(result));
    }

    private void renderItems(GuiGraphics guiGraphics, PoseStack poseStack) {
        int visibleCount = slotMachineConfig.itemVisibleCount.get();
        float maxAlpha = slotMachineConfig.itemMaxAlpha.get().floatValue();
        float minAlpha = slotMachineConfig.itemMinAlpha.get().floatValue();
        float itemSpacing = slotMachineConfig.itemSpacing.get().floatValue();

        poseStack.pushPose();
        for (int i = 0; i < decoys.size(); i++) {
            float value = (i + visualOffset) - ((visibleCount - 1) / 2f);
            float clamped = Math.clamp(value, -visibleCount, visibleCount);
            float normalized = Math.abs(clamped) / visibleCount;
            if (normalized >= 1.1f) continue;

            float scale = 1.0f + 0.25f * (1.0f - Math.min(normalized / 0.20f, 1.0f));
            float alpha = maxAlpha - (maxAlpha - minAlpha) * normalized;
            if (alpha <= 0f || scale <= 0f) continue;

            poseStack.pushPose();
            int finalI = i;
            alphaDraw(guiGraphics, () -> {
                poseStack.translate(0f, itemSpacing * clamped, 0f);
                poseStack.scale(scale, scale, scale);
                if (finalI == resultIndex && complete) {
                    guiGraphics.renderItem(result.getCountItemStack(), -8, -8);
                    guiGraphics.renderItemDecorations(getFont(), result.getCountItemStack(), -8, -8);
                } else {
                    guiGraphics.renderFakeItem(decoys.get(finalI), -8, -8);
                }
            }, alpha);
            poseStack.popPose();
        }
        poseStack.popPose();
        guiGraphics.flush();
    }

    private void alphaDraw(GuiGraphics guiGraphics, Runnable runnable, float alpha) {
        if (alpha < 0.999F) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
            guiGraphics.setColor(1.0F, 1.0F, 1.0F, alpha);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableDepthTest();
            runnable.run();
            guiGraphics.flush();
            RenderSystem.enableDepthTest();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableBlend();
        } else {
            RenderSystem.disableDepthTest();
            runnable.run();
            RenderSystem.enableDepthTest();
        }
    }

    private void renderExitAnimation(GuiGraphics guiGraphics, PoseStack poseStack) {
        if (!exitStarted) {
            return;
        }
        float scale = 1.0f + slotMachineConfig.exitMaxScale.get().floatValue() * exitProgress;
        float alpha = 1.0f - exitProgress;
        if (scale <= 0 || alpha <= 0) {
            return;
        }

        poseStack.pushPose();
        poseStack.scale(scale, scale, scale);
        alphaDraw(guiGraphics, () -> {
            guiGraphics.renderFakeItem(result.getCountItemStack(), -8, -8);
            renderBox(guiGraphics, poseStack, result.getCountItemStack());
        }, alpha);
        poseStack.popPose();
    }

    private void renderBox(GuiGraphics guiGraphics, PoseStack poseStack, ItemStack stack) {
        poseStack.pushPose();
        poseStack.translate(0, 0, 1000);
        poseStack.scale(1.25f, 1.25f, 1.25f);
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
        poseStack.popPose();
    }

    @Override
    protected boolean isValidClickButton(int button) {
        return false;
    }

    private void renderItemTooltip(GuiGraphics guiGraphics, PoseStack poseStack, int mouseX, int mouseY) {
        if (!isHovered || !complete) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0, 0, 1000);
        guiGraphics.renderTooltip(getFont(), result.getCountItemStack(), mouseX, mouseY);
        poseStack.popPose();
    }

    private @NotNull SoundManager getSoundManager() {
        return getMinecraft().getSoundManager();
    }

    private @NotNull Font getFont() {
        return getMinecraft().font;
    }

    private @NotNull Minecraft getMinecraft() {
        return slotMachineScreen.getMinecraft();
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

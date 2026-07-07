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
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import xiaojin.gachaaddiction.client.gui.screen.GachaScreen;
import xiaojin.gachaaddiction.init.ModSoundEvents;
import xiaojin.gachaaddiction.util.RarityUtil;

import java.util.List;

public class ReelWidget extends AbstractWidget {
    private final GachaScreen gachaScreen;
    private final int index;
    private final ItemStack result;
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

    public ReelWidget(GachaScreen gachaScreen, int index, ItemStack result, List<ItemStack> decoys, int resultIndex) {
        super(0, 0, 32, 32, Component.empty());
        this.gachaScreen = gachaScreen;
        this.index = index;
        this.result = result;
        this.decoys = decoys;
        this.resultIndex = resultIndex;
        this.resultColor = RarityUtil.getRarityColor(result) | 0xFF000000;
        this.resultLevel = RarityUtil.getRarityLevel(result);
        this.speedMultiplier = GachaScreen.ReelConfig.SPEED_VARIANCE_MIN
                + RandomSource.create().nextFloat()
                * (GachaScreen.ReelConfig.SPEED_VARIANCE_MAX - GachaScreen.ReelConfig.SPEED_VARIANCE_MIN);

        decelZone = Math.max(1, GachaScreen.ReelConfig.DECEL_ZONE * GachaScreen.ReelConfig.SPEED_VARIANCE_MIN
                + RandomSource.create().nextFloat()
                * (GachaScreen.ReelConfig.SPEED_VARIANCE_MAX - GachaScreen.ReelConfig.SPEED_VARIANCE_MIN));

        minSpeed = Math.max(GachaScreen.ReelConfig.MIN_SPEED, GachaScreen.ReelConfig.MIN_SPEED * GachaScreen.ReelConfig.SPEED_VARIANCE_MIN
                + RandomSource.create().nextFloat()
                * (GachaScreen.ReelConfig.SPEED_VARIANCE_MAX - GachaScreen.ReelConfig.SPEED_VARIANCE_MIN));
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.isHovered = isIsHovered(guiGraphics, mouseX, mouseY);

        tickAnimation(partialTick);

        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(getX(), getY(), 0);
        renderItems(guiGraphics, poseStack);
        renderBox(guiGraphics, poseStack, result);
//        if (!exitStarted) {
//            renderItems(guiGraphics, poseStack);
//            renderBox(guiGraphics, poseStack, result);
//        }
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
            exitProgress = Math.min(exitProgress + partialTick * GachaScreen.ReelConfig.EXIT_SPEED, 1f);
        }

        if (complete) return;

        float target = -resultIndex + (GachaScreen.ReelConfig.VISIBLE_COUNT - 1) / 2f;
        float remaining = target - visualOffset;

        if (remaining >= 0f) {
            visualOffset = target;
            complete = true;
        } else {
            float dist = -remaining;
            float speedMul = dist > decelZone ? 1f
                    : Math.max(minSpeed, dist / decelZone);
            float move = partialTick * GachaScreen.ReelConfig.ITEMS_PER_SECOND * speedMul * speedMultiplier / 20f;
            visualOffset = Math.max(target, visualOffset - move);
        }

        playPassSound();
        playCompleteSound();
        if (complete && soundPlayed && !exitStarted) {
            exitStarted = true;
        }
    }

    private void playPassSound() {
        int passedIndex = (int) Math.floor(-visualOffset);
        if (passedIndex == lastPassedIndex) return;
        lastPassedIndex = passedIndex;
        if (!complete) {
//            getSoundManager().stop(SoundEvents.UI_BUTTON_CLICK.value().getLocation(), SoundSource.MASTER);
            getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
    }

    private void playCompleteSound() {
        if (!complete || soundPlayed) return;
        soundPlayed = true;
        if (resultLevel < gachaScreen.getCurrentRenderHighestNewCompleteLevel()) {
            return;
        }
        if (!gachaScreen.tryClaimCompleteSound()) {
            return;
        }
//        getSoundManager().stop(SoundEvents.EXPERIENCE_ORB_PICKUP.getLocation(), SoundSource.MASTER);
        getSoundManager().play(SimpleSoundInstance.forUI(getSoundEvent(), 1.0F, 2));
    }

    private @NotNull SoundEvent getSoundEvent() {
        if (resultLevel >= 4) {
            return ModSoundEvents.LEVEL4.get();
        } else if (resultLevel == 3) {
            return ModSoundEvents.LEVEL3.get();
        } else if (resultLevel == 2) {
            return ModSoundEvents.LEVEL2.get();
        } else if (resultLevel == 1) {
            return ModSoundEvents.LEVEL1.get();
        } else {
            return ModSoundEvents.LEVEL0.get();
        }/* else {
            return SoundEvents.EXPERIENCE_ORB_PICKUP;
        }*/
    }

    private void renderItems(GuiGraphics guiGraphics, PoseStack poseStack) {
        poseStack.pushPose();
        for (int i = 0; i < decoys.size(); i++) {
            float value = (i + visualOffset) - ((GachaScreen.ReelConfig.VISIBLE_COUNT - 1) / 2f);
            float clamped = Math.clamp(value, -GachaScreen.ReelConfig.VISIBLE_COUNT, GachaScreen.ReelConfig.VISIBLE_COUNT);
            float normalized = Math.abs(clamped) / GachaScreen.ReelConfig.VISIBLE_COUNT;
            if (normalized >= 1.1f) continue;

            float scale = 1.0f + 0.25f * (1.0f - Math.min(normalized / 0.20f, 1.0f));
            float alpha = GachaScreen.ReelConfig.MAX_ALPHA - (GachaScreen.ReelConfig.MAX_ALPHA - GachaScreen.ReelConfig.MIN_ALPHA) * normalized;
            if (alpha <= 0f || scale <= 0f) continue;

            poseStack.pushPose();
            int finalI = i;
            alphaDraw(guiGraphics, () -> {
                poseStack.translate(0f, GachaScreen.ReelConfig.ITEM_SPACING * clamped, 0f);
                poseStack.scale(scale, scale, scale);
                if (finalI == resultIndex && complete) {
                    guiGraphics.renderItem(result, -8, -8);
                    guiGraphics.renderItemDecorations(getFont(), result, -8, -8);
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
        float scale = 1.0f + GachaScreen.ReelConfig.EXIT_MAX_SCALE * exitProgress;
        float alpha = 1.0f - exitProgress;
        if (scale <= 0 || alpha <= 0) {
            return;
        }

        poseStack.pushPose();
//        poseStack.translate(-8, -8, 0);
        poseStack.scale(scale, scale, scale);
        alphaDraw(guiGraphics, () -> {
            guiGraphics.renderFakeItem(result, -8, -8);
            renderBox(guiGraphics, poseStack, result);
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
        guiGraphics.renderTooltip(getFont(), result, mouseX, mouseY);
        poseStack.popPose();
    }

    private @NotNull SoundManager getSoundManager() {
        return getMinecraft().getSoundManager();
    }

    private @NotNull Font getFont() {
        return getMinecraft().font;
    }

    private @NotNull Minecraft getMinecraft() {
        return gachaScreen.getMinecraft();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
    }

    public void skipToEnd() {
        complete = true;
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

    public ItemStack getResult() {
        return result;
    }
}

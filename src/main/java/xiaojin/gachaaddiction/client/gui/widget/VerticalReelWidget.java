package xiaojin.gachaaddiction.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
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
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        this.isHovered = isIsHovered(graphics, mouseX, mouseY);

        tickAnimation(partialTick);

        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(getX(), getY());
        renderItems(graphics, pose);
        renderBox(graphics, pose);
        renderExitAnimation(graphics, pose);
        pose.popMatrix();
        renderItemTooltip(graphics, mouseX, mouseY);
    }

    private boolean isIsHovered(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        return graphics.containsPointInScissor(mouseX, mouseY) &&
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

    /**
     * 26.2 的物品绘制没有 alpha 参数（{@code GuiItemRenderState} 只有 pose/x/y，
     * {@code RenderSystem.setShaderColor} 也已移除），所以淡出改为在物品上方
     * 叠一层渐变遮罩——{@code fillGradient} 是支持 ARGB alpha 的。
     * <p>
     * 另外不能再把位置 clamp 到窗口边界，否则窗口外的物品会全部堆在最上和最下一行。
     */
    private void renderItems(GuiGraphicsExtractor graphics, Matrix3x2fStack pose) {
        int visibleCount = slotMachineConfig.itemVisibleCount.get();
        float itemSpacing = slotMachineConfig.itemSpacing.get().floatValue();

        for (int i = 0; i < decoys.size(); i++) {
            float value = (i + visualOffset) - ((visibleCount - 1) / 2f);
            if (Math.abs(value) >= visibleCount) continue;

            float normalized = Math.abs(value) / visibleCount;

            float scale = 1.0f + 0.25f * (1.0f - Math.min(normalized / 0.20f, 1.0f));
            if (scale <= 0f) continue;

            pose.pushMatrix();
            pose.translate(0f, itemSpacing * value);
            pose.scale(scale, scale);
            if (i == resultIndex && complete) {
                graphics.item(result.getCountItemStack(), -8, -8);
                graphics.itemDecorations(getFont(), result.getCountItemStack(), -8, -8);
            } else {
                graphics.fakeItem(decoys.get(i), -8, -8);
            }
            pose.popMatrix();
        }
        // 淡出遮罩由 SlotMachineScreen 统一画，跨整个屏幕宽度，
        // 免得每个滚轮各画一块拼出带硬边和直角的矩形。
    }

    private void renderExitAnimation(GuiGraphicsExtractor graphics, Matrix3x2fStack pose) {
        if (!exitStarted) {
            return;
        }
        float scale = 1.0f + slotMachineConfig.exitMaxScale.get().floatValue() * exitProgress;
        float alpha = 1.0f - exitProgress;
        if (scale <= 0 || alpha <= 0) {
            return;
        }

        pose.pushMatrix();
        pose.scale(scale, scale);
        graphics.fakeItem(result.getCountItemStack(), -8, -8);
        renderBox(graphics, pose, alpha);
        pose.popMatrix();
    }

    private void renderBox(GuiGraphicsExtractor graphics, Matrix3x2fStack pose) {
        renderBox(graphics, pose, 1.0f);
    }

    private void renderBox(GuiGraphicsExtractor graphics, Matrix3x2fStack pose, float alpha) {
        pose.pushMatrix();
        pose.scale(1.25f, 1.25f);
        int i = 18 / 2;
        int color = complete ? resultColor : 0xFFFFFFFF;
        // alpha 直接编码进 ARGB，替代已移除的 setShaderColor
        color = (color & 0x00FFFFFF) | (Math.round(Math.clamp(alpha, 0f, 1f) * 255f) << 24);
        graphics.horizontalLine(-i, i - 1, -i, color);
        graphics.horizontalLine(-i, i - 1, i - 1, color);
        graphics.verticalLine(-i, -i, i - 1, color);
        graphics.verticalLine(i - 1, -i, i - 1, color);
        pose.popMatrix();
    }

    @Override
    protected boolean isValidClickButton(MouseButtonInfo button) {
        return false;
    }

    private void renderItemTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (!isHovered || !complete) {
            return;
        }
        graphics.setTooltipForNextFrame(getFont(), result.getCountItemStack(), mouseX, mouseY);
    }

    private @NotNull SoundManager getSoundManager() {
        return getMinecraft().getSoundManager();
    }

    private @NotNull Font getFont() {
        return getMinecraft().font;
    }

    private @NotNull Minecraft getMinecraft() {
        return Minecraft.getInstance();
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

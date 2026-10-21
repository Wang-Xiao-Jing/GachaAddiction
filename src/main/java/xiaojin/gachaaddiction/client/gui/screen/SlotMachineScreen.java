package xiaojin.gachaaddiction.client.gui.screen;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import xiaojin.gachaaddiction.api.ItemStackEntry;
import xiaojin.gachaaddiction.api.RewardData;
import xiaojin.gachaaddiction.api.client.ReelHost;
import xiaojin.gachaaddiction.client.gui.widget.VerticalReelWidget;
import xiaojin.gachaaddiction.client.render.LoopingSlotMachineRenderer;
import xiaojin.gachaaddiction.client.render.ModernGachaEffect;
import xiaojin.gachaaddiction.config.ClientConfig;
import xiaojin.gachaaddiction.util.TranslationKeys;

import java.util.List;

public class SlotMachineScreen extends ModernGachaScreen {
    public static final String SKIP_HINT = TranslationKeys.GUI_SLOT_MACHINES_KIP_HINT;
    private static final long RESULT_HOLD_MILLIS = 450;
    private static final float MILLISECONDS_PER_SECOND = 1000.0F;
    private static final int LOOP_TINT = 0x9567E8;
    private static final int TEXT_COLOR = 0xFFFFFF;
    private static final int BACKGROUND = 0xFF0A0812;
    private static final int HINT_MARGIN = 24;
    private static final int TOOLTIP_DEPTH = 1000;
    protected final ClientConfig.SlotMachineConfig slotMachineConfig = clientConfig.slotMachineConfig;
    private final LoopingSlotMachineRenderer looping = new LoopingSlotMachineRenderer();
    private final ModernGachaEffect backdrop = new ModernGachaEffect();
    private boolean knownRewards;
    private boolean summary;
    private boolean spaceHeld;
    private final long startedAt = Util.getMillis();
    private long completedAt = -1;
    private int effectColor = LOOP_TINT;
    private boolean passSoundPlayedThisFrame;

    public SlotMachineScreen(@Nullable Screen originalScreen, List<@Nullable ResourceLocation> lootTableId, List<ItemStackEntry> itemStackEntries) {
        super(originalScreen, lootTableId, itemStackEntries);
    }

    /**
     * 创建单个转轮控件的共享工厂。
     * <p>
     * 供兼容界面与配置预览面板复用有限转轮；主老虎机界面使用独立的循环演出。
     *
     * @param host              转轮宿主（真实屏幕或预览宿主）
     * @param data              奖励数据
     * @param entries           可抽取的物品池
     * @param slotMachineConfig 老虎机配置
     * @param random            随机源
     */
    public static VerticalReelWidget createReelWidget(ReelHost host, RewardData data,
                                                      List<ItemStackEntry> entries,
                                                      ClientConfig.SlotMachineConfig slotMachineConfig,
                                                      RandomSource random) {
        int rarityLevel = data.getRarityLevel();
        int slotSize = slotMachineConfig.itemSlotSize.get();
        int decoyCount = 40;
        if (rarityLevel < 0) {
            decoyCount -= random.nextInt(Math.min(20, -rarityLevel * 2));
        } else if (rarityLevel > 0) {
            int min = rarityLevel * 2;
            int max = rarityLevel * slotSize;
            if (max > min) decoyCount += random.nextInt(min, max);
        } else {
            decoyCount += random.nextInt(0, slotSize);
        }

        List<ItemStack> decoys = ItemStackEntry.generateGachaResult(entries, decoyCount, random);

        int resultIndex = decoys.size() > slotSize
                ? random.nextInt(decoys.size() - slotSize) + slotSize
                : Math.max(0, decoys.size() - 1);

        decoys.add(resultIndex, data.getItemStack().getItem().getDefaultInstance());
        return new VerticalReelWidget(host, data, decoys, resultIndex);
    }

    @Override
    public void updateRewards(NonNullList<ItemStack> items) {
        super.updateRewards(items);
    }

    @Override
    protected void refreshWidgets() {
        knownRewards = true;
        int availableWidth = width > 0 ? width : Minecraft.getInstance().getWindow().getGuiScaledWidth();
        looping.reset(rewards, itemStackEntries, slotMachineConfig, availableWidth, Util.getMillis());
        if (rewards.isEmpty()) {
            showModernSummary();
        }
    }

    private void showModernSummary() {
        beginSummary();
        summary = true;
        backdrop.close();
    }

    public boolean tryClaimPassSound() {
        if (!slotMachineConfig.passSoundEffectsOptimize.get()) return true;
        if (passSoundPlayedThisFrame) return false;
        passSoundPlayedThisFrame = true;
        return true;
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (summary) {
            super.renderBackground(graphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        long now = Util.getMillis();
        if (!summary && knownRewards) {
            passSoundPlayedThisFrame = false;
            List<RewardData> completed = looping.update(now);
            int highest = completed.stream().mapToInt(RewardData::getRarityLevel).max().orElse(Integer.MIN_VALUE);
            boolean soundClaimed = false;
            for (RewardData reward : completed) {
                boolean playSound = !clientConfig.rewardSoundEffectsOptimize.get()
                        || !soundClaimed && reward.getRarityLevel() == highest;
                completePresentedReward(reward, playSound);
                soundClaimed |= playSound;
            }
            if (looping.passedAny() && slotMachineConfig.passSoundEffects.get() && tryClaimPassSound()) {
                getMinecraft().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            }
            if (looping.isComplete()) {
                if (completedAt < 0) {
                    completedAt = now;
                } else if (now - completedAt >= RESULT_HOLD_MILLIS) {
                    showModernSummary();
                }
            }
        }
        if (summary) {
            super.render(graphics, mouseX, mouseY, partialTick);
            return;
        }

        if (!backdrop.renderBackdrop(graphics, effectColor, (now - startedAt) / MILLISECONDS_PER_SECOND)) {
            graphics.fill(0, 0, width, height, BACKGROUND);
        }
        ItemStack hovered = knownRewards
                ? looping.render(graphics, width, height, mouseX, mouseY, partialTick) : ItemStack.EMPTY;
        String hint = !knownRewards ? TranslationKeys.GUI_ANIMATION_WAITING
                : looping.isStopping() ? TranslationKeys.GUI_LEGACY_SLOT_STOP_HINT
                : TranslationKeys.GUI_LEGACY_SLOT_SPIN_HINT;
        if (isForceWatch() && looping.isStopping()) {
            hint = TranslationKeys.GUI_ANIMATION_FORCED;
        }
        graphics.drawCenteredString(font, Component.translatable(hint), width / 2, height - HINT_MARGIN, TEXT_COLOR);
        renderSourceTitle(graphics);
        if (!hovered.isEmpty()) {
            graphics.flush();
            graphics.pose().pushPose();
            try {
                graphics.pose().translate(0, 0, TOOLTIP_DEPTH);
                graphics.renderTooltip(font, hovered, mouseX, mouseY);
            } finally {
                graphics.pose().popPose();
            }
        }
    }

    private void stopOrFinish() {
        if (!knownRewards) {
            return;
        }
        if (looping.isStopping()) {
            if (!isForceWatch()) {
                looping.finishNow();
            }
        } else {
            effectColor = rewards.isEmpty() ? LOOP_TINT : rewards.getFirst().getRarityColor();
            looping.stop(Util.getMillis());
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (summary) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            stopOrFinish();
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_SPACE && spaceHeld) {
            return true;
        }
        if (!summary && keyCode == GLFW.GLFW_KEY_SPACE) {
            spaceHeld = true;
            stopOrFinish();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_SPACE) {
            spaceHeld = false;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return summary && super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void removed() {
        backdrop.close();
        super.removed();
    }
}

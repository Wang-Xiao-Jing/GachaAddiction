package xiaojin.gachaaddiction.client.gui.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import xiaojin.gachaaddiction.api.ItemStackEntry;
import xiaojin.gachaaddiction.api.RewardData;
import xiaojin.gachaaddiction.client.resource.GachaRewardSoundResources;
import xiaojin.gachaaddiction.client.render.ModernGachaEffect;
import xiaojin.gachaaddiction.client.render.ModernGachaEntrance;
import xiaojin.gachaaddiction.registry.GachaTypes;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class GachaSummaryScreen extends BasicGachaScreen {
    protected static final int OVERLAY_DEPTH = 1000;
    protected static final int SINGLE_RARITY_THRESHOLD = 0;
    protected static final int SUMMARY_BACKGROUND = 0xCC080A10;
    protected static final int FOOTER_MARGIN = 20;
    protected static final int OPAQUE_ALPHA = 0xFF000000;
    protected static final int TEXT_COLOR = 0xFFFFFF;
    protected static final int FALLBACK_BACKGROUND = 0xFF08050D;
    protected static final float FALLBACK_TINT_STRENGTH = 0.35F;
    private final Set<RewardData> completedRewards = new HashSet<>();
    private final Set<Integer> pressedKeys = new HashSet<>();
    private boolean receivedResults;
    private boolean summaryActive;
    private final GachaSummaryView summary = new GachaSummaryView(this);

    protected GachaSummaryScreen(Component title, @Nullable Screen originalScreen,
                                 List<@Nullable ResourceLocation> lootTableId, List<ItemStackEntry> itemStackEntries) {
        super(title, originalScreen, lootTableId, itemStackEntries);
    }

    protected GachaSummaryScreen(@Nullable Screen originalScreen,
                                 List<@Nullable ResourceLocation> lootTableId, List<ItemStackEntry> itemStackEntries) {
        this(Component.translatable(GachaTypes.MODERN.getTranslationKey()), originalScreen, lootTableId, itemStackEntries);
    }

    @Override
    public void updateRewards(NonNullList<ItemStack> items) {
        updateRewards(items, false);
    }

    public void updateRewards(NonNullList<ItemStack> items, boolean summaryOnly) {
        // 本次展示固定使用第一次完整奖励快照，自动拿取后的容器重同步不重开揭晓。
        if (receivedResults) {
            return;
        }

        receivedResults = true;
        for (ItemStack item : items) {
            if (!item.isEmpty()) {
                rewardReceived(item, false);
            }
        }

        if (summaryOnly) {
            beginSummary();
        } else {
            refreshWidgets();
        }
    }

    public boolean hasReceivedResults() {
        return receivedResults;
    }

    protected void beginSummary() {
        summaryActive = true;
        summary.begin();
    }

    protected long beginSummary(ModernGachaEffect effect, ModernGachaEntrance entrance, long startedAt) {
        summaryActive = true;
        return summary.begin(effect, entrance, startedAt);
    }

    protected boolean isShowingSummary() {
        return summaryActive;
    }

    protected Font summaryFont() {
        return font;
    }

    @Override
    protected void refreshWidgets() {
        summary.resetPreview();
    }

    protected void renderSummaryScreen(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        if (summaryActive) {
            summary.render(graphics, mouseX, mouseY, partialTick);
        } else {
            renderSourceTitle(graphics);
        }
    }

    protected void renderSummaryItems(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        summary.renderItems(graphics, mouseX, mouseY, partialTick, true, false);
    }

    protected void resetSummaryPreview() {
        summary.resetPreview();
    }

    protected void renderSummaryPreview(GuiGraphics graphics, float partialTick, float progress, boolean heading) {
        summary.renderPreview(graphics, partialTick, progress, heading);
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (!summaryActive) {
            graphics.fill(0, 0, width, height, SUMMARY_BACKGROUND);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT || !summaryActive) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        if (summary.isEntryComplete()) {
            onClose();
        }
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (isForceWatch() && (keyCode == GLFW.GLFW_KEY_SPACE || keyCode == GLFW.GLFW_KEY_LEFT_SHIFT
                || keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT)) {
            return true;
        }
        if (keyCode != GLFW.GLFW_KEY_SPACE && keyCode != GLFW.GLFW_KEY_LEFT_SHIFT
                && keyCode != GLFW.GLFW_KEY_RIGHT_SHIFT) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        if (!pressPresentationKey(keyCode)) {
            return true;
        }
        if (summaryActive && keyCode == GLFW.GLFW_KEY_SPACE) {
            summary.finishEntry();
        }
        return true;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return pressedKeys.remove(keyCode) || super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!summaryActive || scrollY == 0.0) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        return summary.scroll(scrollY);
    }

    @Override
    public void removed() {
        summary.close();
        pressedKeys.clear();
        super.removed();
    }

    protected void completePresentedReward(RewardData reward, boolean playSound) {
        if (!completedRewards.add(reward)) {
            return;
        }

        onRewardRevealed(reward);
        var soundFunction = GachaRewardSoundResources.getSoundEventsFunction();
        if (playSound && clientConfig.rewardSoundEffects.get() && soundFunction != null) {
            getMinecraft().getSoundManager().play(soundFunction.apply(reward.getItemStack()));
        }
    }

    protected boolean pressPresentationKey(int keyCode) {
        return pressedKeys.add(keyCode);
    }

    protected void renderSummaryTooltip(GuiGraphics graphics, ItemStack item, int mouseX, int mouseY) {
        graphics.flush();
        graphics.pose().pushPose();
        try {
            graphics.pose().translate(0, 0, OVERLAY_DEPTH);
            graphics.renderTooltip(font, item, mouseX, mouseY);
        } finally {
            graphics.pose().popPose();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

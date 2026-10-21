package xiaojin.gachaaddiction.client.compat.ldlib2;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import xiaojin.gachaaddiction.GachaAddiction;
import xiaojin.gachaaddiction.api.ItemStackEntry;
import xiaojin.gachaaddiction.api.RewardData;
import xiaojin.gachaaddiction.client.compat.ldlib2.element.LDLib2GachaReelElement;
import xiaojin.gachaaddiction.client.gui.screen.BasicGachaScreen;
import xiaojin.gachaaddiction.client.gui.screen.SlotMachineScreen;
import xiaojin.gachaaddiction.client.gui.widget.VerticalReelWidget;
import xiaojin.gachaaddiction.network.stc.GachaPresentationPayload;
import xiaojin.gachaaddiction.registry.GachaTypes;
import xiaojin.gachaaddiction.util.TranslationKeys;

import java.util.ArrayList;
import java.util.List;

public class LDLib2GachaScreen extends BasicGachaScreen {
    private final LDLib2GachaResources.View view;
    private final ModularUI modularUI;
    private final List<VerticalReelWidget> reels = new ArrayList<>();
    private int page;
    private boolean passSoundPlayedThisFrame;
    private boolean fallbackStarted;
    private boolean receivedRewards;
    private @Nullable SlotMachineScreen fallbackScreen;

    public LDLib2GachaScreen(@Nullable Screen parent, List<@Nullable ResourceLocation> lootTableIds,
                             List<ItemStackEntry> entries) {
        super(Component.translatable(GachaTypes.LDLIB2.getTranslationKey()), parent, lootTableIds, entries);
        view = LDLib2GachaResources.load();
        view.title().setText(getTitle());
        view.context().setText(Component.empty());
        view.previous().setText(Component.translatable(TranslationKeys.GUI_CARD_POOL_PREVIOUS))
                .setOnClick(event -> changePage(-1));
        view.next().setText(Component.translatable(TranslationKeys.GUI_CARD_POOL_NEXT))
                .setOnClick(event -> changePage(1));
        view.skip().setText(Component.translatable(TranslationKeys.GUI_GACHA_SKIP))
                .setOnClick(event -> skipOrAdvance());
        view.close().setText(Component.translatable(TranslationKeys.GUI_CARD_POOL_RESULT_DONE))
                .setOnClick(event -> onClose());
        modularUI = ModularUI.of(UI.of(view.root()));
        rebuildPage();
    }

    @Override
    protected void init() {
        super.init();
        try {
            modularUI.setScreenAndInit(this);
            addRenderableWidget(modularUI.getWidget());
            setFocused(modularUI.getWidget());
        } catch (RuntimeException | LinkageError failure) {
            nativeFallback(failure);
        }
    }

    @Override
    public void setPresentation(GachaPresentationPayload presentation) {
        super.setPresentation(presentation);
        view.title().setText(getTitle());
    }

    @Override
    public void updateRewards(NonNullList<ItemStack> items) {
        receivedRewards = true;
        if (fallbackScreen != null) {
            fallbackScreen.updateRewards(items);
            return;
        }
        for (ItemStack item : items) {
            rewardReceived(item, false);
        }
        refreshWidgets();
    }

    @Override
    public void rewardReceived(ItemStack itemStack, boolean isRefreshWidget) {
        if (fallbackScreen != null) {
            fallbackScreen.rewardReceived(itemStack, isRefreshWidget);
            return;
        }
        super.rewardReceived(itemStack, isRefreshWidget);
    }

    @Override
    public void rewardReceived(ItemStack itemStack, int count, boolean isRefreshWidget) {
        if (fallbackScreen != null) {
            fallbackScreen.rewardReceived(itemStack, count, isRefreshWidget);
            return;
        }
        super.rewardReceived(itemStack, count, isRefreshWidget);
    }

    @Override
    protected void refreshWidgets() {
        if (fallbackScreen != null) {
            fallbackScreen.updateRewards(NonNullList.create());
            return;
        }
        reels.clear();
        RandomSource random = RandomSource.create();
        for (RewardData reward : rewards) {
            reels.add(SlotMachineScreen.createReelWidget(this, reward, itemStackEntries,
                    clientConfig.slotMachineConfig, random));
        }
        page = 0;
        rebuildPage();
    }

    private void rebuildPage() {
        view.rewards().clearAllScrollViewChildren();
        int reelHeight = Math.max(LDLib2GachaUiConstants.MINIMUM_REEL_HEIGHT,
                (int) (clientConfig.slotMachineConfig.itemVisibleCount.get()
                        * clientConfig.slotMachineConfig.itemSpacing.get()));
        for (VerticalReelWidget reel : pageReels()) {
            view.rewards().addScrollViewChild(new LDLib2GachaReelElement(reel, reelHeight));
        }
        view.previous().setActive(page > 0);
        view.next().setActive((page + 1) * LDLib2GachaUiConstants.REWARDS_PER_PAGE < reels.size());
        view.skip().setActive(!reels.isEmpty());
    }

    private List<VerticalReelWidget> pageReels() {
        int from = Math.min(page * LDLib2GachaUiConstants.REWARDS_PER_PAGE, reels.size());
        int to = Math.min(from + LDLib2GachaUiConstants.REWARDS_PER_PAGE, reels.size());
        return reels.subList(from, to);
    }

    private void changePage(int offset) {
        int selected = page + offset;
        if (selected < 0 || selected * LDLib2GachaUiConstants.REWARDS_PER_PAGE >= reels.size()) {
            return;
        }
        page = selected;
        rebuildPage();
    }

    private void skipOrAdvance() {
        var incomplete = pageReels().stream().filter(reel -> !reel.isComplete()).findFirst();
        if (incomplete.isPresent()) {
            if (!isForceWatch()) {
                incomplete.get().skipToEnd();
            }
        } else if ((page + 1) * LDLib2GachaUiConstants.REWARDS_PER_PAGE < reels.size()) {
            changePage(1);
        } else {
            onClose();
        }
    }

    @Override
    public boolean tryClaimPassSound() {
        if (!clientConfig.slotMachineConfig.passSoundEffectsOptimize.get()) return true;
        if (passSoundPlayedThisFrame) return false;
        passSoundPlayedThisFrame = true;
        return true;
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (receivedRewards && reels.stream().allMatch(VerticalReelWidget::isComplete)) {
            completePresentation();
        }
        if (clientConfig.rewardSoundEffectsOptimize.get()) {
            currentRenderHighestNewCompleteLevel = pageReels().stream()
                    .filter(reel -> reel.isComplete() && !reel.hasPlayedSound())
                    .mapToInt(VerticalReelWidget::getResultLevel).max().orElse(-1);
            completeSoundPlayedThisFrame = false;
        }
        passSoundPlayedThisFrame = false;
        try {
            super.render(graphics, mouseX, mouseY, partialTick);
        } catch (RuntimeException | LinkageError failure) {
            nativeFallback(failure);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_SPACE && !reels.isEmpty()) {
            skipOrAdvance();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void nativeFallback(Throwable failure) {
        if (fallbackStarted) {
            return;
        }
        fallbackStarted = true;
        GachaAddiction.LOGGER.warn(LDLib2GachaUiConstants.FALLBACK_LOG, failure);
        var fallback = new SlotMachineScreen(originalScreen, lootTableId, itemStackEntries);
        transferPresentationTo(fallback);
        NonNullList<ItemStack> stacks = NonNullList.create();
        rewards.forEach(reward -> stacks.add(reward.getCountItemStack().copy()));
        fallback.updateRewards(stacks);
        fallback.setReturnOriginalScreen(isReturnOriginalScreen());
        setReturnOriginalScreen(false);
        fallbackScreen = fallback;
        Minecraft.getInstance().setScreen(fallback);
    }
}

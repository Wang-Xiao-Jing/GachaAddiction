package xiaojin.gachaaddiction.client.gui.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import xiaojin.gachaaddiction.api.ItemStackEntry;
import xiaojin.gachaaddiction.api.RewardData;
import xiaojin.gachaaddiction.client.api.GachaRewardPhase;
import xiaojin.gachaaddiction.client.api.IReelHost;
import xiaojin.gachaaddiction.client.announcement.GachaAnnouncementClient;
import xiaojin.gachaaddiction.config.ClientConfig;
import xiaojin.gachaaddiction.config.GachaAddictionConfig;
import xiaojin.gachaaddiction.mixed.IAbstractContainerMenu;
import xiaojin.gachaaddiction.network.cts.GachaPresentationControlPayload;
import xiaojin.gachaaddiction.network.stc.GachaPresentationPayload;
import xiaojin.gachaaddiction.util.client.GachaRewardClientUtil;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;

public abstract class BasicGachaScreen extends Screen implements IReelHost {
    private static final Set<GachaRewardPhase> DEFAULT_AUTO_TAKE_PHASES =
            Set.of(GachaRewardPhase.OPEN);
    private static final float SOURCE_TITLE_HEIGHT_FRACTION = 0.06F;
    private static final int SOURCE_TITLE_COLOR = 0xFFFFFF;
    protected final ClientConfig clientConfig = GachaAddictionConfig.CLIENT;
    protected final boolean isMerge = clientConfig.mergeItem.get();
    protected final boolean raritySorting = clientConfig.raritySorting.get();
    protected final @Nullable Screen originalScreen;
    protected final List<@Nullable ResourceLocation> lootTableId = new ArrayList<>();
    protected final List<ItemStackEntry> itemStackEntries = new ArrayList<>();
    protected final List<RewardData> rewards = new ArrayList<>();
    private final Map<GachaRewardPhase, Set<Integer>> autoTakeRequests = new EnumMap<>(GachaRewardPhase.class);
    protected int currentRenderHighestNewCompleteLevel = -1;
    protected boolean completeSoundPlayedThisFrame = false;
    private boolean isReturnOriginalScreen = true;
    private GachaPresentationPayload presentation;
    private boolean presentationComplete;
    private ItemStack triggerItem = ItemStack.EMPTY;
    private int presentationMenuId = Integer.MIN_VALUE;

    protected BasicGachaScreen(
            Component title,
            @Nullable Screen originalScreen,
            List<@Nullable ResourceLocation> lootTableId,
            List<ItemStackEntry> itemStackEntries) {
        super(title);
        this.originalScreen = originalScreen;
        this.lootTableId.addAll(lootTableId);
        this.itemStackEntries.addAll(itemStackEntries);
    }

    public static <T> List<T> mergePreservingOrder(
            List<T> list,
            BiPredicate<T, T> matcher,
            BinaryOperator<T> merger) {

        List<T> result = new ArrayList<>();
        for (T current : list) {
            boolean merged = false;
            // 遍历现有结果，看是否有符合匹配的
            for (int i = 0; i < result.size(); i++) {
                T existing = result.get(i);
                if (matcher.test(existing, current)) {
                    // 合并并替换
                    result.set(i, merger.apply(existing, current));
                    merged = true;
                    break;
                }
            }
            if (!merged) {
                result.add(current);
            }
        }
        return result;
    }

    public abstract void updateRewards(NonNullList<ItemStack> item);

    @Override
    public Component getTitle() {
        if (originalScreen instanceof MenuAccess<?>) {
            return originalScreen.getTitle();
        }
        ItemStack trigger = getTriggerItem();
        return trigger.isEmpty() ? Blocks.CHEST.getName() : trigger.getHoverName();
    }

    protected void renderSourceTitle(GuiGraphics graphics) {
        graphics.drawCenteredString(font, getTitle(), width / 2,
                (int) (height * SOURCE_TITLE_HEIGHT_FRACTION), SOURCE_TITLE_COLOR);
    }

    public void bindPresentationMenu(int menuId) {
        presentationMenuId = menuId;
    }

    public boolean acceptsPresentationMenu(int menuId) {
        return presentationMenuId == menuId && !presentationComplete;
    }

    public boolean acceptsPresentationToken(UUID token) {
        return presentation == null || presentation.token().equals(token);
    }

    public boolean hasExplicitAnimation() {
        if (presentation != null && presentation.animation().isPresent()) {
            return true;
        }
        if (originalScreen instanceof MenuAccess<?> menu) {
            var data = IAbstractContainerMenu.of(menu.getMenu()).gachaaddiction$getGachaaData();
            return data != null && data.gachaType().isPresent();
        }
        return false;
    }

    public boolean hasReceivedResults() {
        return !rewards.isEmpty();
    }

    public void preparePresentationReplacement() {
    }

    public void replayRewardsTo(BasicGachaScreen target) {
        if (!hasReceivedResults()) {
            return;
        }
        NonNullList<ItemStack> snapshot = NonNullList.create();
        rewards.forEach(reward -> snapshot.add(reward.getCountItemStack().copy()));
        target.updateRewards(snapshot);
    }

    public void setPresentation(GachaPresentationPayload presentation) {
        this.presentation = presentation;
        triggerItem = presentation.trigger().copyWithCount(1);
    }

    public void transferPresentationTo(BasicGachaScreen target) {
        target.presentation = presentation;
        target.presentationComplete = presentationComplete;
        target.triggerItem = triggerItem;
        target.presentationMenuId = presentationMenuId;
        autoTakeRequests.forEach((phase, requests) -> target.autoTakeRequests.put(phase, new HashSet<>(requests)));
        presentation = null;
    }

    public boolean isForceWatch() {
        return presentation != null && presentation.forceWatch() && !presentationComplete;
    }

    public ItemStack getTriggerItem() {
        return triggerItem;
    }

    public void releasePresentation(UUID token) {
        if (presentation != null && presentation.token().equals(token)) {
            presentationComplete = true;
        }
    }

    protected void completePresentation() {
        if (!presentationComplete) {
            presentationComplete = true;
            sendPresentationControl(true);
        }
    }

    private void sendPresentationControl(boolean completed) {
        if (presentation != null && minecraft != null && minecraft.getConnection() != null
                && minecraft.getConnection().hasChannel(GachaPresentationControlPayload.TYPE)) {
            PacketDistributor.sendToServer(
                    new GachaPresentationControlPayload(presentation.token(), completed));
        }
    }

    @Override
    public void onClose() {
        if (!isForceWatch()) {
            super.onClose();
        }
    }

    @Override
    public void removed() {
        if (!presentationComplete) {
            sendPresentationControl(false);
        }
        super.removed();
    }

    public void rewardReceived(ItemStack itemStack, boolean isRefreshWidget) {
        rewards.add(new RewardData(itemStack.copyWithCount(1), itemStack.getCount()));
        handleRewardPhase(GachaRewardPhase.OPEN, new RewardData(itemStack.copyWithCount(1), itemStack.getCount()));
        if (raritySorting) {
            raritySorting();
        }
        if (isMerge) {
            merge();
        }
        if (isRefreshWidget) {
            refreshWidgets();
        }
    }

    protected abstract void refreshWidgets();

    protected Set<GachaRewardPhase> getAutoTakePhases() {
        return DEFAULT_AUTO_TAKE_PHASES;
    }

    protected void handleRewardPhase(GachaRewardPhase phase, RewardData reward) {
        if (getAutoTakePhases().contains(phase)) {
            GachaRewardClientUtil.requestAutoTake(this, reward,
                    autoTakeRequests.computeIfAbsent(phase, ignored -> new HashSet<>()));
        }
    }

    @Override
    public void onRewardRevealed(RewardData reward) {
        handleRewardPhase(GachaRewardPhase.COMPLETE, reward);
    }

    @Override
    public boolean canAnnounceReward(RewardData reward) {
        return GachaAnnouncementClient.INSTANCE.canAnnounce(this, reward.getCountItemStack());
    }

    public void rewardReceived(ItemStack itemStack, int count, boolean isRefreshWidget) {
        rewards.add(new RewardData(itemStack.copyWithCount(1), Math.max(1, count)));
        handleRewardPhase(GachaRewardPhase.OPEN, new RewardData(itemStack.copyWithCount(1), Math.max(1, count)));
        if (raritySorting) {
            raritySorting();
        }
        if (isMerge) {
            merge();
        }
        if (isRefreshWidget) {
            refreshWidgets();
        }
    }

    protected void merge() {
        List<RewardData> rewards1 = new ArrayList<>(rewards);
        rewards.clear();
        rewards.addAll(mergePreservingOrder(rewards1,
                (stack, other) -> ItemStack.isSameItemSameComponents(stack.getItemStack(), other.getItemStack()),
                (oldData, newData) -> new RewardData(oldData.getItemStack(), oldData.getCount() + newData.getCount())));
    }

    protected void raritySorting() {
        rewards.sort(Comparator.comparingInt(RewardData::getRarityLevel)
                .thenComparingInt(RewardData::getCount)
                .reversed());
    }

    public @Nullable Screen getOriginalScreen() {
        return originalScreen;
    }

    public List<ItemStackEntry> getItemStackEntries() {
        return itemStackEntries;
    }

    public List<@Nullable ResourceLocation> getLootTableId() {
        return lootTableId;
    }

    public boolean isReturnOriginalScreen() {
        return isReturnOriginalScreen;
    }

    public BasicGachaScreen setReturnOriginalScreen(boolean returnOriginalScreen) {
        isReturnOriginalScreen = returnOriginalScreen;
        return this;
    }

    public int getCurrentRenderHighestNewCompleteLevel() {
        if (!clientConfig.rewardSoundEffectsOptimize.get()) return -1;
        return currentRenderHighestNewCompleteLevel;
    }

    public boolean tryClaimCompleteSound() {
        if (!clientConfig.rewardSoundEffectsOptimize.get()) return true;
        if (completeSoundPlayedThisFrame) return false;
        completeSoundPlayedThisFrame = true;
        return true;
    }

    @Override
    public boolean tryClaimPassSound() {
        return true;
    }
}

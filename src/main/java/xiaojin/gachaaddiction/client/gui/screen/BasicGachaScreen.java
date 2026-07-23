package xiaojin.gachaaddiction.client.gui.screen;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import xiaojin.gachaaddiction.GachaAddictionConfig;
import xiaojin.gachaaddiction.api.ItemStackEntry;
import xiaojin.gachaaddiction.api.RewardData;
import xiaojin.gachaaddiction.config.ClientConfig;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;

public abstract class BasicGachaScreen extends Screen {
    protected final ClientConfig clientConfig = GachaAddictionConfig.CLIENT;
    protected final boolean isMerge = clientConfig.mergeItem.get();
    protected final boolean raritySorting = clientConfig.raritySorting.get();
    protected final @Nullable Screen originalScreen;
    protected final List<@Nullable ResourceLocation> lootTableId = new ArrayList<>();
    protected final List<ItemStackEntry> itemStackEntries = new ArrayList<>();
    protected final List<RewardData> rewards = new ArrayList<>();
    private boolean isReturnOriginalScreen = true;
    protected int currentRenderHighestNewCompleteLevel = -1;
    protected boolean completeSoundPlayedThisFrame = false;

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

    public abstract void updateRewards(NonNullList<ItemStack> item);

    public void rewardReceived(ItemStack itemStack, boolean isRefreshWidget) {
        rewards.add(new RewardData(itemStack.copyWithCount(1), itemStack.getCount()));
        if (isRefreshWidget) {
            refreshWidgets();
        }
        if (raritySorting) {
            raritySorting();
        }
        if (isMerge) {
            merge();
        }
    }

    protected abstract void refreshWidgets();

    public void rewardReceived(ItemStack itemStack, int count, boolean isRefreshWidget) {
        rewards.add(new RewardData(itemStack.copyWithCount(1), count));
        if (isRefreshWidget) {
            refreshWidgets();
        }
        if (raritySorting) {
            raritySorting();
        }
        if (isMerge) {
            merge();
        }
    }

    protected void merge() {
        List<RewardData> rewards1 = new ArrayList<>(rewards);
        rewards.clear();
        rewards.addAll(mergePreservingOrder(rewards1,
                (stack, other) -> ItemStack.isSameItemSameComponents(stack.getItemStack(), other.getItemStack()),
                (oldData, newData) -> new RewardData(oldData.getItemStack(), oldData.getCount() + newData.getCount())));
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
}

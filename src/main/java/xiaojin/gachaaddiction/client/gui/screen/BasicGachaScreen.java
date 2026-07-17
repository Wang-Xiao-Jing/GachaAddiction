package xiaojin.gachaaddiction.client.gui.screen;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import xiaojin.gachaaddiction.api.ItemStackEntry;
import xiaojin.gachaaddiction.api.RewardData;

import java.util.ArrayList;
import java.util.List;

public abstract class BasicGachaScreen extends Screen {
    private boolean isMerge;
    private boolean isReturnOriginalScreen = true;
    protected final @Nullable Screen originalScreen;
    protected final List<@Nullable ResourceLocation> lootTableId = new ArrayList<>();
    protected final List<ItemStackEntry> itemStackEntries = new ArrayList<>();
    protected final List<ItemStack> rewards = new ArrayList<>();
    protected final Object2IntOpenHashMap<RewardData> mergeRewards = new Object2IntOpenHashMap<>();

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

    public void rewardReceived(ItemStack itemStack, boolean isRefresh) {
        rewards.add(itemStack.copy());
        RewardData rewardData = new RewardData(itemStack.copyWithCount(1));
        mergeRewards.put(rewardData, mergeRewards.getInt(rewardData) + itemStack.getCount());
        if (isRefresh) {
            refreshWidgets();
        }
    }

    protected abstract void refreshWidgets();

    public void rewardReceived(ItemStack itemStack, int count, boolean isRefresh) {
        ItemStack itemStack1 = itemStack.copyWithCount(1);
        rewards.add(itemStack1);
        RewardData rewardData = new RewardData(itemStack1);
        mergeRewards.put(rewardData, mergeRewards.getInt(rewardData) + count);
        if (isRefresh) {
            refreshWidgets();
        }
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

    public boolean isMerge() {
        return isMerge;
    }

    public void setMerge(boolean merge) {
        isMerge = merge;
    }

    public boolean isReturnOriginalScreen() {
        return isReturnOriginalScreen;
    }

    public void setReturnOriginalScreen(boolean returnOriginalScreen) {
        isReturnOriginalScreen = returnOriginalScreen;
    }
}

package xiaojin.gachaaddiction.util.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import xiaojin.gachaaddiction.api.RewardData;
import xiaojin.gachaaddiction.client.gui.screen.BasicGachaScreen;
import xiaojin.gachaaddiction.config.GachaAddictionConfig;
import xiaojin.gachaaddiction.config.filter.FilterRuleMatcher;
import xiaojin.gachaaddiction.network.cts.GachaAutoTakePayload;
import xiaojin.gachaaddiction.util.RarityUtil;

import java.util.Set;

public class GachaRewardClientUtil {
    public static boolean isEligible(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        var config = GachaAddictionConfig.CLIENT;
        return FilterRuleMatcher.matches(stack, config.whitelistFilterList.get())
                || RarityUtil.getRarityLevel(stack) >= config.minimumRewardRarity.get();
    }

    public static void requestAutoTake(BasicGachaScreen screen, RewardData reward, Set<Integer> requestedSlots) {
        Minecraft minecraft = Minecraft.getInstance();
        var player = minecraft.player;
        var connection = minecraft.getConnection();
        if (!GachaAddictionConfig.COMMON.autoTakeEnabled.get() || player == null || connection == null
                || minecraft.screen != screen || !connection.hasChannel(GachaAutoTakePayload.TYPE)
                || !(screen.getOriginalScreen() instanceof MenuAccess<?> source)) {
            return;
        }
        ItemStack expectedReward = reward.getCountItemStack();
        if (!isEligible(expectedReward)) {
            return;
        }
        var menu = source.getMenu();
        if (menu != player.containerMenu || menu == player.inventoryMenu) {
            return;
        }
        int remaining = expectedReward.getCount();
        for (int index = 0; index < menu.slots.size() && remaining > 0; index++) {
            var slot = menu.slots.get(index);
            ItemStack stack = slot.getItem();
            if (slot.container == player.getInventory() || stack.isEmpty() || requestedSlots.contains(index)
                    || !ItemStack.isSameItemSameComponents(stack, expectedReward)
                    || stack.getCount() > remaining) {
                continue;
            }
            requestedSlots.add(index);
            remaining -= stack.getCount();
            PacketDistributor.sendToServer(new GachaAutoTakePayload(menu.containerId, index, stack.copy()));
        }
    }
}

package xiaojin.gachaaddiction.mixin.compat.ftb;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.ftb.mods.ftbquests.client.gui.RewardNotificationsScreen;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.loot.RewardTable;
import dev.ftb.mods.ftbquests.quest.loot.WeightedReward;
import dev.ftb.mods.ftbquests.quest.reward.ItemReward;
import dev.ftb.mods.ftbquests.quest.reward.LootReward;
import dev.ftb.mods.ftbquests.quest.reward.RandomReward;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xiaojin.gachaaddiction.api.GachaType;
import xiaojin.gachaaddiction.api.ItemStackEntry;
import xiaojin.gachaaddiction.config.GachaAddictionConfig;
import xiaojin.gachaaddiction.mixed.ILootRewardModeMap;
import xiaojin.gachaaddiction.registry.client.GachaTypeClients;

import java.util.ArrayList;
import java.util.List;

@Mixin(LootReward.class)
public abstract class LootRewardClientMixin extends RandomReward {
    public LootRewardClientMixin(long id, Quest parent) {
        super(id, parent);
    }

    @WrapOperation(method = "onButtonClicked", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftbquests/client/gui/RewardNotificationsScreen;openGui()V"))
    private void gachaaddiction$onButtonClicked(RewardNotificationsScreen instance, Operation<Void> original) {
        original.call(instance);
        if (!GachaAddictionConfig.CLIENT.ftbQuestsGachaa.get()) {
            return;
        }

        GachaType gachaType = ((ILootRewardModeMap) this).gachaaddiction$getGachaType();
        if (gachaType.isEmpty()) {
            gachaType = GachaAddictionConfig.CLIENT.getDefaultGachaaType();
            if (gachaType.isEmpty()) {
                return;
            }
        }

        RewardTable table = getTable();
        List<ItemStackEntry> itemStackEntries;
        if (table == null) {
            itemStackEntries = List.of();
        } else {
            itemStackEntries = new ArrayList<>();
            for (WeightedReward weightedReward : table.getWeightedRewards()) {
                Reward reward = weightedReward.getReward();
                if (reward instanceof ItemReward itemReward) {
                    ItemStack itemStack = itemReward.getItem().copyWithCount(itemReward.getCount());
                    int weight = (int) (weightedReward.getWeight() * 100);
                    itemStackEntries.add(new ItemStackEntry(itemStack, weight));
                }
            }
        }

        GachaTypeClients.get(gachaType)
                .open(List.of(), itemStackEntries)
                .setReturnOriginalScreen(false);
    }

}

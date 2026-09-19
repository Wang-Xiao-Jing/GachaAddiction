package xiaojin.gachaaddiction.mixin.compat.ftb;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.ftb.mods.ftbquests.client.gui.RewardNotificationsScreen;
import dev.ftb.mods.ftbquests.item.LootCrateItem;
import dev.ftb.mods.ftbquests.quest.loot.LootCrate;
import dev.ftb.mods.ftbquests.quest.loot.WeightedReward;
import dev.ftb.mods.ftbquests.quest.reward.ItemReward;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import xiaojin.gachaaddiction.config.GachaAddictionConfig;
import xiaojin.gachaaddiction.api.GachaType;
import xiaojin.gachaaddiction.api.ItemStackEntry;

import java.util.ArrayList;
import java.util.List;

@Pseudo
@Mixin(LootCrateItem.class)
public abstract class LootCrateItemMixin {
    @WrapOperation(method = "use", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftbquests/client/gui/RewardNotificationsScreen;openGui()V"))
    private void gachaaddiction$use$openGui(RewardNotificationsScreen instance, Operation<Void> original,
                                            @Local(argsOnly = true) Player player,
                                            @Local(argsOnly = true) InteractionHand hand) {
        original.call(instance);

        ItemStack stack = player.getItemInHand(hand);
        LootCrate crate = LootCrateItem.getCrate(stack, true);
        if (crate == null) {
            return;
        }

        GachaType gachaType = GachaAddictionConfig.CLIENT.getDefaultGachaaType();
        if (gachaType.isEmpty()) {
            return;
        }

        List<ItemStackEntry> itemStackEntries = new ArrayList<>();
        for (WeightedReward weightedReward : crate.getTable().getWeightedRewards()) {
            Reward reward = weightedReward.getReward();
            if (reward instanceof ItemReward itemReward) {
                ItemStack itemStack = itemReward.getItem().copyWithCount(itemReward.getCount());
                int weight = (int) (weightedReward.getWeight() * 100);
                itemStackEntries.add(new ItemStackEntry(itemStack, weight));
            }
        }

        gachaType.getClientData()
                .open(List.of(), itemStackEntries)
                .setReturnOriginalScreen(false);
    }
}

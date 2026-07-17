package xiaojin.gachaaddiction.mixin.ftb;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.ftb.mods.ftblibrary.util.client.ClientUtils;
import dev.ftb.mods.ftbquests.client.FTBQuestsNetClient;
import dev.ftb.mods.ftbquests.client.gui.RewardKey;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xiaojin.gachaaddiction.client.gui.screen.BasicGachaScreen;

@Mixin(FTBQuestsNetClient.class)
public abstract class FTBquestsNetClientMixin {
    @WrapOperation(method = "displayItemRewardToast", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftbquests/client/gui/IRewardListenerScreen;add(Ldev/ftb/mods/ftbquests/client/gui/RewardKey;I)Z"))
    private static boolean gachaaddiction$displayItemRewardToast(RewardKey key, int count, Operation<Boolean> original, @Local(argsOnly = true) ItemStack stack) {
        boolean call = original.call(key, count);
        BasicGachaScreen currentGuiAs = ClientUtils.getCurrentGuiAs(BasicGachaScreen.class);
        if (currentGuiAs != null) {
            currentGuiAs.rewardReceived(stack, count, true);
        }
        return call;
    }
}

package xiaojin.gachaaddiction.mixin.compat.lootbundles;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import cy.jdkdigital.lootbundles.item.LootBundle;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xiaojin.gachaaddiction.api.ItemStackEntry;
import xiaojin.gachaaddiction.network.OpenGachaScreenPayload;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 兼容 Loot Bundles（战利品袋）：玩家右键打开战利品袋时，服务端生成掉落物后，
 * 将实际掉落的物品作为候选池与奖励发送给客户端打开 gacha 抽奖屏幕。
 * 一次使用（含潜行整组开启）的多次生成会累积后一次性展示。
 */
@Mixin(LootBundle.class)
public abstract class LootBundleMixin {
    @WrapOperation(method = "use",
            at = @At(value = "INVOKE", target = "Lcy/jdkdigital/lootbundles/item/LootBundle;dropContents(Ljava/util/List;Lnet/minecraft/world/entity/player/Player;)Z"))
    private boolean gachaaddiction$use$dropContents(List<ItemStack> items, Player player, Operation<Boolean> original,
                                                    @Share("gachaaddiction$allItems") LocalRef<List<ItemStack>> allItems) {
        boolean result = original.call(items, player);
        if (allItems.get() == null) {
            allItems.set(new ArrayList<>());
        }
        allItems.get().addAll(items);
        return result;
    }

    @Inject(method = "use", at = @At("RETURN"))
    private void gachaaddiction$use$return(Level level, Player player, InteractionHand hand,
                                           CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir,
                                           @Share("gachaaddiction$allItems") LocalRef<List<ItemStack>> allItems) {
        if (level.isClientSide()) {
            return;
        }

        List<ItemStack> items = allItems.get();
        if (items == null || items.isEmpty()) {
            return;
        }

        List<ItemStackEntry> entries = new ArrayList<>(items.size());
        for (ItemStack stack : items) {
            entries.add(new ItemStackEntry(stack.copy(), 1));
        }

        PacketDistributor.sendToPlayer((ServerPlayer) player,
                new OpenGachaScreenPayload(Optional.empty(), List.of(), entries, items));
    }
}

package xiaojin.gachaaddiction.mixin.compat.treasurebags;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.neoforge.network.PacketDistributor;
import net.silentchaos512.lib.item.LootContainerItem;
import net.silentchaos512.treasurebags.item.TreasureBagItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xiaojin.gachaaddiction.network.OpenGachaScreenPayload;
import xiaojin.gachaaddiction.util.LootDisplayCache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Mixin(TreasureBagItem.class)
public abstract class TreasureBagItemMixin {
    @Shadow
    protected abstract ResourceKey<LootTable> getLootTable(ItemStack stack);

    @Inject(method = "use", at = @At(value = "INVOKE", target = "Ljava/util/Collection;forEach(Ljava/util/function/Consumer;)V"))
    private void gachaaddiction$use$forEach(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir,
                                            @Share("itemList") LocalRef<List<ItemStack>> itemList,
                                            @Local(name = "lootDrops") Collection<ItemStack> lootDrops) {
        itemList.set(new ArrayList<>());
        itemList.get().addAll(lootDrops);
    }

    @Inject(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"))
    private void gachaaddiction$use$shrink(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir,
                                           @Share("itemList") LocalRef<List<ItemStack>> itemList,
                                           @Local(name = "heldItem") ItemStack heldItem) {
        if (itemList.get() == null) {
            itemList.set(new ArrayList<>());
        }

        ResourceKey<LootTable> lootTable = this.getLootTable(heldItem);
        // TODO 自定义GachaType
        PacketDistributor.sendToPlayer((ServerPlayer) player, new OpenGachaScreenPayload(
                Optional.empty(),
                List.of(lootTable.location()),
                LootDisplayCache.get(lootTable),
                itemList.get()
        ));
    }
}

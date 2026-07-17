package xiaojin.gachaaddiction.mixin;

import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xiaojin.gachaaddiction.mixed.IMenuProviderLootExtension;

import java.util.List;

@Mixin(targets = "net.minecraft.world.level.block.ChestBlock$2$1")
public abstract class ChestBlock_2_1Mixin implements IMenuProviderLootExtension {
    @Shadow
    @Final
    ChestBlockEntity val$p_51604_;

    @Shadow
    @Final
    ChestBlockEntity val$p_51605_;

    @Override
    public List<@Nullable ResourceKey<LootTable>> gachaaddiction$getLootTable() {
        return ObjectList.of(val$p_51604_.getLootTable(), val$p_51605_.getLootTable());
    }
}
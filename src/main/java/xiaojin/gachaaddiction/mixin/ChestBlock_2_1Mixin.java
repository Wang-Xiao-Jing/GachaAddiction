package xiaojin.gachaaddiction.mixin;

import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import xiaojin.gachaaddiction.mixed.IMenuProviderLootExtension;
import xiaojin.gachaaddiction.mixed.IRandomizableContainerBlockEntity;

import java.util.List;

@Mixin(targets = "net.minecraft.world.level.block.ChestBlock$2$1")
public abstract class ChestBlock_2_1Mixin implements IMenuProviderLootExtension {
    @Shadow
    @Final
    ChestBlockEntity val$first;

    @Shadow
    @Final
    ChestBlockEntity val$second;

    @Override
    public List<@Nullable ResourceKey<LootTable>> gachaaddiction$getLootTable() {
        return ObjectList.of(val$first.getLootTable(), val$second.getLootTable());
    }

    @Override
    public boolean gachaaddiction$isOpenGachaaUi() {
        return gachaaddiction$getFirst().gachaaddiction$isOpenGachaaUi() ||
                gachaaddiction$getSecond().gachaaddiction$isOpenGachaaUi();
    }

    @Override
    public void gachaaddiction$setOpenGachaaUi(boolean isOpen) {
        gachaaddiction$getFirst().gachaaddiction$setOpenGachaaUi(isOpen);
        gachaaddiction$getSecond().gachaaddiction$setOpenGachaaUi(isOpen);
    }

    @Override
    public List<@Nullable ResourceKey<LootTable>> gachaaddiction$getOldLootTable() {
        return ObjectList.of(gachaaddiction$getFirst().gachaaddiction$getOldLootTable(),
                gachaaddiction$getSecond().gachaaddiction$getOldLootTable());
    }

    @Unique
    private IRandomizableContainerBlockEntity gachaaddiction$getSecond() {
        return IRandomizableContainerBlockEntity.of(val$second);
    }

    @Unique
    private IRandomizableContainerBlockEntity gachaaddiction$getFirst() {
        return IRandomizableContainerBlockEntity.of(val$first);
    }
}
package xiaojin.gachaaddiction.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xiaojin.gachaaddiction.mixed.IRandomizableContainerBlockEntity;
import xiaojin.gachaaddiction.util.ModUtil;

import org.jetbrains.annotations.Nullable;

@Mixin(RandomizableContainerBlockEntity.class)
public abstract class RandomizableContainerBlockEntityMixin extends BaseContainerBlockEntity implements IRandomizableContainerBlockEntity, RandomizableContainer {
    @Shadow
    @Nullable
    protected ResourceKey<LootTable> lootTable;

    @Shadow
    @Nullable
    public abstract ResourceKey<LootTable> getLootTable();

    @Nullable
    @Unique
    private ResourceKey<LootTable> gachaaddiction$oldLootTable;
    @Unique
    private boolean gachaaddiction$isOpenGachaaUi;

    protected RandomizableContainerBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    public boolean gachaaddiction$isOpenGachaaUi() {
        return gachaaddiction$isOpenGachaaUi;
    }

    @Override
    public void gachaaddiction$setOpenGachaaUi(boolean isOpens) {
        gachaaddiction$isOpenGachaaUi = isOpens;
    }

    @Override
    public @Nullable ResourceKey<LootTable> gachaaddiction$getOldLootTable() {
        return gachaaddiction$oldLootTable;
    }

    @Inject(method = "removeComponentsFromTag", at = @At("RETURN"))
    private void gachaaddiction$removeComponentsFromTag(ValueOutput tag, CallbackInfo ci) {
        tag.discard(ModUtil.OPEN_GACHAA_UI);
    }

    @Inject(method = "setLootTable", at = @At("HEAD"))
    private void gachaaddiction$setLootTable(ResourceKey<LootTable> lootTable, CallbackInfo ci) {
        if (lootTable == null && this.getLootTable() != null) {
            gachaaddiction$isOpenGachaaUi = true;
        }
        gachaaddiction$oldLootTable = this.getLootTable();
    }

    @Override
    protected void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        this.gachaaddiction$isOpenGachaaUi = tag.getBooleanOr(ModUtil.OPEN_GACHAA_UI, false);
    }

    @Override
    protected void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        if (gachaaddiction$isOpenGachaaUi) {
            tag.putBoolean(ModUtil.OPEN_GACHAA_UI, true);
        }
    }
}

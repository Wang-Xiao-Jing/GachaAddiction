package xiaojin.gachaaddiction.mixin.entity;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecartContainer;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xiaojin.gachaaddiction.mixed.IAbstractMinecartContainer;
import xiaojin.gachaaddiction.util.ModUtil;

import org.jetbrains.annotations.Nullable;

@Mixin(AbstractMinecartContainer.class)
public abstract class AbstractMinecartContainerMixin extends AbstractMinecart implements IAbstractMinecartContainer, ContainerEntity {
    @Shadow
    @Nullable
    public abstract ResourceKey<LootTable> getContainerLootTable();

    @Shadow
    @Nullable
    private ResourceKey<LootTable> lootTable;
    @Nullable
    @Unique
    private ResourceKey<LootTable> gachaaddiction$oldLootTable;

    @Unique
    private boolean gachaaddiction$isOpenGachaaUi;

    protected AbstractMinecartContainerMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public boolean gachaaddiction$isOpenGachaaUi() {
        return gachaaddiction$isOpenGachaaUi || getContainerLootTable() != null;
    }

    @Override
    public void gachaaddiction$setOpenGachaaUi(boolean isOpen) {
        gachaaddiction$isOpenGachaaUi = isOpen;
    }

    @Override
    public @org.jetbrains.annotations.Nullable ResourceKey<LootTable> gachaaddiction$getOldLootTable() {
        return gachaaddiction$oldLootTable;
    }

    @Inject(method = "setLootTable*", at = @At("HEAD"))
    private void gachaaddiction$setLootTable(ResourceKey<LootTable> lootTable, long seed, CallbackInfo ci) {
        if (lootTable == null && this.getContainerLootTable() != null) {
            gachaaddiction$isOpenGachaaUi = true;
        }
        gachaaddiction$oldLootTable = this.getContainerLootTable();
    }

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    private void gachaaddiction$addAdditionalSaveData(ValueOutput output, CallbackInfo ci) {
        if (gachaaddiction$isOpenGachaaUi) {
            output.putBoolean(ModUtil.OPEN_GACHAA_UI, true);
        } else {
            output.discard(ModUtil.OPEN_GACHAA_UI);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    private void gachaaddiction$readAdditionalSaveData(ValueInput input, CallbackInfo ci) {
        this.gachaaddiction$isOpenGachaaUi = input.getBooleanOr(ModUtil.OPEN_GACHAA_UI, false);
    }
}

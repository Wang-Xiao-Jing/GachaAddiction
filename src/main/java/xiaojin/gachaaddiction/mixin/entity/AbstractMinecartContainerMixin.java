package xiaojin.gachaaddiction.mixin.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.AbstractMinecartContainer;
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

import javax.annotation.Nullable;

@Mixin(AbstractMinecartContainer.class)
public abstract class AbstractMinecartContainerMixin extends AbstractMinecart implements IAbstractMinecartContainer, ContainerEntity {
    @Nullable
    @Unique
    private ResourceKey<LootTable> gachaaddiction$oldLootTable;
    @Unique
    private boolean gachaaddiction$isOpenGachaaUi;

    protected AbstractMinecartContainerMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow
    @Nullable
    public abstract ResourceKey<LootTable> getLootTable();

    @Override
    public boolean gachaaddiction$isOpenGachaaUi() {
        return gachaaddiction$isOpenGachaaUi || getLootTable() != null;
    }

    @Override
    public void gachaaddiction$setOpenGachaaUi(boolean isOpen) {
        gachaaddiction$isOpenGachaaUi = isOpen;
    }

    @Override
    public @org.jetbrains.annotations.Nullable ResourceKey<LootTable> gachaaddiction$getOldLootTable() {
        return gachaaddiction$oldLootTable;
    }

    @Inject(method = "setLootTable(Lnet/minecraft/resources/ResourceKey;)V", at = @At("HEAD"))
    private void gachaaddiction$setLootTable(ResourceKey<LootTable> lootTable, CallbackInfo ci) {
        gachaaddiction$rememberLootTable(lootTable);
    }

    @Inject(method = "setLootTable(Lnet/minecraft/resources/ResourceKey;J)V", at = @At("HEAD"))
    private void gachaaddiction$setLootTable(ResourceKey<LootTable> lootTable, long seed, CallbackInfo ci) {
        gachaaddiction$rememberLootTable(lootTable);
    }

    @Unique
    private void gachaaddiction$rememberLootTable(ResourceKey<LootTable> lootTable) {
        if (lootTable == null && this.getLootTable() != null) {
            gachaaddiction$isOpenGachaaUi = true;
        }
        gachaaddiction$oldLootTable = this.getLootTable();
    }

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    private void gachaaddiction$addAdditionalSaveData(CompoundTag compound, CallbackInfo ci) {
        if (gachaaddiction$isOpenGachaaUi) {
            compound.putBoolean(ModUtil.OPEN_GACHAA_UI, true);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    private void gachaaddiction$readAdditionalSaveData(CompoundTag compound, CallbackInfo ci) {
        gachaaddiction$isOpenGachaaUi = compound.getBoolean(ModUtil.OPEN_GACHAA_UI);
    }
}

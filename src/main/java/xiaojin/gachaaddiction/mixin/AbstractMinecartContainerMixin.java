package xiaojin.gachaaddiction.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecartContainer;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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
    public @Nullable ResourceKey<LootTable> gachaaddiction$getOldLootTable() {
        return gachaaddiction$oldLootTable;
    }

    /**
     * 战利品被取出时 {@code unpackChestVehicleLootTable} 会把表设为 null，
     * 这里记录下来以便之后仍能显示抽奖界面。
     * <p>
     * 26.2 的 {@code setLootTable} 是 (ResourceKey, long)，
     * 真正表示“清空”的是单参的 {@code setContainerLootTable}，所以注入后者。
     */
    @Inject(method = "setContainerLootTable", at = @At("HEAD"))
    private void gachaaddiction$setContainerLootTable(ResourceKey<LootTable> lootTable, CallbackInfo ci) {
        if (lootTable == null && this.getContainerLootTable() != null) {
            gachaaddiction$isOpenGachaaUi = true;
        }
        gachaaddiction$oldLootTable = this.getContainerLootTable();
    }

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    private void gachaaddiction$addAdditionalSaveData(ValueOutput output, CallbackInfo ci) {
        if (gachaaddiction$isOpenGachaaUi) {
            output.putBoolean(ModUtil.OPEN_GACHAA_UI, true);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    private void gachaaddiction$readAdditionalSaveData(ValueInput input, CallbackInfo ci) {
        this.gachaaddiction$isOpenGachaaUi = input.getBooleanOr(ModUtil.OPEN_GACHAA_UI, false);
    }
}

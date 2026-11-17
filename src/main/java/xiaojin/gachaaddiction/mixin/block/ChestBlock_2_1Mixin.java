package xiaojin.gachaaddiction.mixin.block;

import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import xiaojin.gachaaddiction.api.GachaRandomType;
import xiaojin.gachaaddiction.api.GachaSourceSettings;
import xiaojin.gachaaddiction.api.IGachaSource;
import xiaojin.gachaaddiction.mixed.IMenuProviderLootExtension;
import xiaojin.gachaaddiction.mixed.IRandomizableContainerBlockEntity;
import xiaojin.gachaaddiction.registry.GachaSources;
import xiaojin.gachaaddiction.util.TranslationKeys;

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

    @Override
    public IGachaSource gachaaddiction$getGachaSource() {
        var first = GachaSources.find(val$p_51604_);
        var second = GachaSources.find(val$p_51605_);
        if (first == null) {
            return second;
        }
        if (second == null) {
            return first;
        }
        return new IGachaSource() {
            @Override
            public GachaSourceSettings getSettings() {
                var left = first.getSettings();
                var right = second.getSettings();
                return new GachaSourceSettings(left.gachaType().or(right::gachaType), left.forceWatch() || right.forceWatch(),
                        left.secondaryType().or(right::secondaryType), left.secondaryType().isPresent() ? left.choiceCount() : right.choiceCount(),
                        left.randomType() != GachaRandomType.NONE ? left.randomType() : right.randomType());
            }

            @Override
            public void setSettings(GachaSourceSettings settings) {
                first.setSettings(settings);
                second.setSettings(settings);
            }

            @Override
            public ItemStack getDisplayItem() {
                return first.getDisplayItem();
            }

            @Override
            public Object getLockTarget() {
                return first.getLockTarget();
            }
        };
    }

    @Override
    public boolean gachaaddiction$isOpenGachaaUi() {
        return gachaaddiction$getVal$p51604().gachaaddiction$isOpenGachaaUi() ||
                gachaaddiction$getVal$p51605().gachaaddiction$isOpenGachaaUi();
    }

    @Override
    public void gachaaddiction$setOpenGachaaUi(boolean isOpen) {
        gachaaddiction$getVal$p51604().gachaaddiction$setOpenGachaaUi(isOpen);
        gachaaddiction$getVal$p51605().gachaaddiction$setOpenGachaaUi(isOpen);
    }

    @Override
    public List<@Nullable ResourceKey<LootTable>> gachaaddiction$getOldLootTable() {
        return ObjectList.of(gachaaddiction$getVal$p51604().gachaaddiction$getOldLootTable(),
                gachaaddiction$getVal$p51605().gachaaddiction$getOldLootTable());
    }

    @Unique
    private IRandomizableContainerBlockEntity gachaaddiction$getVal$p51605() {
        return IRandomizableContainerBlockEntity.of(val$p_51605_);
    }

    @Override
    public BlockPos gachaaddiction$getAnnouncementPosition() {
        return val$p_51604_.getBlockPos();
    }

    @Override
    public Component gachaaddiction$getAnnouncementName() {
        return Component.translatable(TranslationKeys.DOUBLE_CHEST_NAME);
    }

    @Override
    public @Nullable Component gachaaddiction$getAnnouncementCustomName() {
        return val$p_51604_.hasCustomName() ? val$p_51604_.getCustomName() : val$p_51605_.getCustomName();
    }

    @Unique
    private IRandomizableContainerBlockEntity gachaaddiction$getVal$p51604() {
        return IRandomizableContainerBlockEntity.of(val$p_51604_);
    }
}

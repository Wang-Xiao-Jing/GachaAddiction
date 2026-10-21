package xiaojin.gachaaddiction.mixin.entity;

import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import xiaojin.gachaaddiction.api.GachaData;
import xiaojin.gachaaddiction.mixed.IAbstractContainerMenu;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin implements IAbstractContainerMenu {
    @Unique
    @Nullable
    private GachaData gachaaddiction$gachaaData;

    @Override
    public @Nullable GachaData gachaaddiction$getGachaaData() {
        return gachaaddiction$gachaaData;
    }

    @Override
    public void gachaaddiction$setGachaaData(@Nullable GachaData data) {
        gachaaddiction$gachaaData = data;
    }
}

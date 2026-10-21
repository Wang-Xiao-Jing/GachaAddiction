package xiaojin.gachaaddiction.mixed;

import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;
import xiaojin.gachaaddiction.api.GachaData;

public interface IAbstractContainerMenu {
    static IAbstractContainerMenu of(AbstractContainerMenu menu) {
        return (IAbstractContainerMenu) menu;
    }

    @Nullable
    GachaData gachaaddiction$getGachaaData();

    void gachaaddiction$setGachaaData(GachaData data);
}

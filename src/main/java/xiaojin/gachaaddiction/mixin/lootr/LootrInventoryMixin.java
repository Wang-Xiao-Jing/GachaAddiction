package xiaojin.gachaaddiction.mixin.lootr;

import noobanidus.mods.lootr.common.api.interfaces.inventory.ILootrInventory;
import noobanidus.mods.lootr.common.data.LootrInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import xiaojin.gachaaddiction.mixed.IILootrInventory;

/**
 * 本次会话内记录该 Lootr 容器是否已被打开过。
 * <p>
 * Lootr 26.2 改为 Codec 序列化，原先挂在 {@code saveToTag} 上的持久化注入已无对应目标；
 * 跨存档的状态改由 Lootr 自己的 {@code ILootrData#hasBeenOpened()} 提供，
 * 见 {@link xiaojin.gachaaddiction.util.LootrUtil#isOpen}。
 */
@Mixin(LootrInventory.class)
public abstract class LootrInventoryMixin implements ILootrInventory, IILootrInventory {
    @Unique
    private boolean gachaaddiction$isOpen;

    @Override
    public boolean gachaaddiction$isOpen() {
        return gachaaddiction$isOpen;
    }

    @Override
    public void gachaaddiction$setOpen(boolean isOpen) {
        this.gachaaddiction$isOpen = isOpen;
    }
}

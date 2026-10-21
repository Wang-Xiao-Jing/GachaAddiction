package xiaojin.gachaaddiction.util;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public class GachaLockedItemHandler implements IItemHandler {
    private final IItemHandler delegate;
    private final Object source;

    private GachaLockedItemHandler(IItemHandler delegate, Object source) {
        this.delegate = delegate;
        this.source = source;
    }

    public static @Nullable IItemHandler wrap(@Nullable IItemHandler handler, Object source) {
        return handler == null || handler instanceof GachaLockedItemHandler ? handler : new GachaLockedItemHandler(handler, source);
    }

    @Override
    public int getSlots() {
        return delegate.getSlots();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return delegate.getStackInSlot(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return delegate.insertItem(slot, stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return GachaPresentations.isLocked(source) ? ItemStack.EMPTY : delegate.extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return delegate.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return delegate.isItemValid(slot, stack);
    }
}

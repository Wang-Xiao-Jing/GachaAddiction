package xiaojin.gachaaddiction.client.render;

import net.minecraft.world.level.block.entity.ChestBlockEntity;
import org.jetbrains.annotations.Nullable;

public class GachaChestRenderOverride implements AutoCloseable {
    private static final float NO_OVERRIDE = -1.0F;
    private static final ThreadLocal<ActiveChest> ACTIVE_CHEST = new ThreadLocal<>();
    private final @Nullable ActiveChest previous;

    public GachaChestRenderOverride(ChestBlockEntity chest, @Nullable ChestBlockEntity neighbor, float opening) {
        previous = ACTIVE_CHEST.get();
        ACTIVE_CHEST.set(new ActiveChest(chest, neighbor, opening));
    }

    public static float overrideOpenNess(ChestBlockEntity chest) {
        ActiveChest active = ACTIVE_CHEST.get();
        return active != null && (active.first == chest || active.second == chest) ? active.opening : NO_OVERRIDE;
    }

    public static float overrideRenderedOpenNess() {
        ActiveChest active = ACTIVE_CHEST.get();
        return active == null ? NO_OVERRIDE : active.opening;
    }

    @Override
    public void close() {
        if (previous == null) {
            ACTIVE_CHEST.remove();
        } else {
            ACTIVE_CHEST.set(previous);
        }
    }

    private record ActiveChest(ChestBlockEntity first, @Nullable ChestBlockEntity second, float opening) {
    }
}

package xiaojin.gachaaddiction.client.render;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import org.jetbrains.annotations.Nullable;

public class GachaSceneSources {
    private static final long SOURCE_LIFETIME_MILLIS = 5000;
    private static @Nullable PendingSource pending;

    public static @Nullable CapturedSource takeSource(@Nullable Screen parent) {
        Minecraft minecraft = Minecraft.getInstance();
        PendingSource candidate = pending;
        pending = null;
        if (candidate == null || minecraft.level != candidate.level
                || Util.getMillis() - candidate.createdAt > SOURCE_LIFETIME_MILLIS) {
            return null;
        }

        if (candidate.requiresNewMenu && (!(parent instanceof MenuAccess<?> menu)
                || minecraft.player == null || menu.getMenu() != minecraft.player.containerMenu
                || menu.getMenu().containerId == candidate.previousMenuId)) {
            return null;
        }

        return new CapturedSource(candidate.chest != null && !candidate.chest.isRemoved() ? candidate.chest : null,
                candidate.display);
    }

    public static void rememberReplacement(@Nullable ChestBlockEntity source, ItemStack display) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null && minecraft.player != null) {
            pending = new PendingSource(source, display.copyWithCount(1), minecraft.level,
                    minecraft.player.containerMenu.containerId, false, Util.getMillis());
        }
    }

    public static void rememberSource(BlockPos position) {
        pending = null;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null && minecraft.player != null) {
            ChestBlockEntity chest = minecraft.level.getBlockEntity(position) instanceof ChestBlockEntity holder
                    ? holder : null;
            pending = new PendingSource(chest, new ItemStack(minecraft.level.getBlockState(position).getBlock()),
                    minecraft.level, minecraft.player.containerMenu.containerId, true, Util.getMillis());
        }
    }

    public static void rememberItemSource(ItemStack item) {
        pending = null;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null && minecraft.player != null && !item.isEmpty()) {
            pending = new PendingSource(null, item.copyWithCount(1), minecraft.level,
                    minecraft.player.containerMenu.containerId, false, Util.getMillis());
        }
    }

    public static void rememberEntitySource(Entity entity) {
        pending = null;
        Minecraft minecraft = Minecraft.getInstance();
        ItemStack display = entity.getPickResult();
        if (minecraft.level != null && minecraft.player != null && display != null && !display.isEmpty()) {
            pending = new PendingSource(null, display.copyWithCount(1), minecraft.level,
                    minecraft.player.containerMenu.containerId, true, Util.getMillis());
        }
    }

    public static void clearSource() {
        pending = null;
    }

    private record PendingSource(@Nullable ChestBlockEntity chest, ItemStack display, Level level,
                                 int previousMenuId, boolean requiresNewMenu, long createdAt) {
    }

    public record CapturedSource(@Nullable ChestBlockEntity chest, ItemStack display) {
    }
}

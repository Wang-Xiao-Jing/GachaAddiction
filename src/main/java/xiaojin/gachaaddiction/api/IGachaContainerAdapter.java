package xiaojin.gachaaddiction.api;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.Nullable;

public interface IGachaContainerAdapter {
    boolean supports(MenuProvider provider);

    @Nullable ResourceKey<LootTable> lootTable(MenuProvider provider);

    boolean isOpen(MenuProvider provider);

    void setOpen(MenuProvider provider, boolean open);

    BlockPos announcementPosition(MenuProvider provider);
}

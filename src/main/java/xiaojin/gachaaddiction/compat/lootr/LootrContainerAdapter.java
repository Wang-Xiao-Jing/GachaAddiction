package xiaojin.gachaaddiction.compat.lootr;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.Nullable;
import xiaojin.gachaaddiction.api.IGachaContainerAdapter;
import xiaojin.gachaaddiction.util.LootrUtil;

public class LootrContainerAdapter implements IGachaContainerAdapter {
    @Override
    public boolean supports(MenuProvider provider) {
        return LootrUtil.isInstanceofILootrInventory(provider);
    }

    @Override
    public @Nullable ResourceKey<LootTable> lootTable(MenuProvider provider) {
        return LootrUtil.getInfoLootTable(provider);
    }

    @Override
    public boolean isOpen(MenuProvider provider) {
        return LootrUtil.isOpen(provider);
    }

    @Override
    public void setOpen(MenuProvider provider, boolean open) {
        LootrUtil.setOpen(provider, open);
    }

    @Override
    public BlockPos announcementPosition(MenuProvider provider) {
        return LootrUtil.getAnnouncementPosition(provider);
    }
}

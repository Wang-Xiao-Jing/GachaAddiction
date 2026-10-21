package xiaojin.gachaaddiction.mixed;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.Nullable;

public interface IRandomizableContainerBlockEntity {
    static IRandomizableContainerBlockEntity of(RandomizableContainerBlockEntity randomizableContainerBlockEntity) {
        return (IRandomizableContainerBlockEntity) randomizableContainerBlockEntity;
    }

    boolean gachaaddiction$isOpenGachaaUi();

    void gachaaddiction$setOpenGachaaUi(boolean isOpens);

    @Nullable ResourceKey<LootTable> gachaaddiction$getOldLootTable();
}

package xiaojin.gachaaddiction.mixed;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.Nullable;
import xiaojin.gachaaddiction.api.IGachaSource;
import xiaojin.gachaaddiction.util.TranslationKeys;

import java.util.List;

public interface IMenuProviderLootExtension {
    default @Nullable IGachaSource gachaaddiction$getGachaSource() {
        return null;
    }

    List<@Nullable ResourceKey<LootTable>> gachaaddiction$getLootTable();

    boolean gachaaddiction$isOpenGachaaUi();

    void gachaaddiction$setOpenGachaaUi(boolean isOpen);

    List<@Nullable ResourceKey<LootTable>> gachaaddiction$getOldLootTable();

    default @Nullable BlockPos gachaaddiction$getAnnouncementPosition() {
        return null;
    }

    default Component gachaaddiction$getAnnouncementName() {
        return Component.translatable(TranslationKeys.ANNOUNCEMENT_CONTAINER);
    }

    default @Nullable Component gachaaddiction$getAnnouncementCustomName() {
        return null;
    }
}

package xiaojin.gachaaddiction.mixed;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.Nullable;
import xiaojin.gachaaddiction.GachaAddiction;
import xiaojin.gachaaddiction.util.LootrUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record MenuProviderData(
        List<@Nullable ResourceKey<LootTable>> lootTable,
        boolean isInit
) {
    public MenuProviderData(ResourceKey<LootTable> lootTable, boolean isInit) {
        this(lootTable == null ? List.of() : List.of(lootTable), isInit);
    }

    public MenuProviderData(ResourceKey<LootTable> lootTable) {
        this(lootTable, lootTable == null);
    }

    public static MenuProviderData create(MenuProvider menuProvider) {
        if (GachaAddiction.LOOTR_LOADED && LootrUtil.isInstanceofILootrInventory(menuProvider)) {
            return new MenuProviderData(LootrUtil.getInfoLootTable(menuProvider), LootrUtil.isOpen(menuProvider));
        }

        if (menuProvider instanceof RandomizableContainer randomizableContainer) {
            return new MenuProviderData(randomizableContainer.getLootTable());
        }

        if (menuProvider instanceof ContainerEntity containerEntity) {
            return new MenuProviderData(containerEntity.getLootTable());
        }

        if (menuProvider instanceof IMenuProviderLootExtension iMenuProviderLootExtension) {
            List<@Nullable ResourceKey<LootTable>> list = iMenuProviderLootExtension.gachaaddiction$getLootTable();
            return new MenuProviderData(list, list.isEmpty() || list.stream().anyMatch(Objects::isNull));
        }

        return new MenuProviderData(List.of(), true);
    }

    public static boolean isLootrContainer(MenuProvider menu) {
        if (GachaAddiction.LOOTR_LOADED && LootrUtil.isInstanceofILootrInventory(menu)) {
            return true;
        }
        if (menu instanceof RandomizableContainer) {
            return true;
        }
        if (menu instanceof ContainerEntity) {
            return true;
        }
        if (menu instanceof IMenuProviderLootExtension) {
            return true;
        }
        return false;
    }
}
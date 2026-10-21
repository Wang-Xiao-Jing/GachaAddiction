package xiaojin.gachaaddiction.api;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.vehicle.AbstractMinecartContainer;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.Nullable;
import xiaojin.gachaaddiction.mixed.IAbstractMinecartContainer;
import xiaojin.gachaaddiction.mixed.IMenuProviderLootExtension;
import xiaojin.gachaaddiction.mixed.IRandomizableContainerBlockEntity;
import xiaojin.gachaaddiction.registry.GachaSources;
import xiaojin.gachaaddiction.registry.GachaContainerAdapters;

import java.util.List;
import java.util.Objects;

public record MenuProviderData(
        List<@Nullable ResourceKey<LootTable>> lootTable,
        boolean isOpenGachaaUi
) {
    private MenuProviderData(@Nullable ResourceKey<LootTable> lootTable, boolean isOpenGachaaUi) {
        this(lootTable == null ? List.of() : List.of(lootTable), isOpenGachaaUi);
    }

    private MenuProviderData(@Nullable ResourceKey<LootTable> lootTable) {
        this(lootTable, lootTable != null);
    }

    public static MenuProviderData create(MenuProvider menuProvider) {
        IGachaSource source = GachaSources.find(menuProvider);
        return source == null ? inspectContainer(menuProvider) : source.inspect(menuProvider);
    }

    public static MenuProviderData inspectContainer(MenuProvider menuProvider) {
        IGachaContainerAdapter adapter = GachaContainerAdapters.find(menuProvider);
        if (adapter != null) {
            return new MenuProviderData(adapter.lootTable(menuProvider), !adapter.isOpen(menuProvider));
        }

        if (menuProvider instanceof RandomizableContainerBlockEntity blockEntity) {
            IRandomizableContainerBlockEntity iBlockEntity = IRandomizableContainerBlockEntity.of(blockEntity);
            ResourceKey<LootTable> lootTable = blockEntity.getLootTable();
            if (lootTable == null) {
                lootTable = iBlockEntity.gachaaddiction$getOldLootTable();
            }
            return new MenuProviderData(lootTable, iBlockEntity.gachaaddiction$isOpenGachaaUi());
        }

        if (menuProvider instanceof IMenuProviderLootExtension i) {
            List<@Nullable ResourceKey<LootTable>> lootTable = i.gachaaddiction$getLootTable();
            if (lootTable == null || lootTable.isEmpty() || lootTable.stream().allMatch(Objects::isNull)) {
                lootTable = i.gachaaddiction$getOldLootTable();
            }
            return new MenuProviderData(lootTable, i.gachaaddiction$isOpenGachaaUi());
        }

        if (menuProvider instanceof AbstractMinecartContainer abstractMinecartContainer) {
            ResourceKey<LootTable> lootTable = abstractMinecartContainer.getLootTable();
            IAbstractMinecartContainer iAbstractMinecartContainer = IAbstractMinecartContainer.of(abstractMinecartContainer);
            if (lootTable == null) {
                lootTable = iAbstractMinecartContainer.gachaaddiction$getOldLootTable();
            }
            return new MenuProviderData(lootTable,
                    iAbstractMinecartContainer.gachaaddiction$isOpenGachaaUi());
        }

        if (menuProvider instanceof RandomizableContainer randomizableContainer) {
            return new MenuProviderData(randomizableContainer.getLootTable());
        }

        if (menuProvider instanceof ContainerEntity containerEntity) {
            return new MenuProviderData(containerEntity.getLootTable());
        }

        return new MenuProviderData(List.of(), false);
    }

    public static boolean isLootContainer(MenuProvider menu) {
        return GachaContainerAdapters.find(menu) != null ||
                menu instanceof RandomizableContainer ||
                menu instanceof ContainerEntity ||
                menu instanceof IMenuProviderLootExtension;
    }

    public static void open(MenuProvider menuProvider) {
        IGachaSource source = GachaSources.find(menuProvider);
        if (source == null) {
            openContainer(menuProvider);
        } else {
            source.opened(menuProvider);
        }
    }

    public static void openContainer(MenuProvider menuProvider) {
        IGachaContainerAdapter adapter = GachaContainerAdapters.find(menuProvider);
        if (adapter != null) {
            adapter.setOpen(menuProvider, true);
        }

        if (menuProvider instanceof RandomizableContainerBlockEntity blockEntity) {
            IRandomizableContainerBlockEntity.of(blockEntity).gachaaddiction$setOpenGachaaUi(false);
        }

        if (menuProvider instanceof IMenuProviderLootExtension i) {
            i.gachaaddiction$setOpenGachaaUi(false);
        }

        if (menuProvider instanceof AbstractMinecartContainer randomizableContainer) {
            IAbstractMinecartContainer.of(randomizableContainer).gachaaddiction$setOpenGachaaUi(false);
        }
    }
}
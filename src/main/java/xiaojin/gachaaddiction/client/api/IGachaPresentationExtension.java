package xiaojin.gachaaddiction.client.api;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import xiaojin.gachaaddiction.api.ItemStackEntry;
import xiaojin.gachaaddiction.client.gui.screen.BasicGachaScreen;

import java.util.List;
import java.util.UUID;

public interface IGachaPresentationExtension {
    BasicGachaScreen create(@Nullable Screen parent, List<@Nullable ResourceLocation> tables, List<ItemStackEntry> entries);

    Screen createResult(UUID transactionId, ResourceLocation cardPoolId, ResourceLocation packageId, List<ItemStack> rewards);

    Screen createWorkbench(@Nullable Screen parent);
}

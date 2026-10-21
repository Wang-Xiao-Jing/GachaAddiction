package xiaojin.gachaaddiction.client.compat.ldlib2;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import xiaojin.gachaaddiction.api.ItemStackEntry;
import xiaojin.gachaaddiction.client.api.IGachaPresentationExtension;
import xiaojin.gachaaddiction.client.gui.screen.BasicGachaScreen;

import java.util.List;
import java.util.UUID;

public class LDLib2PresentationExtension implements IGachaPresentationExtension {
    @Override
    public BasicGachaScreen create(@Nullable Screen parent, List<@Nullable ResourceLocation> tables, List<ItemStackEntry> entries) {
        return new LDLib2GachaScreen(parent, tables, entries);
    }

    @Override
    public Screen createResult(UUID transactionId, ResourceLocation cardPoolId, ResourceLocation packageId, List<ItemStack> rewards) {
        return new LDLib2GachaResultScreen(transactionId, cardPoolId, packageId, rewards);
    }

    @Override
    public Screen createWorkbench(@Nullable Screen parent) {
        return LDLib2LocalEditorScreen.create(parent);
    }
}

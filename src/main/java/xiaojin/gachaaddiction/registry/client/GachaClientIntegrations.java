package xiaojin.gachaaddiction.registry.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import xiaojin.gachaaddiction.client.api.IGachaItemBorderRenderer;
import xiaojin.gachaaddiction.client.api.IGachaPresentationExtension;

public class GachaClientIntegrations {
    private static @Nullable IGachaPresentationExtension presentation;
    private static IGachaItemBorderRenderer itemBorders = new IGachaItemBorderRenderer() {
        @Override
        public float updateFlow(float partialTick) {
            return 0.0F;
        }

        @Override
        public boolean render(GuiGraphics graphics, ItemStack stack, int left, int top, int size, float flow) {
            return false;
        }
    };

    public static void registerPresentation(IGachaPresentationExtension extension) {
        presentation = extension;
    }

    public static @Nullable IGachaPresentationExtension presentation() {
        return presentation;
    }

    public static void registerItemBorders(IGachaItemBorderRenderer renderer) {
        itemBorders = renderer;
    }

    public static IGachaItemBorderRenderer itemBorders() {
        return itemBorders;
    }
}

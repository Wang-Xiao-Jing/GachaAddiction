package xiaojin.gachaaddiction.client.api;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

public interface IGachaItemBorderRenderer {
    float updateFlow(float partialTick);

    boolean render(GuiGraphics graphics, ItemStack stack, int left, int top, int size, float flow);
}

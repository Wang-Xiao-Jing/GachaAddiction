package xiaojin.gachaaddiction.mixin.accessor;

import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Screen.class)
public interface ScreenAccessor {
    @Invoker
    <T extends GuiEventListener & Renderable & NarratableEntry> T callAddRenderableWidget(T renderable);

    @Invoker
    <T extends Renderable> T callAddRenderableOnly(T renderable);

    @Invoker
    <T extends GuiEventListener & NarratableEntry> T callAddWidget(T listener);

    @Invoker
    void callRemoveWidget(GuiEventListener listener);
}

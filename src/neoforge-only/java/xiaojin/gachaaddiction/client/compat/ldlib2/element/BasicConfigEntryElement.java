package xiaojin.gachaaddiction.client.compat.ldlib2.element;

import com.lowdragmc.lowdraglib2.editor.resource.IResourcePath;
import com.lowdragmc.lowdraglib2.gui.ColorPattern;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.UITemplateElement;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public abstract class BasicConfigEntryElement<T> extends UITemplateElement {
    private static final float HOVER_VERTICAL_PADDING = 2;

    protected final UIElement contentElement;
    private final Label textElement;
    private final IGuiTexture backgroundTexture;

    protected BasicConfigEntryElement(@Nullable IResourcePath templatePath) {
        super(templatePath);
        layout(layout -> layout
                .paddingTop(HOVER_VERTICAL_PADDING)
                .paddingBottom(HOVER_VERTICAL_PADDING));
        contentElement = select("#content", UIElement.class).findFirst().orElseThrow();
        textElement = select("#text", Label.class).findFirst().orElseThrow();
        backgroundTexture = getStyle().backgroundTexture();
        addEventListener(UIEvents.MOUSE_ENTER,
                event -> event.currentElement.style(style -> style.backgroundTexture(ColorPattern.LIGHT_GRAY.rectTexture())),
                true);
        addEventListener(UIEvents.MOUSE_LEAVE,
                event -> event.currentElement.style(style -> style.backgroundTexture(backgroundTexture)), true);
    }

    @SuppressWarnings("unused")
    public UIElement getContentElement() {
        return contentElement;
    }

    public Label getTextElement() {
        return textElement;
    }

    public BasicConfigEntryElement<T> setText(String text) {
        getTextElement().setText(text);
        return this;
    }

    public BasicConfigEntryElement<T> setText(Component component) {
        getTextElement().setText(component);
        return this;
    }

    public Component getText() {
        return getTextElement().getValue();
    }

    public abstract T getValue();

    public abstract BasicConfigEntryElement<T> setValue(T value);
}

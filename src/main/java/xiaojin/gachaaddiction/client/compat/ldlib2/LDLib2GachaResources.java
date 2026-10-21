package xiaojin.gachaaddiction.client.compat.ldlib2;

import com.lowdragmc.lowdraglib2.Platform;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.UITemplate;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextElement;
import com.lowdragmc.lowdraglib2.gui.ui.style.Stylesheet;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.config.GachaAddictionConfig;
import xiaojin.gachaaddiction.registry.GachaTypes;
import xiaojin.gachaaddiction.util.TranslationKeys;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class LDLib2GachaResources {
    private LDLib2GachaResources() {
    }

    public static UITemplate createDefaultTemplate() {
        return LDLib2GachaDesignerTemplates.createDefault();
    }

    public static UITemplate loadTemplate() {
        ResourceLocation location = ResourceLocation.parse(GachaAddictionConfig.CLIENT.ldlib2GachaTemplate.get());
        var resource = Minecraft.getInstance().getResourceManager().getResource(location);
        if (resource.isEmpty()) {
            if (location.equals(LDLib2GachaUiConstants.DEFAULT_TEMPLATE_ID)) {
                return createDefaultTemplate();
            }
            throw new IllegalStateException(LDLib2GachaUiConstants.TEMPLATE_MISSING + location);
        }
        try (var stream = resource.get().open()) {
            byte[] bytes = stream.readNBytes((int) LDLib2GachaUiConstants.MAXIMUM_TEMPLATE_BYTES + 1);
            if (bytes.length > LDLib2GachaUiConstants.MAXIMUM_TEMPLATE_BYTES) {
                throw new IllegalStateException(LDLib2GachaUiConstants.TEMPLATE_TOO_LARGE + location);
            }
            try (var input = new DataInputStream(new ByteArrayInputStream(bytes))) {
                CompoundTag wrapper = NbtIo.read(input,
                        NbtAccounter.create(LDLib2GachaUiConstants.MAXIMUM_TEMPLATE_BYTES));
                if (input.available() != 0
                        || !wrapper.getString(LDLib2GachaUiConstants.RESOURCE_TYPE_KEY)
                        .equals(LDLib2GachaUiConstants.RESOURCE_UI_TYPE)
                        || !(wrapper.get(LDLib2GachaUiConstants.RESOURCE_DATA_KEY) instanceof CompoundTag encoded)) {
                    throw new IllegalStateException(LDLib2GachaUiConstants.TEMPLATE_INVALID + location);
                }
                LDLib2GachaDesignerTemplates.validateEncoded(encoded);
                UITemplate template = UITemplate.CODEC.parse(
                                Platform.getFrozenRegistry().createSerializationContext(NbtOps.INSTANCE), encoded)
                        .getOrThrow();
                LDLib2GachaDesignerTemplates.validate(template);
                return template;
            }
        } catch (IOException failure) {
            throw new IllegalStateException(LDLib2GachaUiConstants.TEMPLATE_INVALID + location, failure);
        }
    }

    public static View load() {
        UITemplate template = loadTemplate();
        var root = new UIElement();
        template.initUI(root);
        LDLib2GachaDesignerTemplates.makeItemsReadOnly(root);
        if (template.getBuiltinStyles() != null && !template.getBuiltinStyles().isBlank()) {
            root.addLocalStylesheet(Stylesheet.parse(template.getBuiltinStyles()));
        }
        for (ResourceLocation stylesheet : template.getStylesheets()) {
            addStylesheet(root, stylesheet, false);
        }
        addStylesheet(root, ResourceLocation.parse(GachaAddictionConfig.CLIENT.ldlib2GachaStyleSheet.get()), true);
        required(root, LDLib2GachaUiConstants.REWARDS_ID, ScrollerView.class).viewContainer
                .addClass(LDLib2GachaUiConstants.REWARDS_ROW_CLASS);
        return new View(root,
                required(root, LDLib2GachaUiConstants.TITLE_ID, TextElement.class),
                required(root, LDLib2GachaUiConstants.CONTEXT_ID, TextElement.class),
                required(root, LDLib2GachaUiConstants.REWARDS_ID, ScrollerView.class),
                required(root, LDLib2GachaUiConstants.PREVIOUS_ID, Button.class),
                required(root, LDLib2GachaUiConstants.NEXT_ID, Button.class),
                required(root, LDLib2GachaUiConstants.SKIP_ID, Button.class),
                required(root, LDLib2GachaUiConstants.CLOSE_ID, Button.class));
    }

    public static UIElement createPreviewRoot(UIElement simulatedRewards, Runnable replay, Runnable close) {
        View view = load();
        view.title().setText(Component.translatable(GachaTypes.LDLIB2.getTranslationKey()));
        view.context().setText(Component.translatable(TranslationKeys.GUI_GACHA_DESIGNER_SIMULATED));
        view.rewards().clearAllScrollViewChildren();
        view.rewards().addScrollViewChild(simulatedRewards);
        view.previous().setActive(false);
        view.next().setActive(false);
        view.skip().setText(Component.translatable(TranslationKeys.GUI_CONFIG_PREVIEW_REPLAY))
                .setOnClick(event -> replay.run());
        view.close().setText(Component.translatable(TranslationKeys.GUI_CARD_POOL_RESULT_DONE))
                .setOnClick(event -> close.run());
        return view.root();
    }

    private static void addStylesheet(UIElement root, ResourceLocation location, boolean allowDefaultMissing) {
        var resource = Minecraft.getInstance().getResourceManager().getResource(location);
        if (resource.isEmpty()) {
            if (allowDefaultMissing && location.equals(LDLib2GachaUiConstants.DEFAULT_STYLESHEET_ID)) {
                return;
            }
            throw new IllegalStateException(LDLib2GachaUiConstants.STYLESHEET_MISSING + location);
        }
        try (var stream = resource.get().open()) {
            byte[] bytes = stream.readNBytes(LDLib2GachaUiConstants.MAXIMUM_STYLESHEET_BYTES + 1);
            if (bytes.length > LDLib2GachaUiConstants.MAXIMUM_STYLESHEET_BYTES) {
                throw new IllegalStateException(LDLib2GachaUiConstants.STYLESHEET_TOO_LARGE + location);
            }
            String styles = new String(bytes, StandardCharsets.UTF_8);
            LDLib2GachaDesignerTemplates.validateStyles(styles);
            root.addLocalStylesheet(Stylesheet.parse(styles));
        } catch (IOException failure) {
            throw new IllegalStateException(LDLib2GachaUiConstants.STYLESHEET_MISSING + location, failure);
        }
    }

    private static <T extends UIElement> T required(UIElement root, String id, Class<T> type) {
        return root.selectId(id, type).findFirst().orElseThrow(() ->
                new IllegalStateException(LDLib2GachaUiConstants.TEMPLATE_INVALID + id));
    }

    public record View(UIElement root, TextElement title, TextElement context, ScrollerView rewards,
                       Button previous, Button next, Button skip, Button close) {
    }
}

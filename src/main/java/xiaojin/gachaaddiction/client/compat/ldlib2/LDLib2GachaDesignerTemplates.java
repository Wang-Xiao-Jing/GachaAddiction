package xiaojin.gachaaddiction.client.compat.ldlib2;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.UITemplate;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextElement;
import com.lowdragmc.lowdraglib2.gui.ui.style.StylesheetManager;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.FlexWrap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.util.TranslationKeys;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public final class LDLib2GachaDesignerTemplates {
    public static final int MAXIMUM_NODES = 512;
    public static final int MAXIMUM_DEPTH = 32;
    private static final int MAXIMUM_STYLESHEET_CHARACTERS = 65_536;
    private static final int MAXIMUM_STYLESHEETS = 16;
    private static final int DEFAULT_WIDTH = 300;
    private static final int DEFAULT_HEIGHT = 200;
    private static final int GAP = 4;
    private static final String TEMPLATE_KEY = "template";
    private static final String BUILTIN_STYLES_KEY = "builtin_styles";
    private static final String STYLESHEETS_KEY = "stylesheets";
    private static final Set<String> NODE_TYPES = Set.of("element", "text", "label", "button", "scroller-view", "item-slot");
    private static final Set<String> TEXTURE_TYPES = Set.of("empty", "missing", "rect_texture", "sdf_rect_texture",
            "color_rect_texture", "color_border_texture", "sprite_texture", "vanilla_sprite_texture",
            "text_texture", "item_stack_texture", "group_texture", "animation_texture");
    private static final Set<String> STYLE_FUNCTIONS = Set.of("built-in", "color", "border", "sprite", "icon", "rect",
            "sdf", "vanilla-sprite", "group", "scale", "rotation", "rotate", "translate", "rgb", "rgba", "hsl",
            "hsla", "calc", "min", "max", "clamp", "cubic-bezier", "steps", "not", "is", "has", "nth-child", "nth-of-type");
    private static final Pattern STYLE_FUNCTION = Pattern.compile("([a-zA-Z_][a-zA-Z0-9_-]*)\\s*\\(");
    private static final Map<String, String> FIXED_TYPES = Map.of(
            LDLib2GachaUiConstants.TITLE_ID, "text",
            LDLib2GachaUiConstants.CONTEXT_ID, "text",
            LDLib2GachaUiConstants.REWARDS_ID, "scroller-view",
            LDLib2GachaUiConstants.PREVIOUS_ID, "button",
            LDLib2GachaUiConstants.NEXT_ID, "button",
            LDLib2GachaUiConstants.SKIP_ID, "button",
            LDLib2GachaUiConstants.CLOSE_ID, "button");

    private LDLib2GachaDesignerTemplates() {
    }

    public static UITemplate createDefault() {
        var root = new UIElement().addClass("panel_bg");
        root.layout(layout -> layout.width(DEFAULT_WIDTH).height(DEFAULT_HEIGHT).gapAll(GAP));
        var title = new TextElement().setText(Component.translatable(TranslationKeys.GUI_CARD_POOL_RESULT_TITLE));
        title.setId(LDLib2GachaUiConstants.TITLE_ID);
        title.textStyle(style -> style.adaptiveWidth(true).adaptiveHeight(true));
        var context = new TextElement().setText(Component.translatable(TranslationKeys.GUI_GACHA_DESIGNER_SIMULATED));
        context.setId(LDLib2GachaUiConstants.CONTEXT_ID);
        context.textStyle(style -> style.adaptiveWidth(true).adaptiveHeight(true));
        var rewards = new ScrollerView();
        rewards.setId(LDLib2GachaUiConstants.REWARDS_ID);
        LDLib2Layouts.constrainScrollerHeight(rewards, false);
        rewards.layout(layout -> layout.widthPercent(100));
        rewards.viewContainer.layout(layout -> layout.flexDirection(FlexDirection.ROW)
                .wrap(FlexWrap.WRAP).gapAll(GAP));
        var actions = new UIElement();
        actions.layout(layout -> layout.widthPercent(100).flexDirection(FlexDirection.ROW).gapAll(GAP));
        actions.addChildren(button(LDLib2GachaUiConstants.PREVIOUS_ID, TranslationKeys.GUI_CARD_POOL_PREVIOUS),
                button(LDLib2GachaUiConstants.NEXT_ID, TranslationKeys.GUI_CARD_POOL_NEXT),
                button(LDLib2GachaUiConstants.SKIP_ID, TranslationKeys.GUI_GACHA_SKIP),
                button(LDLib2GachaUiConstants.CLOSE_ID, TranslationKeys.GUI_CARD_POOL_RESULT_DONE));
        root.addChildren(title, context, rewards, actions);
        return UITemplate.of(root, StylesheetManager.ORE);
    }

    private static Button button(String id, String key) {
        var button = new Button().setText(Component.translatable(key));
        button.setId(id);
        button.layout(layout -> layout.flex(1).minWidth(0));
        return button;
    }

    public static boolean isFixed(UIElement element) {
        return FIXED_TYPES.containsKey(element.getId());
    }

    public static void validateEncoded(CompoundTag encodedTemplate) {
        if (!encodedTemplate.contains(TEMPLATE_KEY, Tag.TAG_COMPOUND)) {
            throw invalid(ValidationFailure.FORMAT, TEMPLATE_KEY);
        }
        validateData(encodedTemplate.getCompound(TEMPLATE_KEY));
        validateStyles(encodedTemplate.getString(BUILTIN_STYLES_KEY));
        ListTag sheets = encodedTemplate.getList(STYLESHEETS_KEY, Tag.TAG_STRING);
        if (sheets.size() > MAXIMUM_STYLESHEETS) {
            throw invalid(ValidationFailure.LIMIT, STYLESHEETS_KEY);
        }
        for (Tag sheet : sheets) {
            validateStylesheet(ResourceLocation.parse(sheet.getAsString()));
        }
    }

    public static void validate(UITemplate template) {
        validateData(template.getData());
        validateStyles(template.getBuiltinStyles());
        if (template.getStylesheets().size() > MAXIMUM_STYLESHEETS) {
            throw invalid(ValidationFailure.LIMIT, STYLESHEETS_KEY);
        }
        template.getStylesheets().forEach(LDLib2GachaDesignerTemplates::validateStylesheet);
    }

    private static void validateData(CompoundTag data) {
        var ids = new HashMap<String, String>();
        validateNode(data, "element", 0, new int[]{0}, ids);
        FIXED_TYPES.forEach((id, type) -> {
            if (!type.equals(ids.get(id))) {
                throw invalid(ValidationFailure.BINDING, id);
            }
        });
    }

    private static void validateNode(CompoundTag data, String type, int depth, int[] count,
                                     Map<String, String> ids) {
        if (depth > MAXIMUM_DEPTH || ++count[0] > MAXIMUM_NODES) {
            throw invalid(ValidationFailure.LIMIT, type);
        }
        String id = data.getString("id");
        if (!id.isEmpty() && ids.putIfAbsent(id, type) != null) {
            throw invalid(ValidationFailure.DUPLICATE, id);
        }
        if (FIXED_TYPES.containsKey(id) && !FIXED_TYPES.get(id).equals(type)) {
            throw invalid(ValidationFailure.BINDING, id);
        }
        if (type.equals("button") && !isFixedButton(id)) {
            throw invalid(ValidationFailure.BINDING, id);
        }
        CompoundTag values = data.copy();
        values.remove("children");
        values.remove("internal");
        validateStaticValues(values, 0);
        if (data.contains("script") || data.contains("rpc") || data.contains("events") || data.contains("command")) {
            throw invalid(ValidationFailure.TYPE, type);
        }
        ListTag internal = data.getList("internal", Tag.TAG_COMPOUND);
        for (int index = 0; index < internal.size(); index++) {
            validateInternal(internal.getCompound(index), depth + 1, count, ids);
        }
        ListTag children = data.getList("children", Tag.TAG_COMPOUND);
        for (int index = 0; index < children.size(); index++) {
            CompoundTag child = children.getCompound(index);
            String childType = child.getString("type");
            if (!NODE_TYPES.contains(childType)) {
                throw invalid(ValidationFailure.TYPE, childType);
            }
            validateNode(child.getCompound("data"), childType, depth + 1, count, ids);
        }
    }

    private static void validateInternal(CompoundTag data, int depth, int[] count, Map<String, String> ids) {
        if (FIXED_TYPES.containsKey(data.getString("id"))) {
            throw invalid(ValidationFailure.BINDING, "internal");
        }
        validateNode(data, "internal", depth, count, ids);
    }

    private static boolean isFixedButton(String id) {
        return "button".equals(FIXED_TYPES.get(id));
    }

    private static void validateStaticValues(Tag value, int depth) {
        if (depth > MAXIMUM_DEPTH) {
            throw invalid(ValidationFailure.LIMIT, "inline");
        }
        if (value instanceof CompoundTag compound) {
            if (compound.contains("type", Tag.TAG_STRING) && compound.contains("data")
                    && !TEXTURE_TYPES.contains(compound.getString("type"))) {
                throw invalid(ValidationFailure.TYPE, compound.getString("type"));
            }
            for (String key : compound.getAllKeys()) {
                if (key.equals("clickEvent") || key.equals("script") || key.equals("rpc") || key.equals("command")) {
                    throw invalid(ValidationFailure.TYPE, key);
                }
                validateStaticValues(compound.get(key), depth + 1);
            }
        } else if (value instanceof ListTag list) {
            if (list.size() > MAXIMUM_NODES) {
                throw invalid(ValidationFailure.LIMIT, "inline");
            }
            list.forEach(entry -> validateStaticValues(entry, depth + 1));
        }
    }

    public static void validateStyles(String styles) {
        if (styles == null) {
            return;
        }
        if (styles.length() > MAXIMUM_STYLESHEET_CHARACTERS) {
            throw invalid(ValidationFailure.LIMIT, BUILTIN_STYLES_KEY);
        }
        var functions = STYLE_FUNCTION.matcher(styles);
        while (functions.find()) {
            String function = functions.group(1).toLowerCase(Locale.ROOT);
            if (!STYLE_FUNCTIONS.contains(function)) {
                throw invalid(ValidationFailure.TYPE, function);
            }
        }
    }

    private static void validateStylesheet(ResourceLocation id) {
        if (!id.getPath().endsWith(".lss")) {
            throw invalid(ValidationFailure.FORMAT, id.toString());
        }
        var sheet = StylesheetManager.INSTANCE.getStylesheet(id);
        if (sheet != null) {
            validateStyles(sheet.getRawLss());
        }
    }

    public static void makeItemsReadOnly(UIElement root) {
        root.selfAndAllChildren().filter(ItemSlot.class::isInstance).forEach(element -> element.setActive(false));
    }

    private static IllegalArgumentException invalid(ValidationFailure failure, String value) {
        return new IllegalArgumentException(failure.name() + ": " + value);
    }

    private enum ValidationFailure {
        FORMAT, LIMIT, TYPE, BINDING, DUPLICATE
    }
}

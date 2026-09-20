package xiaojin.gachaaddiction.client.compat.ldlib2.element;

import com.lowdragmc.lowdraglib2.gui.ColorPattern;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Dialog;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.elements.UITemplateElement;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.util.WindowDragHelper;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import xiaojin.gachaaddiction.client.compat.ldlib2.LDLib2Layouts;
import xiaojin.gachaaddiction.client.compat.ldlib2.LDLib2Templates;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static xiaojin.gachaaddiction.util.TranslationKeys.GUI_CONFIG_CANCEL;
import static xiaojin.gachaaddiction.util.TranslationKeys.GUI_CONFIG_CONFIRM;
import static xiaojin.gachaaddiction.util.TranslationKeys.GUI_CONFIG_LIST_INVALID;

public class ListConfigScreen<V> extends Dialog {
    private static final float WINDOW_SIZE = 150;

    private final ScrollerView contentElement;
    private final Button confirmButton;
    private final ListNode<V> rootNode = new ListNode<>(null, null);
    private final Set<ListNode<V>> invalidNodes = Collections.newSetFromMap(new IdentityHashMap<>());
    private final IdentityHashMap<ListNode<V>, UIElement> rowElements = new IdentityHashMap<>();
    private final Supplier<V> newValueSupplier;
    private final Function<V, BasicConfigEntryElement<V>> entryFactory;
    private final Predicate<List<V>> valueValidator;
    private final boolean allowEmpty;
    private final Consumer<List<V>> onValueChanged;
    @Nullable
    private ListNode<V> selectedNode;
    @Nullable
    private ListNode<V> dragCandidate;

    public ListConfigScreen(Component title, Collection<? extends V> value,
                             Supplier<V> newValueSupplier,
                             Function<V, BasicConfigEntryElement<V>> entryFactory,
                             Predicate<List<V>> valueValidator,
                             boolean allowEmpty,
                             Consumer<List<V>> onValueChanged,
                             float mouseX, float mouseY) {
        this.newValueSupplier = Objects.requireNonNull(newValueSupplier);
        this.entryFactory = Objects.requireNonNull(entryFactory);
        this.valueValidator = Objects.requireNonNull(valueValidator);
        this.allowEmpty = allowEmpty;
        this.onValueChanged = Objects.requireNonNull(onValueChanged);
        value.forEach(item -> rootNode.children.add(new ListNode<>(rootNode, item)));

        var rootElement = new UITemplateElement(LDLib2Templates.LIST_CONFIG_SCREEN);
        var template = rootElement.getTemplate();
        if (template != null) {
            template.getAllStylesheets().forEach(rootElement::addLocalStylesheet);
        }
        rootElement.layout(layout -> layout.widthPercent(100).heightPercent(100).minHeight(0));

        var titleElement = rootElement.select("#text", Label.class).findFirst().orElseThrow();
        titleElement.setText(title);
        rootElement.select("#close_button", Button.class).findFirst().orElseThrow()
                .setOnClick(event -> close());
        confirmButton = rootElement.select("#confirm_button", Button.class).findFirst().orElseThrow();
        confirmButton.setText(Component.translatable(GUI_CONFIG_CONFIRM));
        confirmButton.setOnClick(this::confirm);
        rootElement.select("#cancel_button", Button.class).findFirst().orElseThrow()
                .setText(Component.translatable(GUI_CONFIG_CANCEL))
                .setOnClick(event -> close());
        rootElement.select("#increase", Button.class).findFirst().orElseThrow()
                .setOnClick(this::addValue);
        rootElement.select("#reduce", Button.class).findFirst().orElseThrow()
                .setOnClick(this::removeSelectedValue);

        contentElement = rootElement.select("#content", ScrollerView.class).findFirst().orElseThrow();
        LDLib2Layouts.constrainScrollerHeight(contentElement, false);
        refreshList();

        overlay.clearAllChildren();
        overlay.addChild(rootElement);
        windowMode(mouseX, mouseY, WINDOW_SIZE, WINDOW_SIZE);
        var titleBarElement = titleElement.getParent();
        if (titleBarElement != null) {
            WindowDragHelper.setDragMove(titleBarElement, overlay, null, null);
        }
        refreshValidity();
    }

    private UIElement createEntryElement(ListNode<V> node) {
        var entry = entryFactory.apply(node.value);
        entry.setValue(node.value);
        entry.layout(layout -> layout.flex(1));
        bindValueChanged(node, entry);
        var row = new UIElement()
                .layout(layout -> layout
                        .widthPercent(100)
                        .flexDirection(FlexDirection.ROW)
                        .alignItems(AlignItems.CENTER))
                .addChildren(entry);
        row.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            if (event.button == 0) {
                dragCandidate = node;
                select(node);
            }
        });
        row.addEventListener(UIEvents.MOUSE_LEAVE, event -> {
            if (dragCandidate == node && row.isMouseDown(0)) {
                var dragHandler = row.startDrag(node, ColorPattern.LIGHT_GRAY.rectTexture());
                float width = row.getSizeWidth();
                float height = row.getSizeHeight();
                dragHandler.setDragTexture(-width / 2, -height / 2, width, height);
            }
            dragCandidate = null;
        }, true);
        row.addEventListener(UIEvents.MOUSE_UP, event -> dragCandidate = null);
        row.addEventListener(UIEvents.DRAG_PERFORM, event -> reorder(event, node));
        rowElements.put(node, row);
        return row;
    }

    private void bindValueChanged(ListNode<V> node, BasicConfigEntryElement<V> entry) {
        switch (entry) {
            case SearchConfigEntryElement search ->
                    search.getTextField().setTextResponder(ignored -> updateSearchValue(node, entry, search));
            case FieldConfigEntryElement<?> field ->
                    field.getValueElement().setTextResponder(ignored -> updateFieldValue(node, entry, field));
            case ListConfigEntryElement<?> list ->
                    list.setOnValueChanged(ignored -> {
                        if (list instanceof FilterRuleConfigEntryElement ruleEntry) {
                            if (ruleEntry.getValue().isEmpty()) {
                                removeNode(node);
                                return;
                            }
                            ruleEntry.refreshValidationState();
                        }
                        updateValue(node, entry);
                    });
            case SelectorConfigEntryElement<?> selector ->
                    selector.setOnValueChanged(ignored -> updateValue(node, entry));
            case SwitchConfigEntryElement switchElement ->
                    switchElement.setOnValueChanged(ignored -> updateValue(node, entry));
            default -> {
            }
        }
    }

    private void updateSearchValue(ListNode<V> node, BasicConfigEntryElement<V> entry,
                                   SearchConfigEntryElement search) {
        if (search instanceof FilterConditionConfigEntryElement conditionEntry) {
            conditionEntry.refreshValidationState();
        }
        if (search.getTextField().isError()) {
            invalidNodes.add(node);
            refreshValidity();
            return;
        }
        updateValue(node, entry);
    }

    private void addValue(UIEvent event) {
        var node = new ListNode<>(rootNode, newValueSupplier.get());
        rootNode.children.add(node);
        refreshList();
        select(node);
        refreshValidity();
    }

    private void removeSelectedValue(UIEvent event) {
        if (selectedNode == null) {
            return;
        }
        removeNode(selectedNode);
    }

    private void removeNode(ListNode<V> node) {
        rootNode.children.remove(node);
        invalidNodes.remove(node);
        selectedNode = null;
        refreshList();
        selectFirstNode();
        refreshValidity();
    }

    private void updateValue(ListNode<V> node, BasicConfigEntryElement<V> entry) {
        V value;
        try {
            value = entry.getValue();
        } catch (RuntimeException ignored) {
            invalidNodes.add(node);
            refreshValidity();
            return;
        }
        invalidNodes.remove(node);
        if (Objects.equals(node.value, value)) {
            refreshValidity();
            return;
        }
        node.value = value;
        refreshValidity();
    }

    private void updateFieldValue(ListNode<V> node, BasicConfigEntryElement<V> entry,
                                  FieldConfigEntryElement<?> field) {
        if (field.getValueElement().isError()) {
            invalidNodes.add(node);
            refreshValidity();
            return;
        }
        updateValue(node, entry);
    }

    private void select(ListNode<V> node) {
        if (selectedNode != null) {
            var previousRow = rowElements.get(selectedNode);
            if (previousRow != null) {
                previousRow.style(style -> style.background(IGuiTexture.EMPTY));
            }
        }
        selectedNode = node;
        var selectedRow = rowElements.get(node);
        if (selectedRow != null) {
            selectedRow.style(style -> style.background(ColorPattern.LIGHT_GRAY.rectTexture()));
        }
    }

    private void reorder(UIEvent event, ListNode<V> target) {
        Object draggedObject = event.dragHandler.getDraggingObject();
        var dragged = rootNode.children.stream()
                .filter(node -> node == draggedObject)
                .findFirst()
                .orElse(null);
        if (dragged == null || dragged == target || target.parent == null) {
            return;
        }
        int targetIndex = rootNode.children.indexOf(target);
        rootNode.children.remove(dragged);
        rootNode.children.add(targetIndex, dragged);
        refreshList();
        select(dragged);
        refreshValidity();
    }

    private void refreshList() {
        contentElement.clearAllScrollViewChildren();
        rowElements.clear();
        for (var node : rootNode.children) {
            contentElement.addScrollViewChild(createEntryElement(node));
        }
    }

    private void confirm(UIEvent event) {
        removeEmptyNodes();
        var value = currentValue();
        if (!invalidNodes.isEmpty() || !isValueValid(value)) {
            return;
        }
        onValueChanged.accept(value);
        close();
    }

    private void refreshValidity() {
        var value = currentValue();
        boolean valid = invalidNodes.isEmpty() && isValueValid(value);
        confirmButton.setActive(valid);
        confirmButton.style(style -> style.tooltips(valid ? new String[0] : new String[]{GUI_CONFIG_LIST_INVALID}));
    }

    private boolean isValueValid(List<V> value) {
        return allowEmpty && value.isEmpty() || valueValidator.test(value);
    }

    private void removeEmptyNodes() {
        var emptyNodes = rootNode.children.stream()
                .filter(node -> isEmptyValue(node.value))
                .toList();
        if (emptyNodes.isEmpty()) {
            return;
        }
        rootNode.children.removeAll(emptyNodes);
        emptyNodes.forEach(invalidNodes::remove);
        selectedNode = null;
        refreshList();
        selectFirstNode();
        refreshValidity();
    }

    private boolean isEmptyValue(@Nullable V value) {
        return value == null
                || value instanceof String string && string.isBlank()
                || value instanceof Collection<?> collection && collection.isEmpty();
    }

    private void selectFirstNode() {
        if (!rootNode.children.isEmpty()) {
            select(rootNode.children.getFirst());
        }
    }

    private List<V> currentValue() {
        return rootNode.children.stream().map(node -> node.value).toList();
    }

    private static class ListNode<V> {
        @Nullable
        private final ListNode<V> parent;
        private final List<ListNode<V>> children = new ArrayList<>();
        @Nullable
        private V value;

        private ListNode(@Nullable ListNode<V> parent, @Nullable V value) {
            this.parent = parent;
            this.value = value;
        }

    }
}

package xiaojin.gachaaddiction.client.compat.ldlib2;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.gui.editor.view.UIEditorView;
import com.lowdragmc.lowdraglib2.gui.editor.view.UITreeNode;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.UITemplate;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextElement;
import com.lowdragmc.lowdraglib2.gui.ui.event.CommandEvents;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import xiaojin.gachaaddiction.util.TranslationKeys;

import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

public final class LDLib2GachaDesignerView extends UIEditorView {
    private static final int DISPLAY_SIZE = 32;
    private static final String DISPLAY_CLASS = "gacha-display";
    private final Consumer<Component> status;
    private UITemplate savedTemplate;
    private String pendingStyles = "";
    private boolean reattaching;
    private Runnable exportAction = () -> {
    };

    public LDLib2GachaDesignerView(Consumer<Component> status) {
        this.status = status;
        setCanRemove(false);
        hierarchy.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            if (event.button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                event.stopImmediatePropagation();
            }
        }, true);
        hierarchy.addEventListener(UIEvents.VALIDATE_COMMAND, this::blockUnsafeClipboard, true);
        hierarchy.addEventListener(UIEvents.EXECUTE_COMMAND, this::blockUnsafeClipboard, true);
        styleView.clearAllChildren();
        var styles = new Button().setText("builtin_styles");
        styles.setOnClick(event -> editBuiltinStyles());
        styleView.addChild(styles);
        stylesheetEditor.setLinesResponder(this::updateStyles);
        saveButton.setText(Component.translatable(TranslationKeys.GUI_GACHA_DESIGNER_EXPORT));
    }

    private void blockUnsafeClipboard(UIEvent event) {
        if (CommandEvents.PASTE.equals(event.command) || CommandEvents.CUT.equals(event.command)
                || CommandEvents.DUPLICATE.equals(event.command)) {
            event.stopImmediatePropagation();
        }
    }

    public void setExportAction(Runnable exportAction) {
        this.exportAction = exportAction;
    }

    @Override
    public UIEditorView loadTemplate(@NotNull UITemplate template, Consumer<UITemplate> onTemplateSaved) {
        var workingUI = getCurrentUI();
        if (reattaching && workingUI != null) {
            modularUIPreview.loadUI(workingUI);
            hierarchy.loadUI(workingUI);
            modularUIPreview.initPreviewSize((int) graphView.getContentWidth(), (int) graphView.getContentHeight());
            return this;
        }
        LDLib2GachaDesignerTemplates.validate(template);
        super.loadTemplate(template, onTemplateSaved);
        pendingStyles = Objects.requireNonNullElse(template.getBuiltinStyles(), "");
        if (!reattaching) {
            savedTemplate = template.copy();
        }
        LDLib2GachaDesignerTemplates.makeItemsReadOnly(Objects.requireNonNull(getCurrentUI()).rootElement);
        return this;
    }

    @Override
    protected void onAdded() {
        reattaching = true;
        try {
            super.onAdded();
        } finally {
            reattaching = false;
        }
    }

    private void updateStyles(String[] lines) {
        pendingStyles = String.join("\n", lines);
        if (getTemplate() == null || Objects.equals(pendingStyles, getTemplate().getBuiltinStyles())) {
            return;
        }
        markAsDirty();
        try {
            LDLib2GachaDesignerTemplates.validateStyles(pendingStyles);
            getTemplate().setBuiltinStyles(pendingStyles);
            var preview = modularUIPreview.getPreviewModularUI();
            if (preview != null) {
                preview.getStyleEngine().clearAllStylesheets();
                preview.getStyleEngine().addStylesheets(getTemplate().getAllStylesheets());
            }
        } catch (RuntimeException invalid) {
            report(invalid);
        }
    }

    private UITemplate snapshotUnchecked() {
        var template = Objects.requireNonNull(getCurrentUI()).toTemplate();
        template.copyStylesFrom(getTemplate());
        return template;
    }

    public UITemplate snapshot() {
        LDLib2GachaDesignerTemplates.validateStyles(pendingStyles);
        var template = snapshotUnchecked();
        template.setBuiltinStyles(pendingStyles);
        LDLib2GachaDesignerTemplates.validate(template);
        return template;
    }

    public void saved(UITemplate template) {
        savedTemplate = template.copy();
        Objects.requireNonNull(getTemplate()).setData(template.getData().copy());
        clearDirty();
    }

    public void restoreTemplate(UITemplate template) {
        var baseline = savedTemplate;
        loadTemplate(template, null);
        savedTemplate = baseline;
        markAsDirty();
    }

    @Override
    public boolean isTemplateDirty() {
        var template = getTemplate();
        if (savedTemplate == null || getCurrentUI() == null || template == null) {
            return false;
        }
        return !getCurrentUI().toTemplate().getData().equals(savedTemplate.getData())
                || !Objects.equals(pendingStyles, Objects.requireNonNullElse(savedTemplate.getBuiltinStyles(), ""))
                || !template.getStylesheets().equals(savedTemplate.getStylesheets());
    }

    @Override
    public void notifySaved() {
        exportAction.run();
    }

    @Override
    protected void onHierarchyNodeSelected(Set<UITreeNode> selected) {
        if (selected.size() == 1 && LDLib2GachaDesignerTemplates.isFixed(selected.iterator().next().getKey())) {
            UIElement element = selected.iterator().next().getKey();
            inspector.inspect(IConfigurable.create(father ->
                    element.getStyles().forEach(style -> style.buildConfigurator(father))));
        } else {
            super.onHierarchyNodeSelected(selected);
        }
    }

    public void addDisplay(UIElement element) {
        if (getCurrentUI() == null || isSimulationRunning()) {
            return;
        }
        UIElement parent = hierarchy.getSelectedOne().orElse(getCurrentUI().rootElement);
        if (LDLib2GachaDesignerTemplates.isFixed(parent) || parent.isInternalUI()) {
            parent = getCurrentUI().rootElement;
        }
        element.addClass(DISPLAY_CLASS);
        parent.addEditorChild(element, -1);
        LDLib2GachaDesignerTemplates.makeItemsReadOnly(element);
        hierarchy.treeList.reloadList();
        focusElement(element);
        markAsDirty();
    }

    public void removeDisplay() {
        var ui = getCurrentUI();
        if (ui == null || isSimulationRunning()) {
            return;
        }
        hierarchy.getSelectedOne().ifPresent(element -> {
            if (element == ui.rootElement || element.isInternalUI()
                    || element.selfAndAllChildren().anyMatch(LDLib2GachaDesignerTemplates::isFixed)) {
                return;
            }
            element.removeSelf();
            hierarchy.treeList.reloadList();
            inspector.clear();
            markAsDirty();
        });
    }

    @Override
    public void startSimulation() {
        if (getCurrentUI() == null) {
            return;
        }
        try {
            var ui = snapshot().createUI();
            var rewards = ui.rootElement.select("#" + LDLib2GachaUiConstants.REWARDS_ID, ScrollerView.class)
                    .findFirst().orElseThrow();
            Runnable showRewards = () -> {
                rewards.clearAllScrollViewChildren();
                for (int index = 0; index < LDLib2GachaUiConstants.REWARDS_PER_PAGE; index++) {
                    var slot = new ItemSlot().setItem(Items.DIAMOND.getDefaultInstance());
                    slot.addClass(LDLib2GachaUiConstants.REWARD_CLASS);
                    slot.layout(layout -> layout.width(DISPLAY_SIZE).height(DISPLAY_SIZE));
                    slot.setActive(false);
                    rewards.addScrollViewChild(slot);
                }
            };
            showRewards.run();
            ui.rootElement.select("#" + LDLib2GachaUiConstants.CONTEXT_ID, TextElement.class).findFirst().orElseThrow()
                    .setText(Component.translatable(TranslationKeys.GUI_GACHA_DESIGNER_SIMULATED));
            for (String id : Set.of(LDLib2GachaUiConstants.PREVIOUS_ID, LDLib2GachaUiConstants.NEXT_ID,
                    LDLib2GachaUiConstants.SKIP_ID)) {
                ui.rootElement.select("#" + id, Button.class).findFirst().orElseThrow()
                        .setOnClick(event -> showRewards.run());
            }
            ui.rootElement.select("#" + LDLib2GachaUiConstants.CLOSE_ID, Button.class).findFirst().orElseThrow()
                    .setOnClick(event -> stopSimulation());
            LDLib2GachaDesignerTemplates.makeItemsReadOnly(ui.rootElement);
            canvas.setDisplay(true);
            editor.setDisplay(false);
            canvas.startSimulation(ui);
        } catch (RuntimeException invalid) {
            report(invalid);
        }
    }

    @Override
    public void screenTick() {
        super.screenTick();
        if (getCurrentUI() != null) {
            LDLib2GachaDesignerTemplates.makeItemsReadOnly(getCurrentUI().rootElement);
        }
        saveButton.setActive(true);
    }

    @Override
    protected Component getViewName() {
        return Component.translatable(TranslationKeys.GUI_GACHA_DESIGNER_TITLE);
    }

    private void report(RuntimeException invalid) {
        status.accept(Component.translatable(TranslationKeys.GUI_GACHA_DESIGNER_INVALID,
                Objects.requireNonNullElse(invalid.getMessage(), invalid.getClass().getSimpleName())));
    }
}

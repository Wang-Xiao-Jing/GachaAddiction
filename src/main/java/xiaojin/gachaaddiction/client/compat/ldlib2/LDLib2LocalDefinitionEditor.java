package xiaojin.gachaaddiction.client.compat.ldlib2;

import com.lowdragmc.lowdraglib2.editor.ui.Editor;
import com.lowdragmc.lowdraglib2.editor.ui.View;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.elements.*;
import com.lowdragmc.lowdraglib2.gui.ui.event.CommandEvents;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.util.TreeNode;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiaojin.gachaaddiction.GachaAddiction;
import xiaojin.gachaaddiction.admin.GachaDefinitionDocumentKey;
import xiaojin.gachaaddiction.client.GachaLocalDocumentState;
import xiaojin.gachaaddiction.client.GachaLocalWorkspace;
import xiaojin.gachaaddiction.data.GachaDefinitionKind;
import xiaojin.gachaaddiction.util.TranslationKeys;

import java.io.File;
import java.io.IOException;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class LDLib2LocalDefinitionEditor extends Editor {
    private static final String RELOAD_COMMAND = "gacha reload";
    private static final ResourceLocation DEFAULT_ID = ResourceLocation.fromNamespaceAndPath(GachaAddiction.MODID, "example");
    private static final String COPY_SUFFIX = "_copy";
    private final GachaLocalDocumentState state;
    private final TreeList<TreeNode<Component, Void>> tree = new TreeList<>();
    private final Map<TreeNode<Component, Void>, GachaDefinitionDocumentKey> nodes = new IdentityHashMap<>();
    private final View documentView = new View(TranslationKeys.GUI_LOCAL_PACK_TITLE);
    private final TextElement status = new TextElement();

    public LDLib2LocalDefinitionEditor(GachaLocalWorkspace workspace) {
        this(workspace, null);
    }

    public LDLib2LocalDefinitionEditor(GachaLocalWorkspace workspace, @Nullable GachaLocalWorkspace.Document document) {
        state = new GachaLocalDocumentState(workspace);
        if (document != null) {
            state.load(document);
        }
        var navigation = new View(TranslationKeys.GUI_ADMIN_WORKBENCH_DOCUMENT);
        navigation.setCanRemove(false);
        navigation.layout(layout -> layout.widthPercent(100).heightPercent(100).minHeight(0));
        tree.layout(layout -> layout.widthPercent(100).heightPercent(100).minHeight(0));
        tree.setFlattenRoot(true).setStaticTree(true).setSupportMultipleSelection(false);
        tree.setNodeUISupplier(node -> new TextElement().setText(node.getKey()));
        tree.setOnSelectedChanged(selected -> {
            var node = selected.stream().findFirst().orElse(null);
            if (node != null && nodes.containsKey(node)) {
                try {
                    screen().openDocument(nodes.get(node));
                } catch (IOException failure) {
                    report(failure);
                }
            }
        });
        navigation.addChild(tree);
        placeView(navigation, () -> leftWindow.getLeftTop());
        documentView.setCanRemove(false);
        placeView(documentView, () -> centerWindow.getLeftTop());
        placeView(inspectorView, () -> rightWindow.getRightTop());
        status.layout(layout -> layout.widthPercent(100).height(LDLib2AdminUiConstants.TOP_BUTTON_HEIGHT));
        status.textStyle(style -> style.adaptiveWidth(false).adaptiveHeight(false));
        topPlaceholder.addChild(status);
        fileMenu.registerNewMenuCreator((tab, menu) -> {
            for (GachaDefinitionKind kind : List.of(GachaDefinitionKind.TABLE, GachaDefinitionKind.REWARD_POOL,
                    GachaDefinitionKind.EXECUTION_PLAN, GachaDefinitionKind.CARD_POOL)) {
                menu.leaf(kind.directory(), () -> showDocumentDialog(kind, DocumentOperation.CREATE));
            }
        });
        fileMenu.registerMenuCreator((tab, menu) -> {
            menu.leaf(TranslationKeys.GUI_ADMIN_WORKBENCH_DOCUMENT_SAVE, () -> saveProject(null));
            var currentDocument = state.document();
            if (currentDocument != null) {
                menu.leaf(TranslationKeys.GUI_ADMIN_WORKBENCH_DOCUMENT_DUPLICATE,
                        () -> showDocumentDialog(currentDocument.key().kind(), DocumentOperation.DUPLICATE));
                menu.leaf(TranslationKeys.GUI_ADMIN_WORKBENCH_DOCUMENT_RENAME,
                        () -> showDocumentDialog(currentDocument.key().kind(), DocumentOperation.RENAME));
                menu.leaf(TranslationKeys.GUI_ADMIN_WORKBENCH_DOCUMENT_DELETE, this::deleteDocument);
            }
            menu.crossLine().leaf(TranslationKeys.GUI_LOCAL_REFRESH, this::reloadLocal)
                    .leaf(TranslationKeys.GUI_LOCAL_OPEN_FOLDER, () -> Util.getPlatform().openPath(workspace.root()))
                    .leaf(TranslationKeys.GUI_LOCAL_EXPORT, this::exportPack)
                    .leaf(TranslationKeys.GUI_GACHA_DESIGNER_OPEN,
                            () -> Minecraft.getInstance().setScreen(LDLib2LocalEditorScreen.openDesigner(
                                    Minecraft.getInstance().screen)));
        });
        var update = new Button().setText(Component.translatable(TranslationKeys.GUI_LOCAL_RELOAD));
        update.setOnClick(event -> {
            var connection = Minecraft.getInstance().getConnection();
            if (connection != null) {
                connection.sendCommand(RELOAD_COMMAND);
            }
        });
        update.addEventListener(UIEvents.TICK, event ->
                update.setActive(Minecraft.getInstance().getConnection() != null));
        buttonContainer.addChildAt(update, buttonContainer.getChildren().indexOf(closeButton));
        refreshNavigation();
        rebuildDocument();
    }

    private static String template(GachaDefinitionKind kind, ResourceLocation id, String reference) {
        return switch (kind) {
            case TABLE -> GachaAdminQuickTemplateFactory.customLootTable(id);
            case REWARD_POOL -> GachaAdminQuickTemplateFactory.rewardPool(id,
                    reference.isBlank() ? null : ResourceLocation.parse(reference));
            case EXECUTION_PLAN -> GachaAdminQuickTemplateFactory.executionPlan(ResourceLocation.parse(reference));
            case CARD_POOL -> GachaAdminQuickTemplateFactory.cardPool(id, ResourceLocation.parse(reference));
            default -> throw new IllegalArgumentException(kind.directory());
        };
    }

    public GachaLocalDocumentState state() {
        return state;
    }

    public UIElement popupHost() {
        var host = resolveMenuHost(null);
        return host == null ? this : host;
    }

    private LDLib2LocalEditorScreen screen() {
        var ui = getModularUI();
        if (ui != null && ui.getScreen() instanceof LDLib2LocalEditorScreen screen) {
            return screen;
        }
        throw new IllegalStateException(getClass().getSimpleName());
    }

    public void refreshFromWorkspace() {
        refreshNavigation();
        var document = state.document();
        if (document != null && !state.hasUnsavedChanges()) {
            try {
                state.load(state.workspace().document(document.key()));
                rebuildDocument();
            } catch (IOException failure) {
                report(failure);
            }
        }
    }

    private void refreshNavigation() {
        nodes.clear();
        var root = new TreeNode<Component, Void>(Component.empty());
        for (GachaDefinitionKind kind : GachaDefinitionKind.values()) {
            TreeNode<Component, Void> group = null;
            for (GachaDefinitionDocumentKey key : state.workspace().documentKeys()) {
                if (key.kind() != kind) {
                    continue;
                }
                if (group == null) {
                    group = root.createChild(Component.literal(kind.directory()));
                }
                var node = group.createChild(Component.literal(key.id().toString()));
                nodes.put(node, key);
            }
        }
        tree.setRoot(root).reloadList();
    }

    private void rebuildDocument() {
        documentView.clearAllChildren();
        if (state.document() == null) {
            var hint = new TextElement().setText(Component.translatable(TranslationKeys.GUI_LOCAL_HINT));
            hint.layout(layout -> layout.widthPercent(100));
            hint.getTextStyle().adaptiveWidth(false).adaptiveHeight(true).textWrap(TextWrap.WRAP);
            documentView.addChild(hint);
        } else {
            documentView.addChild(LDLib2AdminDraftDocumentScreen.createContent(this));
        }
    }

    private void showDocumentDialog(GachaDefinitionKind kind, DocumentOperation operation) {
        if (operation == DocumentOperation.RENAME && screen().hasUnsavedDocuments()) {
            status.setText(Component.translatable(TranslationKeys.GUI_ADMIN_WORKBENCH_DOCUMENT_SAVE_FIRST));
            return;
        }
        var current = state.document();
        var dialog = new Dialog();
        dialog.overlay.layout(layout -> layout.width(LDLib2AdminUiConstants.DIALOG_WIDTH));
        dialog.contentContainer.layout(layout -> layout.widthPercent(100).gapAll(LDLib2AdminUiConstants.CONTENT_GAP));
        dialog.titleBar.addChild(new TextElement().setText(Component.literal(kind.directory())));
        var id = new TextField();
        id.setValue(current == null ? DEFAULT_ID.toString()
                : current.key().id() + COPY_SUFFIX, false);
        var reference = new TextField();
        id.layout(layout -> layout.widthPercent(100));
        reference.layout(layout -> layout.widthPercent(100));
        reference.setValue("", false);
        var error = new TextElement();
        dialog.addContent(new TextElement().setText(Component.translatable(TranslationKeys.GUI_ADMIN_WORKBENCH_QUICK_ID)));
        dialog.addContent(id);
        if (operation == DocumentOperation.CREATE && kind != GachaDefinitionKind.TABLE) {
            dialog.addContent(new TextElement().setText(Component.translatable(TranslationKeys.GUI_ADMIN_JSON_REFERENCE)));
            dialog.addContent(reference);
        }
        dialog.addContent(error);
        dialog.addButton(new Button().setText(Component.translatable(TranslationKeys.GUI_CONFIG_CONFIRM))
                .setOnClick(event -> {
                    try {
                        ResourceLocation target = ResourceLocation.parse(id.getValue());
                        GachaDefinitionDocumentKey key = new GachaDefinitionDocumentKey(kind, target);
                        if (operation == DocumentOperation.RENAME && current != null) {
                            state.workspace().rename(current, target);
                            state.load(state.workspace().document(key));
                        } else {
                            String content = operation == DocumentOperation.DUPLICATE ? state.content()
                                    : template(kind, target, reference.getValue());
                            state.workspace().create(key, content);
                        }
                        dialog.close();
                        screen().refreshDocuments();
                        screen().openDocument(key);
                    } catch (IOException | RuntimeException failure) {
                        error.setText(Component.translatable(TranslationKeys.GUI_LOCAL_OPERATION_FAILED,
                                failure.getMessage()));
                    }
                }));
        dialog.addButton(new Button().setText(Component.translatable(TranslationKeys.GUI_CONFIG_CANCEL))
                .setOnClick(event -> dialog.close()));
        dialog.show(popupHost());
    }

    private void deleteDocument() {
        var document = state.document();
        if (document == null) {
            return;
        }
        askToSaveProject(() -> Dialog.showCheckBox(TranslationKeys.GUI_ADMIN_WORKBENCH_DOCUMENT_DELETE,
                Component.translatable(TranslationKeys.GUI_ADMIN_WORKBENCH_DOCUMENT_DELETE_CONFIRM,
                        document.key().id()), accepted -> {
                    if (accepted) {
                        try {
                            var saved = state.document();
                            var window = getWindow();
                            if (saved == null || window == null) {
                                return;
                            }
                            state.workspace().delete(saved);
                            screen().refreshDocuments();
                            window.removeEditor(this);
                        } catch (IOException failure) {
                            report(failure);
                        }
                    }
                }).show(popupHost()));
    }

    private void reloadLocal() {
        try {
            state.workspace().reload();
            screen().refreshDocuments();
        } catch (IOException | RuntimeException failure) {
            report(failure);
        }
    }

    private void exportPack() {
        if (screen().hasUnsavedDocuments()) {
            status.setText(Component.translatable(TranslationKeys.GUI_ADMIN_WORKBENCH_DOCUMENT_SAVE_FIRST));
            return;
        }
        try {
            state.workspace().reload();
            status.setText(Component.translatable(TranslationKeys.GUI_LOCAL_EXPORTED, state.workspace().export()));
        } catch (IOException | RuntimeException failure) {
            report(failure);
        }
    }

    private void report(Exception failure) {
        status.setText(Component.translatable(TranslationKeys.GUI_LOCAL_OPERATION_FAILED, failure.getMessage()));
    }

    @Override
    protected void onPrepareInspectorView() {
    }

    @Override
    protected void onPrepareHistoryView() {
    }

    @Override
    protected @NotNull Editor createNewEditorInstance() {
        return new LDLib2LocalDefinitionEditor(state.workspace());
    }

    @Override
    public @NotNull Component getTitle() {
        var document = state == null ? null : state.document();
        return document == null ? Component.translatable(TranslationKeys.GUI_LOCAL_PACK_TITLE)
                : Component.literal(document.key().id() + (state.hasUnsavedChanges()
                ? LDLib2AdminUiConstants.CHANGED_MARKER : ""));
    }

    @Override
    public boolean isCurrentProjectDirty() {
        return state != null && state.hasUnsavedChanges();
    }

    @Override
    public void saveProject(@Nullable Runnable onFinish, boolean showNotification) {
        var document = state.document();
        if (document == null || !state.isInputValid()) {
            return;
        }
        try {
            state.load(state.workspace().save(document, state.content()));
            screen().refreshDocuments();
            status.setText(Component.translatable(TranslationKeys.GUI_LOCAL_SAVED));
            if (onFinish != null) {
                onFinish.run();
            }
        } catch (IOException | RuntimeException failure) {
            report(failure);
        }
    }

    @Override
    public void saveAsProject(@Nullable Runnable onFinish) {
        var document = state.document();
        if (document != null) {
            showDocumentDialog(document.key().kind(), DocumentOperation.DUPLICATE);
        }
    }

    @Override
    public void saveAsProject(@Nullable File defaultSaveFile, @Nullable Runnable onFinish) {
        saveAsProject(onFinish);
    }

    @Override
    protected void onValidateCommand(UIEvent event) {
        if (CommandEvents.SAVE.equals(event.command)) {
            event.stopPropagation();
        }
    }

    @Override
    protected void onExecuteCommand(UIEvent event) {
        if (CommandEvents.SAVE.equals(event.command)) {
            event.stopPropagation();
            saveProject(null);
        }
    }

    private enum DocumentOperation {
        CREATE, DUPLICATE, RENAME
    }
}

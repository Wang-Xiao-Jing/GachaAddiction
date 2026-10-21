package xiaojin.gachaaddiction.client.compat.ldlib2;

import com.lowdragmc.lowdraglib2.Platform;
import com.lowdragmc.lowdraglib2.editor.resource.ColorsResource;
import com.lowdragmc.lowdraglib2.editor.resource.Resources;
import com.lowdragmc.lowdraglib2.editor.resource.TexturesResource;
import com.lowdragmc.lowdraglib2.editor.settings.EditorSettings;
import com.lowdragmc.lowdraglib2.editor.ui.Editor;
import com.lowdragmc.lowdraglib2.editor.ui.EditorWindow;
import com.lowdragmc.lowdraglib2.editor.ui.View;
import com.lowdragmc.lowdraglib2.editor.ui.menu.MenuTab;
import com.lowdragmc.lowdraglib2.gui.editor.UIEditor;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.UITemplate;
import com.lowdragmc.lowdraglib2.gui.ui.elements.*;
import com.lowdragmc.lowdraglib2.gui.util.TreeBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiaojin.gachaaddiction.GachaConstants;
import xiaojin.gachaaddiction.util.TranslationKeys;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.function.Consumer;

public final class LDLib2GachaDesignerWorkbench extends UIEditor {
    public static final ResourceLocation WINDOW_ID = ResourceLocation.fromNamespaceAndPath(GachaConstants.MODID, "gacha_designer");
    private static final String SETTINGS_FILE = "gacha-designer.json";
    private static final int GAP = 4;
    private static final int FIELD_HEIGHT = 18;
    private static final int DISPLAY_SIZE = 32;
    private final Consumer<EditorWindow> minimize;
    private final TextElement status;
    private final TextField file;
    private final LDLib2GachaDesignerView designer;
    private final View tools;

    public LDLib2GachaDesignerWorkbench(Consumer<EditorWindow> minimize) {
        this.minimize = minimize;
        status = line(TranslationKeys.GUI_GACHA_DESIGNER_REQUIRES_LDLIB);
        file = new TextField();
        file.setValue(LDLib2GachaDesignerFiles.defaultFileName(), false);
        file.layout(layout -> layout.widthPercent(100).height(FIELD_HEIGHT)).moveInlineAsDefault();
        designer = new LDLib2GachaDesignerView(status::setText);
        designer.setExportAction(() -> saveProject(null));
        tools = new View(TranslationKeys.GUI_GACHA_DESIGNER_FILE);
        tools.setCanRemove(false);
        tools.layout(layout -> layout.widthPercent(100).heightPercent(100).minHeight(0).gapAll(GAP))
                .moveInlineAsDefault();
        tools.addChildren(line(TranslationKeys.GUI_GACHA_DESIGNER_HINT), file, status);
        placeView(designer, () -> centerWindow.getLeftTop());
        placeView(tools, () -> bottomWindow.getLeftBottom());
        loadProject(new LDLib2GachaDesignerProject(), null);
    }

    private static TextElement line(String key) {
        var text = new TextElement().setText(Component.translatable(key));
        text.textStyle(style -> style.adaptiveWidth(true).adaptiveHeight(true));
        return text;
    }

    @Override
    protected @NotNull Editor createNewEditorInstance() {
        return new LDLib2GachaDesignerWorkbench(minimize);
    }

    @Override
    protected void initMenus() {
        var editMenu = new MenuTab(this) {
            @Override
            protected TreeBuilder.Menu createDefaultMenu() {
                return TreeBuilder.Menu.start()
                        .leaf(TranslationKeys.GUI_GACHA_DESIGNER_ADD_PANEL, () -> {
                            var element = new UIElement();
                            element.layout(layout -> layout.width(DISPLAY_SIZE).height(DISPLAY_SIZE));
                            designer.addDisplay(element);
                        })
                        .leaf(TranslationKeys.GUI_GACHA_DESIGNER_ADD_TEXT, () -> designer.addDisplay(new TextElement()
                                .setText(Component.translatable(TranslationKeys.GUI_GACHA_DESIGNER_SIMULATED))))
                        .leaf(TranslationKeys.GUI_GACHA_DESIGNER_ADD_ITEM, () -> designer.addDisplay(new ItemSlot()))
                        .leaf(TranslationKeys.GUI_GACHA_DESIGNER_REMOVE, designer::removeDisplay)
                        .crossLine()
                        .leaf(TranslationKeys.GUI_GACHA_DESIGNER_PREVIEW, () -> {
                            if (designer.isSimulationRunning()) {
                                designer.stopSimulation();
                            } else {
                                designer.startSimulation();
                            }
                        });
            }

            @Override
            protected Component getComponent() {
                return Component.translatable(TranslationKeys.GUI_GACHA_DESIGNER_TITLE);
            }
        };
        menuContainer.addChildren(fileMenu.createMenuTab(), editMenu.createMenuTab(), viewMenu.createMenuTab());
        fileMenu.addProjectProvider(LDLib2GachaDesignerProject.TYPE);
        fileMenu.registerMenuCreator((menuTab, menu) -> {
            menu.leaf(TranslationKeys.GUI_GACHA_DESIGNER_IMPORT, this::importTemplate);
            menu.leaf(TranslationKeys.GUI_GACHA_DESIGNER_EXPORT, () -> saveProject(null));
            menu.leaf(TranslationKeys.GUI_GACHA_DESIGNER_RESET, () -> loadProject(new LDLib2GachaDesignerProject(), null));
        });
    }

    @Override
    protected @NotNull EditorSettings createSettings() {
        return new EditorSettings(this).setSettingsFile(Platform.getGamePath().resolve("config")
                .resolve(GachaConstants.MODID).resolve(SETTINGS_FILE).toFile());
    }

    @Override
    protected void onAdded() {
        super.onAdded();
        var window = getWindow();
        if (window != null && window.windowID != null && !buttonContainer.getChildren().isEmpty()
                && buttonContainer.getChildren().getFirst() instanceof Button button) {
            button.setOnClick(event -> minimize.accept(window));
        }
    }

    public void activateProject(LDLib2GachaDesignerProject project) {
        designer.loadTemplate(project.getTemplate(), null);
        file.setValue(getCurrentProjectFile() == null ? project.getFileName() : getCurrentProjectFile().getName(), false);
        project.attach(this, designer);
        status.setText(Component.translatable(TranslationKeys.GUI_GACHA_DESIGNER_LOADED, file.getValue()));
        placeView(designer, () -> centerWindow.getLeftTop());
        placeView(tools, () -> bottomWindow.getLeftBottom());
    }

    @Override
    protected void closeCurrentProject() {
        super.closeCurrentProject();
        resourceView.clear();
        resourceView.loadResources(Resources.of(ColorsResource.INSTANCE, TexturesResource.INSTANCE));
        designer.removeSelf();
        tools.removeSelf();
        viewFallbacks.remove(designer);
        viewFallbacks.remove(tools);
    }

    @Override
    public boolean isCurrentProjectDirty() {
        return designer != null && getCurrentProject() != null && designer.isTemplateDirty();
    }

    @Override
    public void saveProject(@Nullable Runnable onFinish, boolean showNotification) {
        if (getCurrentProject() == null) {
            return;
        }
        exportTemplate(onFinish);
    }

    @Override
    public void saveAsProject(@Nullable Runnable onFinish) {
        if (getCurrentProject() == null) {
            return;
        }
        try {
            saveAsProject(LDLib2GachaDesignerFiles.resolve(file.getValue()).toFile(), onFinish);
        } catch (IOException invalid) {
            report(invalid);
        }
    }

    @Override
    public void saveAsProject(@Nullable File defaultSaveFile, @Nullable Runnable onFinish) {
        if (getCurrentProject() == null) {
            return;
        }
        try {
            var directory = LDLib2GachaDesignerFiles.resolve(LDLib2GachaDesignerFiles.defaultFileName()).getParent().toFile();
            Dialog.showFileDialog(TranslationKeys.GUI_GACHA_DESIGNER_EXPORT, directory, false, defaultSaveFile,
                    Dialog.suffixFilter(LDLib2GachaDesignerProject.SUFFIX), target -> {
                        if (target == null || target.isDirectory()) {
                            return;
                        }
                        try {
                            if (!target.getName().endsWith(LDLib2GachaDesignerProject.SUFFIX)) {
                                target = new File(target.getParentFile(), target.getName() + LDLib2GachaDesignerProject.SUFFIX);
                            }
                            LDLib2GachaDesignerProject.requireLocalFile(target);
                            file.setValue(target.getName(), false);
                            exportTemplate(onFinish);
                        } catch (IOException invalid) {
                            report(invalid);
                        }
                    }).show(this);
        } catch (IOException invalid) {
            report(invalid);
        }
    }

    private void importTemplate() {
        try {
            String name = file.getValue();
            UITemplate template = LDLib2GachaDesignerFiles.read(name);
            loadProject(new LDLib2GachaDesignerProject(template, name), LDLib2GachaDesignerFiles.resolve(name).toFile());
        } catch (IOException | RuntimeException invalid) {
            report(invalid);
        }
    }

    private void exportTemplate(@Nullable Runnable onFinish) {
        if (getCurrentProject() == null) {
            return;
        }
        try {
            String name = file.getValue();
            UITemplate template = designer.snapshot();
            if (Files.exists(LDLib2GachaDesignerFiles.resolve(name))) {
                Dialog.showCheckBox(TranslationKeys.GUI_GACHA_DESIGNER_TITLE,
                        Component.translatable(TranslationKeys.GUI_GACHA_DESIGNER_OVERWRITE, name), accepted -> {
                            if (accepted) {
                                write(name, template, true, onFinish);
                            }
                        }).show(this);
            } else {
                write(name, template, false, onFinish);
            }
        } catch (IOException | RuntimeException invalid) {
            report(invalid);
        }
    }

    private void write(String name, UITemplate template, boolean overwrite, @Nullable Runnable onFinish) {
        try {
            LDLib2GachaDesignerFiles.write(name, template, overwrite);
            designer.saved(template);
            currentProjectFile = LDLib2GachaDesignerFiles.resolve(name).toFile();
            recordRecentProject();
            status.setText(Component.translatable(TranslationKeys.GUI_GACHA_DESIGNER_SAVED, name));
            if (onFinish != null) {
                onFinish.run();
            }
        } catch (IOException | RuntimeException invalid) {
            report(invalid);
        }
    }

    public void report(Exception invalid) {
        status.setText(Component.translatable(TranslationKeys.GUI_GACHA_DESIGNER_INVALID,
                Objects.requireNonNullElse(invalid.getMessage(), invalid.getClass().getSimpleName())));
    }

}

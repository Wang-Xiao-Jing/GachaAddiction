package xiaojin.gachaaddiction.client.compat.ldlib2;

import com.lowdragmc.lowdraglib2.editor.ui.Editor;
import com.lowdragmc.lowdraglib2.editor.ui.EditorWindow;
import com.lowdragmc.lowdraglib2.gui.holder.ModularUIScreen;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.style.StylesheetManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import xiaojin.gachaaddiction.admin.GachaDefinitionDocumentKey;
import xiaojin.gachaaddiction.client.workspace.GachaLocalWorkspace;
import xiaojin.gachaaddiction.util.TranslationKeys;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.function.Supplier;

public class LDLib2LocalEditorScreen extends ModularUIScreen {
    private static @Nullable LDLib2LocalEditorScreen active;
    private final @Nullable Screen parent;
    private final GachaLocalWorkspace workspace;
    private final LocalWindow window;
    private boolean minimized;

    private LDLib2LocalEditorScreen(@Nullable Screen parent, GachaLocalWorkspace workspace, LocalWindow window) {
        super(ModularUI.of(UI.of(window, StylesheetManager.ORE))
                        .shouldCloseOnEsc(false).shouldCloseOnKeyInventory(false),
                Component.translatable(TranslationKeys.GUI_LOCAL_PACK_TITLE));
        this.parent = parent;
        this.workspace = workspace;
        this.window = window;
        window.screen = this;
    }

    public static LDLib2LocalEditorScreen create(@Nullable Screen parent) {
        var screen = active;
        if (screen == null) {
            try {
                GachaLocalWorkspace workspace = GachaLocalWorkspace.open();
                screen = new LDLib2LocalEditorScreen(parent, workspace,
                        new LocalWindow(() -> new LDLib2LocalDefinitionEditor(workspace)));
                active = screen;
            } catch (IOException failure) {
                throw new UncheckedIOException(failure);
            }
        } else if (screen.minimized) {
            var restored = screen;
            EditorWindow.open(LDLib2AdminUiConstants.WINDOW_ID,
                    () -> new LDLib2LocalDefinitionEditor(restored.workspace));
            screen.minimized = false;
        }
        return screen;
    }

    public static Screen openDesigner(@Nullable Screen parent) {
        LDLib2LocalEditorScreen screen = create(parent);
        Editor designer = screen.window.getEditors().keySet().stream()
                .filter(editor -> editor instanceof LDLib2GachaDesignerWorkbench).findFirst().orElse(null);
        if (designer == null) {
            designer = screen.window.createNewEditor(() -> new LDLib2GachaDesignerWorkbench(window -> screen.minimize()));
        }
        screen.window.showEditor(designer);
        return screen;
    }

    public static @Nullable Screen parentOf(Screen screen) {
        return screen instanceof LDLib2LocalEditorScreen local ? local.parent : screen;
    }

    public static void minimizeIfActive(Screen screen) {
        if (screen instanceof LDLib2LocalEditorScreen local && Minecraft.getInstance().screen == screen) {
            local.minimize();
        }
    }

    public void openDocument(GachaDefinitionDocumentKey key) throws IOException {
        for (Editor editor : window.getEditors().keySet()) {
            if (editor instanceof LDLib2LocalDefinitionEditor local) {
                var document = local.state().document();
                if (document != null && document.key().equals(key)) {
                    window.showEditor(editor);
                    return;
                }
            }
        }
        var document = workspace.document(key);
        window.createNewEditor(() -> new LDLib2LocalDefinitionEditor(workspace, document));
    }

    public boolean hasUnsavedDocuments() {
        return window.getEditors().keySet().stream()
                .anyMatch(editor -> editor instanceof LDLib2LocalDefinitionEditor && editor.isCurrentProjectDirty());
    }

    public void refreshDocuments() {
        for (Editor editor : List.copyOf(window.getEditors().keySet())) {
            if (editor instanceof LDLib2LocalDefinitionEditor local) {
                local.refreshFromWorkspace();
            }
        }
    }

    private void minimize() {
        minimized = true;
        window.minimizeWindow();
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public void onClose() {
        window.closeWindow();
    }

    private static class LocalWindow extends EditorWindow {
        private @Nullable LDLib2LocalEditorScreen screen;

        private LocalWindow(Supplier<Editor> creator) {
            super(LDLib2AdminUiConstants.WINDOW_ID, creator);
        }

        @Override
        public void removeEditor(Editor editor) {
            super.removeEditor(editor);
            if (getEditors().isEmpty() && screen != null) {
                active = null;
                Minecraft.getInstance().setScreen(screen.parent);
            }
        }
    }
}

package xiaojin.gachaaddiction.client.compat.ldlib2;

import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextElement;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import net.minecraft.network.chat.Component;
import xiaojin.gachaaddiction.client.GachaLocalDocumentState;
import xiaojin.gachaaddiction.client.GachaLocalWorkspace;
import xiaojin.gachaaddiction.util.TranslationKeys;

public final class LDLib2AdminDraftDocumentScreen {
    private LDLib2AdminDraftDocumentScreen() {
    }

    public static ScrollerView createContent(LDLib2LocalDefinitionEditor editor) {
        GachaLocalDocumentState state = editor.state();
        var document = state.document();
        var content = new ScrollerView();
        content.layout(layout -> layout.widthPercent(100).heightPercent(100).minHeight(0));
        LDLib2Layouts.constrainScrollerHeight(content);
        content.viewContainer.layout(layout -> layout.heightPercent(100).minHeight(0));
        if (document == null) return content;

        var inspector = new LDLib2AdminJsonInspector(editor);
        var textArea = new LDLib2AdminJsonCodeEditor();
        String[] displayedContent = {state.content()};
        boolean[] programmaticUpdate = {true};
        textArea.setTextValidator(lines -> {
            String raw = String.join(LDLib2AdminUiConstants.LINE_SEPARATOR, lines);
            if (!programmaticUpdate[0]) {
                displayedContent[0] = raw;
                inspector.acceptRawContent(raw);
            }
            return raw.length() <= GachaLocalWorkspace.MAXIMUM_DOCUMENT_CHARACTERS;
        });
        textArea.setValue(state.content().split(LDLib2AdminUiConstants.LINE_SEPARATOR, -1), false);
        programmaticUpdate[0] = false;
        textArea.setActive(state.canEditDocument());
        textArea.layout(layout -> layout.widthPercent(100).flex(1).minHeight(0));
        content.addScrollViewChild(textArea);
        var status = new TextElement().setText(Component.empty());
        status.getTextStyle().adaptiveWidth(true).adaptiveHeight(true);
        content.addScrollViewChild(status);
        var error = new TextElement().setText(Component.empty());
        error.getTextStyle().adaptiveWidth(true).adaptiveHeight(true)
                .textColor(LDLib2AdminUiConstants.ERROR_COLOR);
        content.addScrollViewChild(error);
        inspector.refresh();
        content.addEventListener(UIEvents.TICK, event -> {
            if (!displayedContent[0].equals(state.content())) {
                displayedContent[0] = state.content();
                programmaticUpdate[0] = true;
                try {
                    textArea.setValue(state.content().split(LDLib2AdminUiConstants.LINE_SEPARATOR, -1), false);
                } finally {
                    programmaticUpdate[0] = false;
                }
            }
            inspector.refresh();
            textArea.setActive(state.canEditDocument());
            status.setText(Component.translatable(state.canEditDocument()
                    ? TranslationKeys.GUI_ADMIN_WORKBENCH_DOCUMENT_EDITABLE
                    : TranslationKeys.GUI_ADMIN_WORKBENCH_DOCUMENT_READ_ONLY));
            error.setText(inspector.error());
        });
        return content;
    }

}

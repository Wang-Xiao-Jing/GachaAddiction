package xiaojin.gachaaddiction.client.workspace;

import org.jetbrains.annotations.Nullable;

public class GachaLocalDocumentState {
    private final GachaLocalWorkspace workspace;
    private @Nullable GachaLocalWorkspace.Document document;
    private String content = "";
    private boolean inputValid = true;

    public GachaLocalDocumentState(GachaLocalWorkspace workspace) {
        this.workspace = workspace;
    }

    public GachaLocalWorkspace workspace() {
        return workspace;
    }

    public @Nullable GachaLocalWorkspace.Document document() {
        return document;
    }

    public void load(GachaLocalWorkspace.Document value) {
        document = value;
        content = value.content();
        inputValid = true;
    }

    public String content() {
        return content;
    }

    public void setContent(String value) {
        content = value;
    }

    public boolean canEditDocument() {
        return document != null;
    }

    public boolean hasUnsavedChanges() {
        return document != null && (!inputValid || !content.equals(document.content()));
    }

    public void setInputValid(boolean value) {
        inputValid = value;
    }

    public boolean isInputValid() {
        return inputValid;
    }
}

package xiaojin.gachaaddiction.client.compat.ldlib2;

import com.lowdragmc.lowdraglib2.editor.project.IProject;
import com.lowdragmc.lowdraglib2.editor.project.ProjectType;
import com.lowdragmc.lowdraglib2.editor.resource.*;
import com.lowdragmc.lowdraglib2.editor.ui.Editor;
import com.lowdragmc.lowdraglib2.gui.ui.UITemplate;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.NotNull;
import xiaojin.gachaaddiction.util.TranslationKeys;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public final class LDLib2GachaDesignerProject implements IProject {
    public static final String SUFFIX = ".ui.nbt";
    public static final ProjectType TYPE = new ProjectType(TranslationKeys.GUI_GACHA_DESIGNER_TITLE,
            SUFFIX, LDLib2GachaDesignerProject::new) {
        @Override
        public IProject loadProjectFromFile(File file) throws IOException {
            requireLocalFile(file);
            return new LDLib2GachaDesignerProject(LDLib2GachaDesignerFiles.read(file.getName()), file.getName());
        }

        @Override
        public void saveProjectToFile(IProject project, File file) throws IOException {
            requireLocalFile(file);
            LDLib2GachaDesignerFiles.write(file.getName(), ((LDLib2GachaDesignerProject) project).snapshot(), false);
        }

        @Override
        public File getRootSavePath(IProject project, File projectRoot) {
            try {
                return LDLib2GachaDesignerFiles.resolve(LDLib2GachaDesignerFiles.defaultFileName()).getParent().toFile();
            } catch (IOException invalid) {
                throw new IllegalArgumentException(invalid);
            }
        }

        @Override
        public File getDefaultSaveFile(IProject project, File projectRoot) {
            try {
                return LDLib2GachaDesignerFiles.resolve(((LDLib2GachaDesignerProject) project).getFileName()).toFile();
            } catch (IOException invalid) {
                throw new IllegalArgumentException(invalid);
            }
        }
    };
    private final String fileName;
    private final LDLib2GachaDesignerResource resource = new LDLib2GachaDesignerResource(this);
    private UITemplate template;
    private LDLib2GachaDesignerWorkbench workbench;
    private LDLib2GachaDesignerView view;

    public LDLib2GachaDesignerProject() {
        this(LDLib2GachaDesignerTemplates.createDefault(), LDLib2GachaDesignerFiles.defaultFileName());
    }

    public LDLib2GachaDesignerProject(UITemplate template, String fileName) {
        LDLib2GachaDesignerTemplates.validate(template);
        this.template = template.copy();
        this.fileName = fileName;
    }

    public static void requireLocalFile(File file) throws IOException {
        if (!file.toPath().toAbsolutePath().normalize().equals(LDLib2GachaDesignerFiles.resolve(file.getName()))) {
            throw new IOException(FileFailure.PATH.name());
        }
    }

    public UITemplate getTemplate() {
        return template.copy();
    }

    public String getFileName() {
        return fileName;
    }

    public void attach(LDLib2GachaDesignerWorkbench workbench, LDLib2GachaDesignerView view) {
        this.workbench = workbench;
        this.view = view;
    }

    private UITemplate snapshot() {
        return view == null ? template.copy() : view.snapshot();
    }

    @Override
    public @NotNull Resources getResources() {
        return Resources.of(resource, ColorsResource.INSTANCE, TexturesResource.INSTANCE);
    }

    @Override
    public @NotNull ProjectType getProjectType() {
        return TYPE;
    }

    @Override
    public @NotNull CompoundTag serializeProject(@NotNull HolderLookup.Provider provider) {
        return (CompoundTag) Objects.requireNonNull(UIResource.INSTANCE.serializeResource(snapshot(), provider));
    }

    @Override
    public void deserializeProject(@NotNull HolderLookup.Provider provider, @NotNull CompoundTag data) {
        LDLib2GachaDesignerTemplates.validateEncoded(data);
        template = Objects.requireNonNull(UIResource.INSTANCE.deserializeResource(data, provider));
        LDLib2GachaDesignerTemplates.validate(template);
        if (view != null) {
            view.restoreTemplate(template);
        }
    }

    @Override
    public @NotNull CompoundTag serializeNBT(@NotNull HolderLookup.Provider provider) {
        var data = new CompoundTag();
        data.putString(LDLib2GachaUiConstants.RESOURCE_TYPE_KEY, LDLib2GachaUiConstants.RESOURCE_UI_TYPE);
        data.put(LDLib2GachaUiConstants.RESOURCE_DATA_KEY, serializeProject(provider));
        return data;
    }

    @Override
    public void deserializeNBT(@NotNull HolderLookup.Provider provider, @NotNull CompoundTag data) {
        if (!LDLib2GachaUiConstants.RESOURCE_UI_TYPE.equals(data.getString(LDLib2GachaUiConstants.RESOURCE_TYPE_KEY))
                || !data.contains(LDLib2GachaUiConstants.RESOURCE_DATA_KEY, Tag.TAG_COMPOUND)) {
            throw new IllegalArgumentException(FileFailure.FORMAT.name());
        }
        deserializeProject(provider, data.getCompound(LDLib2GachaUiConstants.RESOURCE_DATA_KEY));
    }

    @Override
    public void onLoad(@NotNull Editor editor) {
        if (editor instanceof LDLib2GachaDesignerWorkbench designerWorkbench) {
            designerWorkbench.activateProject(this);
        }
    }

    @Override
    public void onClosed(@NotNull Editor editor) {
        if (view != null) {
            view.stopSimulation();
        }
        view = null;
        workbench = null;
    }

    public void openResource(UITemplate value, IResourcePath path) {
        if (workbench == null || value == null) {
            return;
        }
        String name = LDLib2GachaDesignerFiles.defaultFileName();
        File source = null;
        if (path instanceof FilePath filePath) {
            name = filePath.getFile().getName();
            try {
                requireLocalFile(filePath.getFile());
                value = LDLib2GachaDesignerFiles.read(name);
                source = filePath.getFile();
            } catch (IOException | RuntimeException invalid) {
                report(invalid);
                return;
            }
        }
        workbench.loadProject(new LDLib2GachaDesignerProject(value, name), source);
    }

    public void report(Exception invalid) {
        if (workbench != null) {
            workbench.report(invalid);
        }
    }

    private enum FileFailure {
        PATH, FORMAT
    }
}

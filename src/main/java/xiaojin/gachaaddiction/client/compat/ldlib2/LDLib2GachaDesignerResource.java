package xiaojin.gachaaddiction.client.compat.ldlib2;

import com.lowdragmc.lowdraglib2.editor.resource.IResourceProvider;
import com.lowdragmc.lowdraglib2.editor.resource.UIResource;
import com.lowdragmc.lowdraglib2.editor.ui.resource.ResourceProviderContainer;
import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.UITemplate;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class LDLib2GachaDesignerResource extends UIResource {
    private final LDLib2GachaDesignerProject project;

    public LDLib2GachaDesignerResource(LDLib2GachaDesignerProject project) {
        this.project = project;
    }

    @Override
    public @NotNull Tag serializeResource(UITemplate value, HolderLookup.Provider provider) {
        LDLib2GachaDesignerTemplates.validate(value);
        Tag data = super.serializeResource(value, provider);
        if (!(data instanceof CompoundTag encoded)) {
            throw new IllegalArgumentException(ValidationFailure.FORMAT.name());
        }
        LDLib2GachaDesignerTemplates.validateEncoded(encoded);
        return data;
    }

    @Override
    public @Nullable UITemplate deserializeResource(Tag data, HolderLookup.Provider provider) {
        try {
            if (!(data instanceof CompoundTag encoded)) {
                throw new IllegalArgumentException(ValidationFailure.FORMAT.name());
            }
            LDLib2GachaDesignerTemplates.validateEncoded(encoded);
            UITemplate template = Objects.requireNonNull(super.deserializeResource(data, provider));
            LDLib2GachaDesignerTemplates.validate(template);
            return template;
        } catch (RuntimeException invalid) {
            project.report(invalid);
            return null;
        }
    }

    @Override
    public @NotNull ResourceProviderContainer<UITemplate> createResourceProviderContainer(IResourceProvider<UITemplate> provider) {
        return new ResourceProviderContainer<>(provider)
                .setCanOpen(path -> true)
                .setAddDefault(LDLib2GachaDesignerTemplates::createDefault)
                .setUiSupplier(path -> new UIElement().layout(layout -> layout.widthPercent(100).heightPercent(100))
                        .style(style -> style.backgroundTexture(Icons.WIDGET_BASIC)))
                .setOnEdit((container, path) -> project.openResource(provider.getResource(path), path));
    }

    private enum ValidationFailure {
        FORMAT
    }
}

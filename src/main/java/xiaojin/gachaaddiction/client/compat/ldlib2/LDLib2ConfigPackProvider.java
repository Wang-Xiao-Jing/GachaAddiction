package xiaojin.gachaaddiction.client.compat.ldlib2;

import com.lowdragmc.lowdraglib2.Platform;
import com.lowdragmc.lowdraglib2.editor.resource.*;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import xiaojin.gachaaddiction.GachaAddiction;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class LDLib2ConfigPackProvider<T> extends ResourceProvider<T> {
    private static final String LOCAL_ROOT = "ldlib2/assets/gachaaddiction";
    private static final String PACK_ROOT = "ui/";
    private static final String TYPE_KEY = "type";
    private static final String DATA_KEY = "data";
    private static final String READ_FAILURE_LOG = "Could not read packaged configuration resource {}";
    private static final int MAXIMUM_RESOURCE_BYTES = 1_048_576;
    private static final Set<String> UI_FILES = Set.of("config_screen.ui.nbt", "basic_config_entry.ui.nbt",
            "field_config_entry.ui.nbt", "list_config_entry.ui.nbt", "list_config_screen.ui.nbt",
            "search_config_entry.ui.nbt", "selector_config_entry.ui.nbt", "switch_config_entry.ui.nbt");
    private static final Set<String> TEXTURE_FILES = Set.of("reset.texture.nbt");

    private final Set<String> fileNames;

    private LDLib2ConfigPackProvider(ResourceInstance<T> resourceInstance, Set<String> fileNames) {
        super(resourceInstance);
        this.fileNames = fileNames;
    }

    public static void register() {
        register(UIResource.INSTANCE, UI_FILES);
        register(TexturesResource.INSTANCE, TEXTURE_FILES);
    }

    private static <T> void register(Resource<T> resource, Set<String> fileNames) {
        var resources = resource.getResourceInstance();
        var local = new FileResourceProvider<>(resources, Platform.getGamePath().resolve(LOCAL_ROOT).toFile());
        local.setName(GachaAddiction.MODID);
        resources.addBuiltinProvider(local);
        resources.addBuiltinProvider(new LDLib2ConfigPackProvider<>(resources, fileNames));
    }

    public static void prepare() {
        prepare(UIResource.INSTANCE.getResourceInstance());
        prepare(TexturesResource.INSTANCE.getResourceInstance());
    }

    private static void prepare(ResourceInstance<?> resources) {
        var localDirectory = Platform.getGamePath().resolve(LOCAL_ROOT).toFile();
        resources.getBuiltinProviders().getOrDefault(FileResourceProvider.TYPE, List.of()).stream()
                .filter(provider -> provider instanceof FileResourceProvider<?> fileProvider
                        && fileProvider.resourceLocation.equals(localDirectory))
                .forEach(IResourceProvider::checkAndUpdateResourceProvider);
        resources.clearCache();
    }

    private static ResourceLocation packLocation(FilePath path) {
        String resourcePath = Objects.requireNonNull(path.location).getPath();
        return ResourceLocation.fromNamespaceAndPath(GachaAddiction.MODID,
                resourcePath.startsWith(PACK_ROOT) ? resourcePath : PACK_ROOT + resourcePath);
    }

    @Override
    public String getName() {
        return GachaAddiction.MODID;
    }

    @Override
    public ResourceProviderType getType() {
        return FileResourceProvider.TYPE;
    }

    @Override
    public boolean supportResourcePath(IResourcePath path) {
        if (!(path instanceof FilePath filePath) || filePath.location == null
                || !GachaAddiction.MODID.equals(filePath.location.getNamespace())) {
            return false;
        }
        String resourcePath = filePath.location.getPath();
        return fileNames.contains(resourcePath)
                || resourcePath.startsWith(PACK_ROOT) && fileNames.contains(resourcePath.substring(PACK_ROOT.length()));
    }

    @Override
    public IResourcePath createSubPath(String name) {
        return new FilePath(ResourceLocation.fromNamespaceAndPath(GachaAddiction.MODID,
                PACK_ROOT + name + resourceInstance.resource.getFileExtension()));
    }

    @Override
    public boolean hasResource(IResourcePath path) {
        return supportResourcePath(path) && Minecraft.getInstance().getResourceManager()
                .getResource(packLocation((FilePath) path)).isPresent();
    }

    @Override
    @Nullable
    public T getResource(IResourcePath path) {
        if (!supportResourcePath(path)) {
            return null;
        }
        var location = packLocation((FilePath) path);
        var resource = Minecraft.getInstance().getResourceManager().getResource(location);
        if (resource.isEmpty()) {
            return null;
        }
        try (var stream = resource.get().open()) {
            byte[] bytes = stream.readNBytes(MAXIMUM_RESOURCE_BYTES + 1);
            if (bytes.length > MAXIMUM_RESOURCE_BYTES) {
                return null;
            }
            try (var input = new DataInputStream(new ByteArrayInputStream(bytes))) {
                var wrapper = NbtIo.read(input, NbtAccounter.create(MAXIMUM_RESOURCE_BYTES));
                if (input.available() != 0 || !wrapper.getString(TYPE_KEY).equals(resourceInstance.resource.getName())
                        || !wrapper.contains(DATA_KEY)) {
                    return null;
                }
                return resourceInstance.resource.deserializeResource(wrapper.get(DATA_KEY), Platform.getFrozenRegistry());
            }
        } catch (IOException | RuntimeException failure) {
            GachaAddiction.LOGGER.warn(READ_FAILURE_LOG, location, failure);
            return null;
        }
    }

    @Override
    public boolean supportAdd() {
        return false;
    }

    @Override
    public boolean addResource(IResourcePath path, T resource) {
        return false;
    }

    @Override
    @Nullable
    public T removeResource(IResourcePath path) {
        return null;
    }

    @Override
    public boolean canRemove(IResourcePath path) {
        return false;
    }

    @Override
    public boolean canRename(IResourcePath path) {
        return false;
    }

    @Override
    public boolean canEdit(IResourcePath path) {
        return false;
    }
}

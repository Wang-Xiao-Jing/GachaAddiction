package xiaojin.gachaaddiction.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterTypeRegistry;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class GachaDefinitionReloadListener extends SimpleJsonResourceReloadListener {
    public static final String ROOT_DIRECTORY = "gacha";
    public static final String FORMAT_VERSION_KEY = "format_version";
    public static final int CURRENT_FORMAT_VERSION = 1;
    private final GachaRuntimeDefinitionCompiler compiler;
    private final GachaDefinitionRepository repository;

    public GachaDefinitionReloadListener(HolderLookup.Provider registries, GachaParameterTypeRegistry parameterTypes,
                                         GachaDefinitionRepository repository) {
        super(new Gson(), ROOT_DIRECTORY);
        parameterTypes.freeze();
        this.compiler = new GachaRuntimeDefinitionCompiler(registries, parameterTypes);
        this.repository = Objects.requireNonNull(repository, "repository");
    }

    private static void parse(ResourceLocation resourceId, JsonElement json,
                              Map<GachaDefinitionKind, Map<ResourceLocation, GachaRawDefinition>> parsed) {
        String path = resourceId.getPath();
        int separator = path.indexOf('/');
        if (separator <= 0 || separator == path.length() - 1) {
            throw new JsonParseException("Invalid gacha definition path: " + resourceId);
        }
        String directory = path.substring(0, separator);
        GachaDefinitionKind kind = GachaDefinitionKind.fromDirectory(directory).orElseThrow(() ->
                new JsonParseException("Unknown gacha definition directory: " + directory));
        if (!json.isJsonObject()) {
            throw new JsonParseException("Gacha definition root must be an object: " + resourceId);
        }
        JsonObject object = json.getAsJsonObject();
        if (!object.has(FORMAT_VERSION_KEY) || !object.get(FORMAT_VERSION_KEY).isJsonPrimitive()) {
            throw new JsonParseException("Gacha definition is missing format_version: " + resourceId);
        }
        int formatVersion = GachaJson.integer(object, FORMAT_VERSION_KEY, resourceId.toString());
        if (formatVersion != CURRENT_FORMAT_VERSION) {
            throw new JsonParseException("Unsupported gacha definition format version " + formatVersion + ": "
                    + resourceId);
        }
        ResourceLocation definitionId = ResourceLocation.fromNamespaceAndPath(resourceId.getNamespace(),
                path.substring(separator + 1));
        Map<ResourceLocation, GachaRawDefinition> definitions = parsed.computeIfAbsent(kind,
                ignored -> new LinkedHashMap<>());
        if (definitions.putIfAbsent(definitionId,
                new GachaRawDefinition(definitionId, formatVersion, object)) != null) {
            throw new JsonParseException("Duplicate gacha definition: " + definitionId);
        }
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resources, ResourceManager resourceManager,
                         ProfilerFiller profiler) {
        Map<GachaDefinitionKind, Map<ResourceLocation, GachaRawDefinition>> parsed =
                new EnumMap<>(GachaDefinitionKind.class);
        resources.forEach((resourceId, json) -> parse(resourceId, json, parsed));
        GachaRawDefinitionSnapshot raw = new GachaRawDefinitionSnapshot(parsed);
        GachaCompiledDefinitionSnapshot compiled = compiler.compile(raw);
        repository.publish(raw, compiled);
    }
}

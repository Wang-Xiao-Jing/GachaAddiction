package xiaojin.gachaaddiction.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.neoforged.fml.loading.FMLPaths;
import xiaojin.gachaaddiction.admin.GachaDefinitionDocumentKey;
import xiaojin.gachaaddiction.admin.GachaDraftDocumentRenamer;
import xiaojin.gachaaddiction.data.GachaDefinitionKind;
import xiaojin.gachaaddiction.data.GachaDefinitionReloadListener;
import xiaojin.gachaaddiction.util.TranslationKeys;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class GachaLocalWorkspace {
    public static final int MAXIMUM_DOCUMENT_CHARACTERS = 1_048_576;
    private static final int MAXIMUM_DOCUMENT_BYTES = MAXIMUM_DOCUMENT_CHARACTERS * 4;
    private static final String LOCAL_DIRECTORY = "gachaaddiction/local_datapack";
    private static final String EXPORT_NAME = "gachaaddiction_local.zip";
    private static final String DATA_DIRECTORY = "data";
    private static final String PACK_METADATA = "pack.mcmeta";
    private static final String DOCUMENT_SUFFIX = ".json";
    private static final String TEMPORARY_PREFIX = ".gacha-local-";
    private static final String TEMPORARY_SUFFIX = ".tmp";
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private final Path root;
    private final Map<GachaDefinitionDocumentKey, String> documents = new TreeMap<>();
    private final Map<GachaDefinitionKind, List<String>> candidates = new EnumMap<>(GachaDefinitionKind.class);

    private GachaLocalWorkspace(Path root) {
        this.root = root;
    }

    public static GachaLocalWorkspace open() throws IOException {
        Path root = FMLPaths.CONFIGDIR.get().toAbsolutePath().normalize().resolve(LOCAL_DIRECTORY);
        GachaLocalWorkspace workspace = new GachaLocalWorkspace(root);
        workspace.requireLocalPath(root);
        Files.createDirectories(root.resolve(DATA_DIRECTORY));
        if (!Files.exists(root.resolve(PACK_METADATA))) {
            JsonObject pack = new JsonObject();
            pack.addProperty("pack_format", SharedConstants.getCurrentVersion().getPackVersion(PackType.SERVER_DATA));
            pack.addProperty("description", Component.translatable(TranslationKeys.GUI_LOCAL_PACK_TITLE).getString());
            JsonObject metadata = new JsonObject();
            metadata.add("pack", pack);
            workspace.write(root.resolve(PACK_METADATA), JSON.toJson(metadata));
        }
        workspace.reload();
        return workspace;
    }

    private static void replace(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException unsupported) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void validate(String content) throws IOException {
        if (content.length() > MAXIMUM_DOCUMENT_CHARACTERS) {
            throw failure(TranslationKeys.GUI_LOCAL_FILE_TOO_LARGE);
        }
        var parsed = JsonParser.parseString(content);
        if (!parsed.isJsonObject()) {
            throw failure(TranslationKeys.GUI_ADMIN_JSON_INVALID_VALUE);
        }
    }

    private static IOException failure(String key) {
        return new IOException(Component.translatable(key).getString());
    }

    public Path root() {
        return root;
    }

    public List<GachaDefinitionDocumentKey> documentKeys() {
        return List.copyOf(documents.keySet());
    }

    public List<String> candidates(GachaDefinitionKind kind) {
        return candidates.getOrDefault(kind, List.of());
    }

    public void reload() throws IOException {
        Map<GachaDefinitionDocumentKey, String> loaded = new TreeMap<>();
        Path data = root.resolve(DATA_DIRECTORY);
        try (var paths = Files.walk(data)) {
            for (Path file : paths.filter(path -> Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)).toList()) {
                Path relative = data.relativize(file);
                if (relative.getNameCount() < 4
                        || !relative.getName(1).toString().equals(GachaDefinitionReloadListener.ROOT_DIRECTORY)
                        || !file.getFileName().toString().endsWith(DOCUMENT_SUFFIX)) {
                    continue;
                }
                GachaDefinitionKind kind = GachaDefinitionKind.fromDirectory(relative.getName(2).toString()).orElse(null);
                if (kind == null) {
                    continue;
                }
                String path = relative.subpath(3, relative.getNameCount()).toString().replace('\\', '/');
                ResourceLocation id = ResourceLocation.tryBuild(relative.getName(0).toString(),
                        path.substring(0, path.length() - DOCUMENT_SUFFIX.length()));
                if (id == null) {
                    throw failure(TranslationKeys.GUI_LOCAL_INVALID_PATH);
                }
                GachaDefinitionDocumentKey key = new GachaDefinitionDocumentKey(kind, id);
                loaded.put(key, read(path(key)));
            }
        }
        documents.clear();
        documents.putAll(loaded);
        indexCandidates();
    }

    public Document document(GachaDefinitionDocumentKey key) throws IOException {
        String content = read(path(key));
        documents.put(key, content);
        indexCandidates();
        return new Document(key, content);
    }

    public Document create(GachaDefinitionDocumentKey key, String content) throws IOException {
        if (Files.exists(path(key))) {
            throw failure(TranslationKeys.GUI_LOCAL_FILE_EXISTS);
        }
        return save(new Document(key, ""), content);
    }

    public Document save(Document previous, String content) throws IOException {
        validate(content);
        Path target = path(previous.key());
        if (Files.exists(target) ? !read(target).equals(previous.content()) : !previous.content().isEmpty()) {
            throw failure(TranslationKeys.GUI_LOCAL_FILE_CHANGED);
        }
        write(target, content);
        documents.put(previous.key(), content);
        indexCandidates();
        return new Document(previous.key(), content);
    }

    public void delete(Document previous) throws IOException {
        Path target = path(previous.key());
        if (!read(target).equals(previous.content())) {
            throw failure(TranslationKeys.GUI_LOCAL_FILE_CHANGED);
        }
        Files.delete(target);
        documents.remove(previous.key());
        indexCandidates();
    }

    public void rename(Document previous, ResourceLocation id) throws IOException {
        GachaDefinitionDocumentKey targetKey = new GachaDefinitionDocumentKey(previous.key().kind(), id);
        if (targetKey.equals(previous.key())) {
            return;
        }
        if (Files.exists(path(targetKey))) {
            throw failure(TranslationKeys.GUI_LOCAL_FILE_EXISTS);
        }
        for (var entry : documents.entrySet()) {
            if (!read(path(entry.getKey())).equals(entry.getValue())) {
                throw failure(TranslationKeys.GUI_LOCAL_FILE_CHANGED);
            }
        }
        Map<GachaDefinitionDocumentKey, String> renamed = GachaDraftDocumentRenamer.rename(documents, previous.key(), id);
        for (var entry : renamed.entrySet()) {
            if (!entry.getValue().equals(documents.get(entry.getKey()))) {
                validate(entry.getValue());
                write(path(entry.getKey()), entry.getValue());
            }
        }
        // 新文件和引用均落盘后才移除旧文件；失败时保留已写出的文件供本地处理。
        Files.delete(path(previous.key()));
        documents.clear();
        documents.putAll(renamed);
        indexCandidates();
    }

    public Path export() throws IOException {
        for (String content : documents.values()) {
            validate(content);
        }
        Path target = root.getParent().resolve(EXPORT_NAME);
        Path temporary = Files.createTempFile(root.getParent(), TEMPORARY_PREFIX, TEMPORARY_SUFFIX);
        try {
            try (ZipOutputStream output = new ZipOutputStream(Files.newOutputStream(temporary));
                 var paths = Files.walk(root)) {
                for (Path file : paths.filter(path -> Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS))
                        .sorted().toList()) {
                    requireLocalPath(file);
                    String name = root.relativize(file).toString().replace('\\', '/');
                    if (name.endsWith(TEMPORARY_SUFFIX)) {
                        continue;
                    }
                    output.putNextEntry(new ZipEntry(name));
                    Files.copy(file, output);
                    output.closeEntry();
                }
            }
            replace(temporary, target);
        } finally {
            Files.deleteIfExists(temporary);
        }
        return target;
    }

    private Path path(GachaDefinitionDocumentKey key) throws IOException {
        for (String segment : key.id().getPath().split("/", -1)) {
            if (segment.isBlank() || segment.equals(".") || segment.equals("..")) {
                throw failure(TranslationKeys.GUI_LOCAL_INVALID_PATH);
            }
        }
        Path target = root.resolve(DATA_DIRECTORY).resolve(key.id().getNamespace())
                .resolve(GachaDefinitionReloadListener.ROOT_DIRECTORY).resolve(key.kind().directory())
                .resolve(key.id().getPath() + DOCUMENT_SUFFIX).normalize();
        requireLocalPath(target);
        return target;
    }

    private void requireLocalPath(Path path) throws IOException {
        if (!path.toAbsolutePath().normalize().startsWith(root)) {
            throw failure(TranslationKeys.GUI_LOCAL_INVALID_PATH);
        }
        for (Path current = path; current != null; current = current.getParent()) {
            if (Files.isSymbolicLink(current)) {
                throw failure(TranslationKeys.GUI_LOCAL_INVALID_PATH);
            }
        }
    }

    private String read(Path path) throws IOException {
        requireLocalPath(path);
        if (Files.size(path) > MAXIMUM_DOCUMENT_BYTES) {
            throw failure(TranslationKeys.GUI_LOCAL_FILE_TOO_LARGE);
        }
        String content = Files.readString(path, StandardCharsets.UTF_8);
        if (content.length() > MAXIMUM_DOCUMENT_CHARACTERS) {
            throw failure(TranslationKeys.GUI_LOCAL_FILE_TOO_LARGE);
        }
        return content;
    }

    private void write(Path target, String content) throws IOException {
        requireLocalPath(target);
        Files.createDirectories(target.getParent());
        Path temporary = Files.createTempFile(target.getParent(), TEMPORARY_PREFIX, TEMPORARY_SUFFIX);
        try {
            Files.writeString(temporary, content, StandardCharsets.UTF_8);
            replace(temporary, target);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private void indexCandidates() {
        for (GachaDefinitionKind kind : GachaDefinitionKind.values()) {
            candidates.put(kind, documents.keySet().stream().filter(key -> key.kind() == kind)
                    .map(key -> key.id().toString()).toList());
        }
    }

    public record Document(GachaDefinitionDocumentKey key, String content) {
    }
}

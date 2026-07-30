package xiaojin.gachaaddiction.config;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import xiaojin.gachaaddiction.GachaAddiction;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * 配置规格：收集所有 {@link ConfigValue}，并负责 JSON 的读写。
 * <p>
 * NeoForge 的 ModConfigSpec 在 Fabric 上没有对应实现，这里用一个最小的替代品，
 * 保持 {@code define/push/pop} 与 {@code ConfigValue.get()} 的调用方式不变。
 */
public final class ConfigSpec {
    private final String fileName;
    private final List<ConfigValue<?>> values = new ArrayList<>();
    private final List<String> path = new ArrayList<>();

    public ConfigSpec(String fileName) {
        this.fileName = fileName;
    }

    void register(ConfigValue<?> value) {
        values.add(value);
    }

    List<String> currentPath() {
        return path;
    }

    void push(String... keys) {
        path.addAll(List.of(keys));
    }

    void pop(int count) {
        for (int i = 0; i < count && !path.isEmpty(); i++) {
            path.removeLast();
        }
    }

    private Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve(fileName + ".json");
    }

    public void load() {
        Path file = file();
        if (!Files.exists(file)) {
            save();
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            for (ConfigValue<?> value : values) {
                JsonElement element = resolve(root, value.getPath());
                if (element != null) {
                    value.fromJson(element);
                }
            }
        } catch (Exception e) {
            GachaAddiction.LOGGER.warn("无法读取配置文件 {}，将使用默认值: {}", file, e.getMessage());
        }
        // 补齐新增的配置项，并剔除已失效的条目
        save();
    }

    public void save() {
        JsonObject root = new JsonObject();
        for (ConfigValue<?> value : values) {
            List<String> valuePath = value.getPath();
            JsonObject parent = root;
            for (int i = 0; i < valuePath.size() - 1; i++) {
                String key = valuePath.get(i);
                if (!parent.has(key) || !parent.get(key).isJsonObject()) {
                    parent.add(key, new JsonObject());
                }
                parent = parent.getAsJsonObject(key);
            }
            String leaf = valuePath.getLast();
            String comment = value.getComment();
            if (comment != null && !comment.isEmpty()) {
                // JSON 没有注释，用 "//键名" 兄弟条目承载说明，读取时忽略
                parent.addProperty("//" + leaf, comment);
            }
            parent.add(leaf, value.toJson());
        }

        Path file = file();
        try {
            Files.createDirectories(file.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(file)) {
                new GsonBuilder().setPrettyPrinting().create().toJson(root, writer);
            }
        } catch (Exception e) {
            GachaAddiction.LOGGER.error("无法写入配置文件 {}: {}", file, e.getMessage());
        }
    }

    private static JsonElement resolve(JsonObject root, List<String> path) {
        JsonElement current = root;
        for (String key : path) {
            if (current == null || !current.isJsonObject()) {
                return null;
            }
            current = current.getAsJsonObject().get(key);
        }
        return current;
    }
}

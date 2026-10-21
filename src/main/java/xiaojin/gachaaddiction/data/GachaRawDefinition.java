package xiaojin.gachaaddiction.data;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public record GachaRawDefinition(ResourceLocation id, int formatVersion, JsonObject data) {
    public GachaRawDefinition(ResourceLocation id, int formatVersion, JsonObject data) {
        this.id = Objects.requireNonNull(id, "id");
        if (formatVersion <= 0) {
            throw new IllegalArgumentException("Definition format version must be positive");
        }
        this.formatVersion = formatVersion;
        this.data = Objects.requireNonNull(data, "data").deepCopy();
    }

    @Override
    public JsonObject data() {
        return data.deepCopy();
    }
}

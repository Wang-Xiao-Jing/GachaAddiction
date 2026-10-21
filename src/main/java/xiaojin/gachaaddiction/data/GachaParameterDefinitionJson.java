package xiaojin.gachaaddiction.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.reward.parameter.*;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class GachaParameterDefinitionJson {
    private static final Set<String> FIELDS = Set.of("name", "type", "direction", "required", "default");

    private final IGachaParameterTypeResolver parameterTypes;
    private final RegistryOps<JsonElement> ops;

    public GachaParameterDefinitionJson(IGachaParameterTypeResolver parameterTypes,
                                        HolderLookup.Provider registries) {
        this.parameterTypes = Objects.requireNonNull(parameterTypes, "parameterTypes");
        this.ops = RegistryOps.create(JsonOps.INSTANCE, Objects.requireNonNull(registries, "registries"));
    }

    public GachaParameterDefinition<?> decode(ResourceLocation owner, JsonObject json, String location) {
        GachaJson.requireOnly(json, FIELDS, location);
        GachaParameterType<?> type = parameterTypes.resolve(
                ResourceLocation.parse(GachaJson.string(json, "type", location))).orElseThrow(() ->
                new JsonParseException("Unknown parameter type in " + location));
        return decodeTyped(owner, json, location, type);
    }

    public <T> T decodeValue(GachaParameterType<T> type, JsonElement json, String location) {
        return type.codec().parse(ops, json).getOrThrow(message ->
                new JsonParseException("Invalid parameter value at " + location + ": " + message));
    }

    @SuppressWarnings("unchecked")
    public <T> GachaParameterType<T> requireType(ResourceLocation id) {
        return (GachaParameterType<T>) parameterTypes.resolve(Objects.requireNonNull(id, "id")).orElseThrow(() ->
                new JsonParseException("Unknown parameter type: " + id));
    }

    private <T> GachaParameterDefinition<T> decodeTyped(ResourceLocation owner, JsonObject json,
                                                        String location, GachaParameterType<T> type) {
        Optional<T> defaultValue = json.has("default")
                ? Optional.of(decodeValue(type, json.get("default"), location + ".default"))
                : Optional.empty();
        return new GachaParameterDefinition<>(new GachaParameterKey(owner,
                GachaJson.string(json, "name", location)), type,
                GachaParameterDirection.valueOf(GachaJson.string(json, "direction", location)),
                GachaJson.bool(json, "required", false), defaultValue);
    }
}

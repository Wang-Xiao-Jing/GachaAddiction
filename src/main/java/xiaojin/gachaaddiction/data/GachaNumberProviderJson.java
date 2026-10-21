package xiaojin.gachaaddiction.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.reward.number.*;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterKey;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterView;

import java.util.Set;

public final class GachaNumberProviderJson {
    private static final String TYPE_KEY = "type";

    private GachaNumberProviderJson() {
    }

    public static GachaDoubleProvider doubleProvider(JsonElement json, String location) {
        if (json == null || json.isJsonNull()) {
            throw new JsonParseException("Double provider is missing in " + location);
        }
        if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isNumber()) {
            return new GachaConstantDoubleProvider(json.getAsDouble());
        }
        if (!json.isJsonObject()) {
            throw new JsonParseException("Double provider must be a number or object in " + location);
        }
        JsonObject object = json.getAsJsonObject();
        String type = GachaJson.string(object, TYPE_KEY, location);
        return switch (type) {
            case "constant" -> {
                GachaJson.requireOnly(object, Set.of(TYPE_KEY, "value"), location);
                yield new GachaConstantDoubleProvider(doubleValue(object, location));
            }
            case "parameter" -> {
                GachaJson.requireOnly(object, Set.of(TYPE_KEY, "owner", "name", "view"), location);
                yield new GachaNamedDoubleProvider(parameterKey(object, location), view(object, location));
            }
            case "binary" -> {
                GachaJson.requireOnly(object, Set.of(TYPE_KEY, "left", "operation", "right"), location);
                yield new GachaBinaryDoubleProvider(doubleProvider(object.get("left"), location + ".left"),
                        GachaDoubleOperation.valueOf(GachaJson.string(object, "operation", location)),
                        doubleProvider(object.get("right"), location + ".right"));
            }
            case "clamp" -> {
                GachaJson.requireOnly(object, Set.of(TYPE_KEY, "value", "minimum", "maximum"), location);
                yield new GachaClampDoubleProvider(doubleProvider(object.get("value"), location + ".value"),
                        doubleProvider(object.get("minimum"), location + ".minimum"),
                        doubleProvider(object.get("maximum"), location + ".maximum"));
            }
            default -> throw new JsonParseException("Unknown double provider type '" + type + "' in " + location);
        };
    }

    public static GachaLongProvider longProvider(JsonElement json, String location) {
        if (json == null || json.isJsonNull()) {
            throw new JsonParseException("Long provider is missing in " + location);
        }
        if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isNumber()) {
            return new GachaConstantLongProvider(GachaJson.longNumber(json, location));
        }
        if (!json.isJsonObject()) {
            throw new JsonParseException("Long provider must be an integer or object in " + location);
        }
        JsonObject object = json.getAsJsonObject();
        String type = GachaJson.string(object, TYPE_KEY, location);
        return switch (type) {
            case "constant" -> {
                GachaJson.requireOnly(object, Set.of(TYPE_KEY, "value"), location);
                yield new GachaConstantLongProvider(longValue(object, location));
            }
            case "parameter" -> {
                GachaJson.requireOnly(object, Set.of(TYPE_KEY, "owner", "name", "view"), location);
                yield new GachaNamedLongProvider(parameterKey(object, location), view(object, location));
            }
            default -> throw new JsonParseException("Unknown long provider type '" + type + "' in " + location);
        };
    }

    private static GachaParameterKey parameterKey(JsonObject object, String location) {
        return new GachaParameterKey(ResourceLocation.parse(GachaJson.string(object, "owner", location)),
                GachaJson.string(object, "name", location));
    }

    private static GachaParameterView view(JsonObject object, String location) {
        return object.has("view") ? GachaParameterView.valueOf(GachaJson.string(object, "view", location))
                : GachaParameterView.CURRENT;
    }

    private static double doubleValue(JsonObject object, String location) {
        JsonElement value = object.get("value");
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
            throw new JsonParseException("Field 'value' must be a number in " + location);
        }
        double decoded = value.getAsDouble();
        if (!Double.isFinite(decoded)) {
            throw new JsonParseException("Field 'value' must be finite in " + location);
        }
        return decoded;
    }

    private static long longValue(JsonObject object, String location) {
        JsonElement value = object.get("value");
        return GachaJson.longNumber(value, location + ".value");
    }
}

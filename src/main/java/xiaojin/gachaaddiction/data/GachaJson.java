package xiaojin.gachaaddiction.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.util.Set;

public final class GachaJson {
    private GachaJson() {
    }

    public static void requireOnly(JsonObject object, Set<String> allowed, String location) {
        for (String field : object.keySet()) {
            if (!allowed.contains(field)) {
                throw new JsonParseException("Unknown field '" + field + "' in " + location);
            }
        }
    }

    public static JsonObject object(JsonObject parent, String field, String location) {
        JsonElement value = require(parent, field, location);
        if (!value.isJsonObject()) {
            throw new JsonParseException("Field '" + field + "' must be an object in " + location);
        }
        return value.getAsJsonObject();
    }

    public static JsonArray array(JsonObject parent, String field, String location) {
        JsonElement value = require(parent, field, location);
        if (!value.isJsonArray()) {
            throw new JsonParseException("Field '" + field + "' must be an array in " + location);
        }
        return value.getAsJsonArray();
    }

    public static String string(JsonObject parent, String field, String location) {
        JsonElement value = require(parent, field, location);
        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
            throw new JsonParseException("Field '" + field + "' must be a string in " + location);
        }
        return value.getAsString();
    }

    public static int integer(JsonObject parent, String field, String location) {
        JsonElement value = require(parent, field, location);
        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
            throw new JsonParseException("Field '" + field + "' must be an integer in " + location);
        }
        try {
            return value.getAsBigDecimal().intValueExact();
        } catch (ArithmeticException | NumberFormatException failure) {
            throw new JsonParseException("Field '" + field + "' must be an integer in " + location, failure);
        }
    }

    public static long longNumber(JsonElement value, String location) {
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
            throw new JsonParseException("Field must be an integer in " + location);
        }
        try {
            return value.getAsBigDecimal().longValueExact();
        } catch (ArithmeticException | NumberFormatException failure) {
            throw new JsonParseException("Field must be an integer in " + location, failure);
        }
    }

    public static boolean bool(JsonObject parent, String field, boolean fallback) {
        return parent.has(field) ? parent.get(field).getAsBoolean() : fallback;
    }

    private static JsonElement require(JsonObject parent, String field, String location) {
        if (!parent.has(field) || parent.get(field).isJsonNull()) {
            throw new JsonParseException("Missing field '" + field + "' in " + location);
        }
        return parent.get(field);
    }
}

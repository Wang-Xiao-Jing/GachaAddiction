package xiaojin.gachaaddiction.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.execution.*;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterKey;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class GachaParameterMappingJson {
    private static final Set<String> ENTRY_FIELDS = Set.of("target", "source", "constant", "number");
    private static final Set<String> CONSTANT_FIELDS = Set.of("type", "value");
    private static final Set<String> NUMBER_FIELDS = Set.of("type", "value");

    private GachaParameterMappingJson() {
    }

    public static GachaParameterMapping decode(GachaParameterDefinitionJson parameters,
                                               ResourceLocation sourceOwner, ResourceLocation targetOwner,
                                               JsonArray encoded, String location) {
        List<GachaParameterMappingEntry> mappings = new ArrayList<>();
        for (int index = 0; index < encoded.size(); index++) {
            String entryLocation = location + "[" + index + "]";
            JsonElement element = encoded.get(index);
            if (!element.isJsonObject()) {
                throw new JsonParseException("Parameter mapping must be an object at " + entryLocation);
            }
            JsonObject entry = element.getAsJsonObject();
            GachaJson.requireOnly(entry, ENTRY_FIELDS, entryLocation);
            GachaParameterKey target = new GachaParameterKey(targetOwner,
                    GachaJson.string(entry, "target", entryLocation));
            mappings.add(new GachaParameterMappingEntry(target,
                    value(parameters, sourceOwner, entry, entryLocation)));
        }
        return mappings.isEmpty() ? GachaParameterMapping.EMPTY : new GachaParameterMapping(mappings);
    }

    private static GachaParameterValue value(GachaParameterDefinitionJson parameters,
                                             ResourceLocation sourceOwner, JsonObject entry, String location) {
        int valueKinds = (entry.has("source") ? 1 : 0) + (entry.has("constant") ? 1 : 0)
                + (entry.has("number") ? 1 : 0);
        if (valueKinds != 1) {
            throw new JsonParseException("Parameter mapping must declare exactly one value at " + location);
        }
        if (entry.has("source")) {
            return new GachaDirectParameterValue(new GachaParameterKey(sourceOwner,
                    GachaJson.string(entry, "source", location)));
        }
        if (entry.has("constant")) {
            JsonObject constant = GachaJson.object(entry, "constant", location);
            GachaJson.requireOnly(constant, CONSTANT_FIELDS, location + ".constant");
            ResourceLocation type = ResourceLocation.parse(GachaJson.string(constant, "type", location + ".constant"));
            return constant(parameters, type, constant.get("value"), location + ".constant.value");
        }
        JsonObject number = GachaJson.object(entry, "number", location);
        GachaJson.requireOnly(number, NUMBER_FIELDS, location + ".number");
        return switch (GachaJson.string(number, "type", location + ".number")) {
            case "long" -> new GachaLongParameterValue(
                    GachaNumberProviderJson.longProvider(number.get("value"), location + ".number.value"));
            case "double" -> new GachaDoubleParameterValue(
                    GachaNumberProviderJson.doubleProvider(number.get("value"), location + ".number.value"));
            default -> throw new JsonParseException("Numeric mapping type must be long or double at "
                    + location + ".number");
        };
    }

    private static <T> GachaConstantParameterValue<T> constant(GachaParameterDefinitionJson parameters,
                                                               ResourceLocation typeId, JsonElement encoded,
                                                               String location) {
        GachaParameterType<T> type = parameters.requireType(typeId);
        return new GachaConstantParameterValue<>(type, parameters.decodeValue(type, encoded, location));
    }
}

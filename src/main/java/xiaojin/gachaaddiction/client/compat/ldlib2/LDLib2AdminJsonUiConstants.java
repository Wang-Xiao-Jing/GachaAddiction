package xiaojin.gachaaddiction.client.compat.ldlib2;

import com.google.gson.*;
import xiaojin.gachaaddiction.util.TranslationKeys;

public class LDLib2AdminJsonUiConstants {
    public static final int MAXIMUM_DEPTH = 48;
    public static final int MAXIMUM_NODES = 2048;
    public static final int SUMMARY_LENGTH = 64;
    public static final int TREE_WIDTH_PERCENT = 45;
    public static final int CONTENT_GAP = 4;
    public static final String PATH_SEPARATOR = "/";
    public static final String PATH_ESCAPE = "~";
    public static final String PATH_ESCAPED_ESCAPE = "~0";
    public static final String PATH_ESCAPED_SEPARATOR = "~1";
    public static final String SUMMARY_SEPARATOR = " : ";
    public static final String ELLIPSIS = "…";
    public static final String INITIAL_NUMBER = "0";

    private LDLib2AdminJsonUiConstants() {
    }

    public enum Mode {
        FORM, TREE
    }

    public enum ValueType {
        OBJECT(TranslationKeys.GUI_ADMIN_JSON_OBJECT),
        ARRAY(TranslationKeys.GUI_ADMIN_JSON_ARRAY),
        STRING(TranslationKeys.GUI_ADMIN_JSON_STRING),
        NUMBER(TranslationKeys.GUI_ADMIN_JSON_NUMBER),
        BOOLEAN(TranslationKeys.GUI_ADMIN_JSON_BOOLEAN),
        NULL(TranslationKeys.GUI_ADMIN_JSON_NULL);

        private final String translationKey;

        ValueType(String translationKey) {
            this.translationKey = translationKey;
        }

        public static ValueType of(JsonElement value) {
            if (value == null || value.isJsonNull()) return NULL;
            if (value.isJsonObject()) return OBJECT;
            if (value.isJsonArray()) return ARRAY;
            var primitive = value.getAsJsonPrimitive();
            if (primitive.isBoolean()) return BOOLEAN;
            return primitive.isNumber() ? NUMBER : STRING;
        }

        public String translationKey() {
            return translationKey;
        }

        public JsonElement initialValue() {
            return switch (this) {
                case OBJECT -> new JsonObject();
                case ARRAY -> new JsonArray();
                case STRING -> new JsonPrimitive("");
                case NUMBER -> JsonParser.parseString(INITIAL_NUMBER);
                case BOOLEAN -> new JsonPrimitive(false);
                case NULL -> JsonNull.INSTANCE;
            };
        }
    }
}

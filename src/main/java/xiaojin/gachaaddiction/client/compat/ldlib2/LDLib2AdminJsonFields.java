package xiaojin.gachaaddiction.client.compat.ldlib2;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.Nullable;
import xiaojin.gachaaddiction.cardpool.GachaCardPoolResultStyle;
import xiaojin.gachaaddiction.cardpool.GachaRewardDeliveryMode;
import xiaojin.gachaaddiction.data.GachaDefinitionKind;
import xiaojin.gachaaddiction.data.GachaDefinitionReloadListener;
import xiaojin.gachaaddiction.reward.source.GachaSourceSelectionStrategy;
import xiaojin.gachaaddiction.reward.source.GachaTableSelectionStrategy;

import java.util.Arrays;
import java.util.List;

import static xiaojin.gachaaddiction.client.compat.ldlib2.LDLib2AdminJsonUiConstants.ValueType;

public class LDLib2AdminJsonFields {
    public static final String FORMAT_VERSION = "format_version";
    public static final String INCLUDES = "includes";
    public static final String ENTRIES = "entries";
    public static final String SOURCE_GROUPS = "source_groups";
    public static final String RULE_GROUPS = "rule_groups";
    public static final String REWARD_POOL = "reward_pool";
    public static final String EXECUTION_PLAN = "execution_plan";
    public static final String TABLE = "table";
    public static final String TYPE = "type";
    public static final String CUSTOM_TABLE = "custom_table";
    public static final String SOURCES = "sources";
    public static final String CHILDREN = "children";

    private LDLib2AdminJsonFields() {
    }

    public static List<Field> forKind(GachaDefinitionKind kind) {
        Field format = field(FORMAT_VERSION, ValueType.NUMBER);
        return switch (kind) {
            case REWARD_POOL -> List.of(format,
                    field("source_strategy", ValueType.STRING,
                            Arrays.stream(GachaSourceSelectionStrategy.values()).map(Enum::name).toList()),
                    field("source_count", ValueType.NUMBER), field("parameters", ValueType.ARRAY),
                    field("sources", ValueType.ARRAY), field(SOURCE_GROUPS, ValueType.ARRAY),
                    field("limits", ValueType.OBJECT), field("rules", ValueType.ARRAY),
                    field(RULE_GROUPS, ValueType.ARRAY));
            case CARD_POOL -> List.of(format, field(REWARD_POOL, ValueType.STRING),
                    field(EXECUTION_PLAN, ValueType.STRING), field("presentation.title", ValueType.STRING),
                    field("presentation.description", ValueType.STRING), field("presentation.icon", ValueType.OBJECT),
                    field("presentation.result_style", ValueType.STRING,
                            Arrays.stream(GachaCardPoolResultStyle.values()).map(Enum::name).toList()),
                    field("availability.enabled", ValueType.BOOLEAN),
                    field("availability.minimum_permission_level", ValueType.NUMBER),
                    field("availability.opens_at", ValueType.STRING), field("availability.closes_at", ValueType.STRING),
                    field("delivery.mode", ValueType.STRING,
                            Arrays.stream(GachaRewardDeliveryMode.values()).map(Enum::name).toList()),
                    field("delivery.handler", ValueType.STRING),
                    field("parameter_bindings", ValueType.ARRAY), field("packages", ValueType.ARRAY));
            case TABLE -> List.of(format,
                    field("strategy", ValueType.STRING,
                            Arrays.stream(GachaTableSelectionStrategy.values()).map(Enum::name).toList()),
                    field("selection_count", ValueType.NUMBER), field(ENTRIES, ValueType.ARRAY));
            case EXECUTION_PLAN -> List.of(format, field("parameters", ValueType.ARRAY),
                    field("steps", ValueType.ARRAY), field("limits", ValueType.OBJECT));
            case REQUEST_PRESET -> List.of(format, field(REWARD_POOL, ValueType.STRING),
                    field("rounds", ValueType.NUMBER), field("parameters", ValueType.OBJECT));
            case SOURCE_GROUP -> shared(format, "sources");
            case RULE_GROUP -> shared(format, "rules");
            case CONDITION_GROUP -> shared(format, "conditions");
            case QUANTITY_RULE -> shared(format, "operations");
        };
    }

    public static @Nullable GachaDefinitionKind referenceKind(GachaDefinitionKind kind,
                                                              List<String> path, JsonElement parent) {
        if (path.isEmpty()) return null;
        if (kind == GachaDefinitionKind.REWARD_POOL && arrayElement(path, SOURCE_GROUPS, parent)) {
            return GachaDefinitionKind.SOURCE_GROUP;
        }
        if (kind == GachaDefinitionKind.REWARD_POOL && arrayElement(path, RULE_GROUPS, parent)) {
            return GachaDefinitionKind.RULE_GROUP;
        }
        if (arrayElement(path, INCLUDES, parent)) {
            return switch (kind) {
                case SOURCE_GROUP, RULE_GROUP, CONDITION_GROUP, QUANTITY_RULE -> kind;
                default -> null;
            };
        }
        if (path.size() == 1) {
            if ((kind == GachaDefinitionKind.CARD_POOL || kind == GachaDefinitionKind.REQUEST_PRESET)
                    && path.getFirst().equals(REWARD_POOL)) return GachaDefinitionKind.REWARD_POOL;
            if (kind == GachaDefinitionKind.CARD_POOL && path.getFirst().equals(EXECUTION_PLAN)) {
                return GachaDefinitionKind.EXECUTION_PLAN;
            }
        }
        boolean sourceEntry = path.size() == 3 && index(path.get(1))
                && (kind == GachaDefinitionKind.REWARD_POOL && path.getFirst().equals(SOURCES)
                || kind == GachaDefinitionKind.SOURCE_GROUP
                && (path.getFirst().equals(SOURCES) || path.getFirst().equals(ENTRIES)));
        boolean tableEntry = kind == GachaDefinitionKind.TABLE && tableEntry(path);
        if ((sourceEntry || tableEntry) && path.getLast().equals(TABLE) && parent != null && parent.isJsonObject()) {
            JsonElement type = parent.getAsJsonObject().get(TYPE);
            if (type != null && type.isJsonPrimitive() && type.getAsJsonPrimitive().isString()
                    && type.getAsString().equals(CUSTOM_TABLE)) return GachaDefinitionKind.TABLE;
        }
        return null;
    }

    private static boolean arrayElement(List<String> path, String field, JsonElement parent) {
        return path.size() == 2 && path.getFirst().equals(field) && index(path.getLast())
                && parent != null && parent.isJsonArray();
    }

    private static boolean tableEntry(List<String> path) {
        if (path.size() < 3 || !path.getFirst().equals(ENTRIES) || !index(path.get(1))) return false;
        for (int position = 2; position < path.size() - 1; position += 2) {
            if (position + 1 >= path.size() - 1 || !path.get(position).equals(CHILDREN)
                    || !index(path.get(position + 1))) return false;
        }
        return true;
    }

    private static boolean index(String value) {
        return !value.isEmpty() && value.chars().allMatch(Character::isDigit);
    }

    private static List<Field> shared(Field format, String contentField) {
        return List.of(format, field(INCLUDES, ValueType.ARRAY), field(contentField, ValueType.ARRAY),
                field(ENTRIES, ValueType.ARRAY));
    }

    private static Field field(String path, ValueType type) {
        return field(path, type, List.of());
    }

    private static Field field(String path, ValueType type, List<String> choices) {
        return new Field(List.of(path.split("\\.")), type, choices);
    }

    public record Field(List<String> path, ValueType type, List<String> choices) {
        public JsonElement initialValue() {
            if (path.size() == 1 && path.getFirst().equals(FORMAT_VERSION)) {
                return new JsonPrimitive(GachaDefinitionReloadListener.CURRENT_FORMAT_VERSION);
            }
            return type.initialValue();
        }
    }
}

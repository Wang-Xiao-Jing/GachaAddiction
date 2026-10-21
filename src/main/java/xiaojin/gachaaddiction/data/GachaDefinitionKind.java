package xiaojin.gachaaddiction.data;

import java.util.Arrays;
import java.util.Optional;

public enum GachaDefinitionKind {
    REWARD_POOL("reward_pools"),
    CARD_POOL("card_pools"),
    TABLE("tables"),
    SOURCE_GROUP("source_groups"),
    RULE_GROUP("rule_groups"),
    CONDITION_GROUP("condition_groups"),
    QUANTITY_RULE("quantity_rules"),
    EXECUTION_PLAN("execution_plans"),
    REQUEST_PRESET("request_presets");

    private final String directory;

    GachaDefinitionKind(String directory) {
        this.directory = directory;
    }

    public static Optional<GachaDefinitionKind> fromDirectory(String directory) {
        return Arrays.stream(values()).filter(kind -> kind.directory.equals(directory)).findFirst();
    }

    public String directory() {
        return directory;
    }
}

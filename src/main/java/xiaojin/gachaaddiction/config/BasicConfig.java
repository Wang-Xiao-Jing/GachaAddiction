package xiaojin.gachaaddiction.config;

import xiaojin.gachaaddiction.datagen.DatagenI18n;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.function.Function;

public abstract class BasicConfig {
    protected final String modId;
    protected final ModConfigSpec.Builder builder;

    public BasicConfig(String modId, ModConfigSpec.Builder builder) {
        this.modId = modId;
        this.builder = builder;
    }

    protected ModConfigSpec.DoubleValue define(double defaultValue, double min, double max, String key, String... comment) {
        return builder.comment(comment)
                .translation(DatagenI18n.getConfigTranslation(modId, key))
                .defineInRange(key, defaultValue, min, max);
    }

    protected ModConfigSpec.IntValue define(int defaultValue, int min, int max, String key, String... comment) {
        return builder.comment(comment)
                .translation(DatagenI18n.getConfigTranslation(modId, key))
                .defineInRange(key, defaultValue, min, max);
    }

    protected ModConfigSpec.BooleanValue define(boolean defaultValue, String key, String... comment) {
        return builder.comment(comment)
                .translation(DatagenI18n.getConfigTranslation(modId, key))
                .define(key, defaultValue);
    }

    protected <T> Pair<T, ModConfigSpec> configure(Function<ModConfigSpec.Builder, T> consumer) {
        return new ModConfigSpec.Builder().configure(consumer);
    }
}

package xiaojin.gachaaddiction.config;

import net.neoforged.neoforge.common.ModConfigSpec.Builder;
import xiaojin.gachaaddiction.GachaAddiction;
import xiaojin.gachaaddiction.datagen.DatagenI18n;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;
import xiaojin.gachaaddiction.mixin.accessor.ModConfigSpecBuilderAccesor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class BasicConfig {
    protected final String modId;
    protected final Builder builder;

    public BasicConfig(Builder builder) {
        this.modId = GachaAddiction.MODID;
        this.builder = builder;
    }

    protected ModConfigSpec.DoubleValue define(double defaultValue, double min, double max, String key, String... comment) {
        return builder.comment(comment)
                .translation(DatagenI18n.getConfigTranslation(modId, getCurrentPath(key)))
                .defineInRange(key, defaultValue, min, max);
    }

    protected ModConfigSpec.IntValue define(int defaultValue, int min, int max, String key, String... comment) {
        return builder.comment(comment)
                .translation(DatagenI18n.getConfigTranslation(modId, getCurrentPath(key)))
                .defineInRange(key, defaultValue, min, max);
    }

    protected ModConfigSpec.BooleanValue define(boolean defaultValue, String key, String... comment) {
        return builder.comment(comment)
                .translation(DatagenI18n.getConfigTranslation(modId, getCurrentPath(key)))
                .define(key, defaultValue);
    }

    protected <T> Pair<T, ModConfigSpec> configure(Function<Builder, T> consumer) {
        return new Builder().configure(consumer);
    }

    protected Builder push(String... key) {
        builder.translation(DatagenI18n.getConfigTranslation(modId, getCurrentPath(key)));
        return builder.push(Arrays.asList(key));
    }

    protected Builder pop(int count) {
        return builder.pop(count);
    }

    protected Builder pop() {
        return builder.pop();
    }

    protected String[] getCurrentPath(String... key) {
        List<String> currentPath = new ArrayList<>();
        currentPath.addAll(((ModConfigSpecBuilderAccesor) builder).getCurrentPath());
        currentPath.addAll(Arrays.asList(key));
        return currentPath.toArray(String[]::new);
    }
}

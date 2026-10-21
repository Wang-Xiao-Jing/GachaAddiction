package xiaojin.gachaaddiction.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import xiaojin.gachaaddiction.GachaAddiction;
import xiaojin.gachaaddiction.config.entry.*;
import xiaojin.gachaaddiction.generator.lang.LangHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class BasicConfig {
    protected final String modId;
    protected final ModConfigSpec.Builder builder;
    protected final List<String> currentPath = new ArrayList<>();
    protected final List<BasicConfigEntry<?>> entries = new ArrayList<>();

    public BasicConfig(ModConfigSpec.Builder builder) {
        this.modId = GachaAddiction.MODID;
        this.builder = builder;
    }

    protected DoubleConfigEntry define(double defaultValue, double min, double max, String key, String... comment) {
        return define(DoubleConfigEntry.builder(builder, modId, currentPath), entry -> entry
                .key(key).comments(comment).defaultValue(defaultValue).range(min, max));
    }

    protected IntConfigEntry define(int defaultValue, int min, int max, String key, String... comment) {
        return define(IntConfigEntry.builder(builder, modId, currentPath), entry -> entry
                .key(key).comments(comment).defaultValue(defaultValue).range(min, max));
    }

    protected BooleanConfigEntry define(boolean defaultValue, String key, String... comment) {
        return define(BooleanConfigEntry.builder(builder, modId, currentPath), entry -> entry
                .key(key).comments(comment).defaultValue(defaultValue));
    }

    @SuppressWarnings("unused")
    protected StringConfigEntry define(String defaultValue, String key, String... comment) {
        return define(StringConfigEntry.builder(builder, modId, currentPath), entry -> entry
                .key(key).comments(comment).defaultValue(defaultValue));
    }

    @SuppressWarnings("SameParameterValue")
    protected <V> SelectorConfigEntry<V> selectorDefine(V defaultValue, List<V> allowedValues, String key,
                                                        Function<String, V> decoding, Function<V, String> encode, String... comment) {
        return define(SelectorConfigEntry.builder(builder, modId, currentPath), entry -> entry
                .key(key).comments(comment).defaultValue(defaultValue).allowedValues(allowedValues)
                .codec(decoding, encode));
    }

    @SuppressWarnings("unused")
    protected StringListConfigEntry defineList(List<? extends String> defaultValue, Predicate<Object> elementValidator,
                                               String key, String... comment) {
        return define(StringListConfigEntry.builder(builder, modId, currentPath), entry -> entry
                .key(key).comments(comment).defaultValue(defaultValue).elementValidator(elementValidator));
    }

    protected FilterListConfigEntry defineFilterList(List<? extends List<String>> defaultValue,
                                                     Predicate<Object> conditionValidator,
                                                     String key, String... comment) {
        return define(FilterListConfigEntry.builder(builder, modId, currentPath), entry -> entry
                .key(key).comments(comment).defaultValue(defaultValue).conditionValidator(conditionValidator));
    }

    private <E extends BasicConfigEntry<?>, B extends BasicConfigEntry.Builder<E, B>> E define(
            B entryBuilder, Consumer<B> definition) {
        definition.accept(entryBuilder);
        return register(entryBuilder.build());
    }

    protected <T extends BasicConfigEntry<?>> T register(T entry) {
        entries.add(entry);
        return entry;
    }

    public List<BasicConfigEntry<?>> entries() {
        return List.copyOf(entries);
    }

    protected void push(String key) {
        builder.translation(LangHandler.getConfigTranslation(modId, getCurrentPath(key)));
        currentPath.add(key);
        builder.push(key);
    }

    protected void pop() {
        currentPath.removeLast();
        builder.pop();
    }

    protected String[] getCurrentPath(String key) {
        List<String> path = new ArrayList<>(currentPath);
        path.add(key);
        return path.toArray(String[]::new);
    }
}

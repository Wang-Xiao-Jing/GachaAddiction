package xiaojin.gachaaddiction.generator.lang;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import xiaojin.gachaaddiction.GachaAddiction;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class LangHandler {
    private static final Map<String, Map<String, LangSet>> MOD_LANG_MAP = new HashMap<>();
    public static final String EN_US = "en_us";
    public static final String ZH_CN = "zh_cn";
    public static final String RU_RU = "ru_ru";

    public static String getConfigTranslation(String modId, String... keys) {
        StringBuilder builder = new StringBuilder(modId + ".configuration");
        for (String key : keys) {
            builder.append(".").append(key);
        }
        return builder.toString();
    }

    public static String translationKey(String prefix, String... suffix) {
        return prefix + "." + GachaAddiction.MODID
                + (suffix.length > 0 ? "." + String.join(".", suffix) : "");
    }

    public static LangSet getLang(String namespace, String lang) {
        return MOD_LANG_MAP.computeIfAbsent(namespace, k -> new HashMap<>()).computeIfAbsent(lang, k -> new LangSet());
    }

    public static void creates(String namespace, String enUs, String zhCn, BiConsumer<LangSet, String> unaryOperator) {
        create(namespace, EN_US, enUs, unaryOperator);
        create(namespace, ZH_CN, zhCn, unaryOperator);
    }

    public static String create(String namespace, String lang, String key) {
        create(namespace, lang, key, (langSet, txt) -> langSet.add(key, txt));
        return key;
    }

    public static String creates(String namespace, String key, String enUs, String zhCn) {
        creates(namespace, enUs, zhCn, (langSet, txt) -> langSet.add(key, txt));
        return key;
    }

    public static void create(String namespace, String lang, String txt, BiConsumer<LangSet, String> unaryOperator) {
        if (LangHandler.isDataGen()) {
            unaryOperator.accept(LangHandler.getLang(namespace, lang), txt);
        }
    }

    public static LangSet getEnUsLang(String namespace) {
        return getLang(namespace, EN_US);
    }

    public static LangSet getZhCnLang(String namespace) {
        return getLang(namespace, ZH_CN);
    }

    public static boolean isDataGen() {
        return System.getProperty("fabric-api.datagen") != null;
    }

    public static final class LangSet {
        public final Map<Supplier<? extends MobEffect>, String> mobEffects = new HashMap<>();
        public final Map<Supplier<? extends Attribute>, String> attributes = new HashMap<>();
        public final Map<Supplier<? extends SoundEvent>, String> soundEvents = new HashMap<>();
        public final Map<Supplier<? extends Item>, String> items = new HashMap<>();
        public final Map<Supplier<? extends Block>, String> blocks = new HashMap<>();
        public final Map<Supplier<? extends EntityType<?>>, String> entityTypes = new HashMap<>();
        public final Map<TagKey<?>, String> tagKeys = new HashMap<>();
        public final Map<String, String> map = new HashMap<>();

        public void add(String key, String txt) {
            map.put(key, txt);
        }

        public void tagKey(TagKey<?> key, String txt) {
            tagKeys.put(key, txt);
        }

        public void mobEffectText(Supplier<? extends MobEffect> supplier, String txt) {
            mobEffects.put(supplier, txt);
        }

        public void attributeText(Supplier<? extends Attribute> supplier, String txt) {
            attributes.put(supplier, txt);
        }

        public void soundEventText(Supplier<? extends SoundEvent> supplier, String txt) {
            soundEvents.put(supplier, txt);
        }

        public void itemText(Supplier<? extends Item> supplier, String txt) {
            items.put(supplier, txt);
        }

        public void blockText(Supplier<? extends Block> supplier, String txt) {
            blocks.put(supplier, txt);
        }

        public void entityTypeText(Supplier<? extends EntityType<?>> supplier, String txt) {
            entityTypes.put(supplier, txt);
        }

        @Override
        public String toString() {
            return mobEffects + "\n"
                    + attributes + "\n"
                    + soundEvents + "\n"
                    + items + "\n"
                    + blocks + "\n"
                    + entityTypes + "\n"
                    + tagKeys + "\n"
                    + map;
        }
    }
}

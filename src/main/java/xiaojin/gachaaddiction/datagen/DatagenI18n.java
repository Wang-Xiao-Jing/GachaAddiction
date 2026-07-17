package xiaojin.gachaaddiction.datagen;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.data.LanguageProvider;
import xiaojin.gachaaddiction.GachaAddiction;

import java.util.Map;
import java.util.function.Supplier;

public abstract class DatagenI18n extends LanguageProvider {
    protected final String modId;

    public DatagenI18n(PackOutput output, String modId, String locale) {
        super(output, modId, locale);
        this.modId = modId;
    }

    @Override
    public abstract void addTranslations();

    protected void addPackDescription(String a, String description) {
        add("pack." + a + ".description", description);
    }

    protected void addItemList(Map<Supplier<? extends Item>, String> map) {
        map.forEach((holder, zhName) -> add(holder.get(), zhName));
    }

    protected void addEntityList(Map<Supplier<? extends EntityType<?>>, String> map) {
        map.forEach((holder, zhName) -> add(holder.get(), zhName));
    }

    protected void addMobEffectList(Map<Supplier<? extends MobEffect>, String> map) {
        map.forEach((holder, zhName) -> add(holder.get(), zhName));
    }

    protected void addAttributeList(Map<Supplier<? extends Attribute>, String> map) {
        map.forEach((holder, zhName) -> add(holder.get(), zhName));
    }

    /**
     * 生物属性翻译
     */
    protected void add(Attribute attribute, String name) {
        add(attribute.getDescriptionId(), name);
    }

    protected void addSoundEventList(Map<Supplier<? extends SoundEvent>, String> map) {
        map.forEach((holder, zhName) -> add(holder.get(), zhName));
    }

    public void add(ModConfigSpec.ConfigValue<?> configValue, String value, String tooltipValue) {
        add(configValue, value);
        add(getConfigTranslation(modId, configValue.getPath().toArray(new String[0])), tooltipValue);
    }

    public void add(ModConfigSpec.ConfigValue<?> configValue, String value) {
        add(getConfigTranslation(modId, configValue.getPath().toArray(new String[0])), value);
    }

    protected <T> void add(DataComponentType<T> dataComponentType, String name) {
        add(dataComponentType.toString(), name);
    }

    /**
     * 死亡消息翻译
     */
    protected void addDeathMessage(ResourceKey<DamageType> damageType, String name) {
        add("death.attack." + damageType.location().getPath(), name);
    }

    /**
     * 声音字幕翻译
     */
    protected void addSoundEvent(Holder<SoundEvent> holder, String name) {
        add(holder.value(), name);
    }

    protected void add(SoundEvent damageType, String name) {
        add("sound." + damageType.getLocation().toLanguageKey(), name);
    }

    /**
     * 玩家死亡消息翻译
     */
    protected void addPlayerDeathMessage(ResourceKey<DamageType> damageType, String name) {
        add("death.attack." + damageType.location().getPath() + ".player", name);
    }

    public static String getFormattedKey(String... key) {
        StringBuilder builder = new StringBuilder(GachaAddiction.MODID);
        builder.append(".commands");
        for (String s : key) {
            builder.append(".").append(s);
        }
        return builder.toString();
    }

    public static String getConfigTranslation(String modId, String... keys) {
        if (keys.length == 0) {
            return modId + ".config";
        }
        StringBuilder builder = new StringBuilder();
        for (String key : keys) {
            builder.append(".");
            builder.append(key);
        }
        return modId + ".config" + builder;
    }
}

package xiaojin.gachaaddiction.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import xiaojin.gachaaddiction.GachaAddiction;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ModZhCn extends DatagenI18n {
    private static final Map<Supplier<? extends SoundEvent>, String> SOUND_EVENT = new HashMap<>();
    private static final Map<String, String> MAP = new HashMap<>();

    public ModZhCn(PackOutput output) {
        super(output, GachaAddiction.MODID, "zh_cn");
    }

    @Override
    public void addTranslations() {
        addPackDescription(GachaAddiction.MODID, "抽卡瘾");
        MAP.forEach(this::add);
        addSoundEventList(SOUND_EVENT);
    }

    public static void addI18nSoundEventText(String zhName, Supplier<? extends SoundEvent> supplier) {
        if (!FMLEnvironment.production) {
            SOUND_EVENT.put(supplier, zhName);
        }
    }
}

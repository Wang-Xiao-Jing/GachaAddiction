package xiaojin.gachaaddiction.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.SoundDefinition;
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider;
import xiaojin.gachaaddiction.GachaAddiction;
import xiaojin.gachaaddiction.init.ModSoundEvents;

public class DatagenSoundDefinitionsProvider extends SoundDefinitionsProvider {
    protected DatagenSoundDefinitionsProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, GachaAddiction.MODID, helper);
    }

    @Override
    public void registerSounds() {
        add(ModSoundEvents.LEVEL0.get(), 0.5f, 0.5f, 1, 8);
        add(ModSoundEvents.LEVEL1.get(), 0.5f, 0.5f, 1, 8);
        add(ModSoundEvents.LEVEL2.get(), 0.5f, 0.5f, 1, 8);
        add(ModSoundEvents.LEVEL3.get(), 0.5f, 0.5f, 1, 8);
        add(ModSoundEvents.LEVEL4.get(), 0.5f, 0.5f, 1, 8);
    }

    private void add(SoundEvent soundEvent, float volume, float pitch, int weight, int attenuationDistance) {
        add(soundEvent, 1, volume, pitch, weight, attenuationDistance);
    }

    private void add(SoundEvent soundEvent, int amount, float volume, float pitch, int weight, int attenuationDistance) {
        ResourceLocation location = soundEvent.getLocation();
        assert amount > 0 : "sound : " + location + " amount must be greater than 0";

        SoundDefinition definition = SoundDefinition.definition();
        for (int i = 0; i < amount; i++) {
            String suffix = (i > 0) ? Integer.toString(i) : "";
            definition.with(SoundDefinition.Sound.sound(location.withSuffix(suffix), SoundDefinition.SoundType.SOUND)
                    .volume(volume)
                    .pitch(pitch)
                    .weight(weight)
                    .attenuationDistance(attenuationDistance));
        }
        add(soundEvent, definition.subtitle(getSubtitle(location)).replace(true));
    }

    public static String getSubtitle(SoundEvent soundEvent) {
        return getSubtitle(soundEvent.getLocation());
    }

    public static String getSubtitle(ResourceLocation location) {
        return "sound." + location.toLanguageKey();
    }
}

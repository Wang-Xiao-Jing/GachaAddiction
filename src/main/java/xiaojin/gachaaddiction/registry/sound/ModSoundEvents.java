package xiaojin.gachaaddiction.registry.sound;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import xiaojin.gachaaddiction.GachaAddiction;

public class ModSoundEvents {
    public static final SoundEvent LEVEL0 = register("level0");
    public static final SoundEvent LEVEL1 = register("level1");
    public static final SoundEvent LEVEL2 = register("level2");
    public static final SoundEvent LEVEL3 = register("level3");
    public static final SoundEvent LEVEL4 = register("level4");

    private static SoundEvent register(String id) {
        return register(id, id);
    }

    private static SoundEvent register(String id, String location) {
        ResourceLocation resourceLocation = ResourceLocation.fromNamespaceAndPath(GachaAddiction.MODID, location);
        SoundEvent soundEvent = SoundEvent.createVariableRangeEvent(resourceLocation);
        return Registry.register(BuiltInRegistries.SOUND_EVENT,
                ResourceLocation.fromNamespaceAndPath(GachaAddiction.MODID, id), soundEvent);
    }

    public static void init() {
    }
}

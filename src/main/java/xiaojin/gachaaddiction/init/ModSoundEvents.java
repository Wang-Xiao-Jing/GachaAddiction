package xiaojin.gachaaddiction.init;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import xiaojin.gachaaddiction.GachaAddiction;

import java.util.function.Supplier;

public class ModSoundEvents {
    public static final DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, GachaAddiction.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> LEVEL0 = register("level0");
    public static final DeferredHolder<SoundEvent, SoundEvent> LEVEL1 = register("level1");
    public static final DeferredHolder<SoundEvent, SoundEvent> LEVEL2 = register("level2");
    public static final DeferredHolder<SoundEvent, SoundEvent> LEVEL3 = register("level3");
    public static final DeferredHolder<SoundEvent, SoundEvent> LEVEL4 = register("level4");

    private static DeferredHolder<SoundEvent, SoundEvent> register(String id) {
        return register(id, id);
    }

    private static DeferredHolder<SoundEvent, SoundEvent> register(String id, String location) {
        Supplier<SoundEvent> soundEventSupplier = () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(GachaAddiction.MODID, location));
        return REGISTRY.register(id, soundEventSupplier);
    }
}

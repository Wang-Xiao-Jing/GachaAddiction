package xiaojin.gachaaddiction.init;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import xiaojin.gachaaddiction.GachaAddiction;

public class ModSoundEvents {
    public static final Holder.Reference<SoundEvent> LEVEL0 = register("level0");
    public static final Holder.Reference<SoundEvent> LEVEL1 = register("level1");
    public static final Holder.Reference<SoundEvent> LEVEL2 = register("level2");
    public static final Holder.Reference<SoundEvent> LEVEL3 = register("level3");
    public static final Holder.Reference<SoundEvent> LEVEL4 = register("level4");

    private static Holder.Reference<SoundEvent> register(String id) {
        Identifier location = Identifier.fromNamespaceAndPath(GachaAddiction.MODID, id);
        return Registry.registerForHolder(BuiltInRegistries.SOUND_EVENT, location,
                SoundEvent.createVariableRangeEvent(location));
    }

    /** 触发类加载，使上面的静态字段完成注册。 */
    public static void init() {
    }
}

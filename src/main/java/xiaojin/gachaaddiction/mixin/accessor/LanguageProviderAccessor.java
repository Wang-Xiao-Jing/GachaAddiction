package xiaojin.gachaaddiction.mixin.accessor;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(value = LanguageProvider.class, remap = false)
public interface LanguageProviderAccessor {
    @Accessor
    String getLocale();
    @Accessor
    PackOutput getOutput();
}

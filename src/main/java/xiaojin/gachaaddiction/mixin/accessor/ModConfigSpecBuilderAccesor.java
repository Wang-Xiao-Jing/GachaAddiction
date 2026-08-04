package xiaojin.gachaaddiction.mixin.accessor;

/**
 * @author wang_
 * @version 2024.3.4.1
 * @description
 * @date 2026/7/21
 */

import net.neoforged.neoforge.common.ModConfigSpec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ModConfigSpec.Builder.class)
public interface ModConfigSpecBuilderAccesor {
    @Accessor
    List<String> getCurrentPath();
}

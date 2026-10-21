package xiaojin.gachaaddiction.reward.parameter;

import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

@FunctionalInterface
public interface GachaParameterTypeResolver {
    Optional<GachaParameterType<?>> resolve(ResourceLocation id);
}

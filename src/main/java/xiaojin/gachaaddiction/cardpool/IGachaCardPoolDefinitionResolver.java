package xiaojin.gachaaddiction.cardpool;

import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

@FunctionalInterface
public interface IGachaCardPoolDefinitionResolver {
    Optional<GachaCardPoolDefinition> resolveCardPool(ResourceLocation id);
}

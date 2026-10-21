package xiaojin.gachaaddiction.cardpool;

import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

@FunctionalInterface
public interface GachaCardPoolDefinitionResolver {
    Optional<GachaCardPoolDefinition> resolveCardPool(ResourceLocation id);
}

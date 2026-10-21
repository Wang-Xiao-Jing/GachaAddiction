package xiaojin.gachaaddiction.cardpool.transaction;

import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

@FunctionalInterface
public interface GachaConsumptionHandlerResolver {
    Optional<GachaConsumptionHandler> resolve(ResourceLocation type);
}

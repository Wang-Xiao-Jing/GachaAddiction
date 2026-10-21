package xiaojin.gachaaddiction.cardpool.transaction;

import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

@FunctionalInterface
public interface IGachaRewardDeliveryHandlerResolver {
    Optional<IGachaRewardDeliveryHandler> resolve(ResourceLocation type);
}

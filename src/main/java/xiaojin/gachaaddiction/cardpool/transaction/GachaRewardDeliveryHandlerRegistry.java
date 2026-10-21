package xiaojin.gachaaddiction.cardpool.transaction;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public final class GachaRewardDeliveryHandlerRegistry implements GachaRewardDeliveryHandlerResolver {
    private static final Set<GachaRewardDeliveryCapability> REQUIRED_CAPABILITIES = Set.of(
            GachaRewardDeliveryCapability.IDEMPOTENT_BY_TRANSACTION,
            GachaRewardDeliveryCapability.DURABLE_BEFORE_ACKNOWLEDGEMENT);
    private final Map<ResourceLocation, GachaRewardDeliveryHandler> handlers = new LinkedHashMap<>();
    private boolean frozen;

    public synchronized void register(GachaRewardDeliveryHandler handler) {
        Objects.requireNonNull(handler, "handler");
        if (frozen) {
            throw new IllegalStateException("Reward delivery handler registry is frozen");
        }
        ResourceLocation type = Objects.requireNonNull(handler.type(), "handler.type");
        Set<GachaRewardDeliveryCapability> capabilities = Set.copyOf(
                Objects.requireNonNull(handler.capabilities(), "handler.capabilities"));
        if (capabilities.isEmpty()) {
            throw new IllegalArgumentException("Reward delivery handler must declare capabilities: " + type);
        }
        if (!capabilities.containsAll(REQUIRED_CAPABILITIES)) {
            throw new IllegalArgumentException("Reward delivery handler lacks durable idempotent capabilities: "
                    + type);
        }
        if (handlers.putIfAbsent(type, new RegisteredHandler(type, handler, capabilities)) != null) {
            throw new IllegalArgumentException("Duplicate reward delivery handler: " + type);
        }
    }

    public synchronized void freeze() {
        frozen = true;
    }

    @Override
    public synchronized Optional<GachaRewardDeliveryHandler> resolve(ResourceLocation type) {
        return Optional.ofNullable(handlers.get(Objects.requireNonNull(type, "type")));
    }

    private record RegisteredHandler(ResourceLocation type, GachaRewardDeliveryHandler delegate,
                                     Set<GachaRewardDeliveryCapability> capabilities)
            implements GachaRewardDeliveryHandler {
        @Override
        public void validate(GachaTransactionContext context, GachaRewardDeliveryServices services) {
            delegate.validate(context, services);
        }

        @Override
        public void deliver(GachaTransactionContext context, List<ItemStack> rewards,
                            GachaRewardDeliveryServices services) {
            delegate.deliver(context, rewards, services);
        }
    }
}

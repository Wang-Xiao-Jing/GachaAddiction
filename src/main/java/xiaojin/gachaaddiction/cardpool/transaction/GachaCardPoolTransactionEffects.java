package xiaojin.gachaaddiction.cardpool.transaction;

import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.cardpool.GachaCardPoolTransactionSource;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class GachaCardPoolTransactionEffects implements GachaTransactionEffects {
    private static final Set<GachaRewardDeliveryCapability> REQUIRED_CAPABILITIES = Set.of(
            GachaRewardDeliveryCapability.IDEMPOTENT_BY_TRANSACTION,
            GachaRewardDeliveryCapability.DURABLE_BEFORE_ACKNOWLEDGEMENT);

    private final GachaRewardDeliveryHandlerResolver handlers;
    private final GachaRewardDeliveryServices services;

    public GachaCardPoolTransactionEffects(GachaRewardDeliveryHandlerResolver handlers,
                                           GachaRewardDeliveryServices services) {
        this.handlers = Objects.requireNonNull(handlers, "handlers");
        this.services = Objects.requireNonNull(services, "services");
    }

    @Override
    public void validate(GachaTransactionContext context) {
        GachaRewardDeliveryHandler handler = resolve(context);
        if (!handler.capabilities().containsAll(REQUIRED_CAPABILITIES)) {
            throw new IllegalStateException("Reward delivery handler lacks durable idempotent capabilities: "
                    + handler.type());
        }
        handler.validate(context, services);
    }

    @Override
    public boolean canRecover(GachaTransactionContext context) {
        ResourceLocation type = GachaCardPoolTransactionSource.deliveryHandler(context.source());
        Optional<GachaRewardDeliveryHandler> handler = handlers.resolve(type);
        return handler.map(value -> value.capabilities().contains(
                GachaRewardDeliveryCapability.SUPPORTS_OFFLINE_PLAYER)
                || services.player(context.playerId()).isPresent()).orElse(false);
    }

    @Override
    public void apply(GachaTransactionContext context, GachaTransactionResult result) {
        validate(context);
        GachaRewardDeliveryHandler handler = resolve(context);
        handler.deliver(context, result.rewards(), services);
    }

    private GachaRewardDeliveryHandler resolve(GachaTransactionContext context) {
        return handlers.resolve(GachaCardPoolTransactionSource.deliveryHandler(context.source()))
                .orElseThrow(() -> new IllegalArgumentException("Unknown reward delivery handler: "
                        + GachaCardPoolTransactionSource.deliveryHandler(context.source())));
    }
}

package xiaojin.gachaaddiction.cardpool.transaction;

import net.minecraft.resources.ResourceLocation;

import java.util.*;

public final class GachaConsumptionHandlerRegistry implements GachaConsumptionHandlerResolver {
    private final Map<ResourceLocation, GachaConsumptionHandler> handlers = new LinkedHashMap<>();
    private boolean frozen;

    public synchronized void register(GachaConsumptionHandler handler) {
        Objects.requireNonNull(handler, "handler");
        if (frozen) {
            throw new IllegalStateException("Consumption handler registry is frozen");
        }
        if (handlers.putIfAbsent(handler.type(), handler) != null) {
            throw new IllegalArgumentException("Duplicate consumption handler: " + handler.type());
        }
    }

    public synchronized Map<ResourceLocation, GachaConsumptionHandler> freeze() {
        frozen = true;
        return Collections.unmodifiableMap(new LinkedHashMap<>(handlers));
    }

    @Override
    public synchronized Optional<GachaConsumptionHandler> resolve(ResourceLocation type) {
        return Optional.ofNullable(handlers.get(Objects.requireNonNull(type, "type")));
    }
}

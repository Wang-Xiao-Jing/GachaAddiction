package xiaojin.gachaaddiction.reward.parameter;

import net.minecraft.resources.ResourceLocation;

import java.util.*;

public final class GachaParameterTypeRegistry implements GachaParameterTypeResolver {
    private final Map<ResourceLocation, GachaParameterType<?>> types = new LinkedHashMap<>();
    private boolean frozen;

    public GachaParameterTypeRegistry() {
        register(GachaParameterTypes.BOOLEAN);
        register(GachaParameterTypes.INTEGER);
        register(GachaParameterTypes.LONG);
        register(GachaParameterTypes.DOUBLE);
        register(GachaParameterTypes.STRING);
        register(GachaParameterTypes.RESOURCE_LOCATION);
        register(GachaParameterTypes.COMPOUND_TAG);
    }

    public synchronized void register(GachaParameterType<?> type) {
        Objects.requireNonNull(type, "type");
        if (frozen) {
            throw new IllegalStateException("Parameter type registry is frozen");
        }
        if (types.putIfAbsent(type.id(), type) != null) {
            throw new IllegalArgumentException("Duplicate parameter type: " + type.id());
        }
    }

    public synchronized Map<ResourceLocation, GachaParameterType<?>> freeze() {
        frozen = true;
        return Collections.unmodifiableMap(new LinkedHashMap<>(types));
    }

    @Override
    public synchronized Optional<GachaParameterType<?>> resolve(ResourceLocation id) {
        return Optional.ofNullable(types.get(Objects.requireNonNull(id, "id")));
    }
}

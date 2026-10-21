package xiaojin.gachaaddiction.execution;

import net.minecraft.resources.ResourceLocation;

import java.util.*;

public final class GachaExecutionStepTypeRegistry {
    private final Map<ResourceLocation, IGachaExecutionStepType> types = new LinkedHashMap<>();
    private boolean frozen;

    public synchronized void register(ResourceLocation id, IGachaExecutionStepType type) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(type, "type");
        if (frozen) {
            throw new IllegalStateException("Execution step type registry is frozen");
        }
        if (types.putIfAbsent(id, type) != null) {
            throw new IllegalArgumentException("Duplicate execution step type: " + id);
        }
    }

    public synchronized Map<ResourceLocation, IGachaExecutionStepType> freeze() {
        frozen = true;
        return Collections.unmodifiableMap(new LinkedHashMap<>(types));
    }

    public synchronized Optional<IGachaExecutionStepType> resolve(ResourceLocation id) {
        return Optional.ofNullable(types.get(Objects.requireNonNull(id, "id")));
    }
}

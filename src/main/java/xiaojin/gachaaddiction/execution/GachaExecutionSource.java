package xiaojin.gachaaddiction.execution;

import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.Optional;

public record GachaExecutionSource(ResourceLocation id, Optional<String> snapshotHash) {
    public GachaExecutionSource {
        Objects.requireNonNull(id, "id");
        snapshotHash = Objects.requireNonNull(snapshotHash, "snapshotHash");
        snapshotHash.ifPresent(hash -> {
            if (hash.isBlank()) {
                throw new IllegalArgumentException("Execution source snapshot hash cannot be blank");
            }
        });
    }

    public static GachaExecutionSource live(ResourceLocation id) {
        return new GachaExecutionSource(id, Optional.empty());
    }
}

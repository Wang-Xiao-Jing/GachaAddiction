package xiaojin.gachaaddiction.cardpool.transaction;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.Optional;

public record GachaTransactionSource(ResourceLocation id, Optional<String> snapshotHash, CompoundTag data) {
    public GachaTransactionSource(ResourceLocation id, Optional<String> snapshotHash, CompoundTag data) {
        this.id = Objects.requireNonNull(id, "id");
        this.snapshotHash = Objects.requireNonNull(snapshotHash, "snapshotHash").map(snapshotHash1 -> {
            if (snapshotHash1.isBlank()) {
                throw new IllegalArgumentException("Transaction source snapshot hash cannot be blank");
            }
            return snapshotHash1;
        });
        this.data = Objects.requireNonNull(data, "data").copy();
    }

    @Override
    public CompoundTag data() {
        return data.copy();
    }
}

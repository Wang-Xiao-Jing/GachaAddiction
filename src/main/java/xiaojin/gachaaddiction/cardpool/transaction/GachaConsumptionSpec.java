package xiaojin.gachaaddiction.cardpool.transaction;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public record GachaConsumptionSpec(ResourceLocation type, CompoundTag data) {
    public GachaConsumptionSpec(ResourceLocation type, CompoundTag data) {
        this.type = Objects.requireNonNull(type, "type");
        this.data = Objects.requireNonNull(data, "data").copy();
    }

    @Override
    public CompoundTag data() {
        return data.copy();
    }
}

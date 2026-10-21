package xiaojin.gachaaddiction.cardpool.transaction;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public record GachaConsumptionReservation(ResourceLocation handlerType, CompoundTag data) {
    public GachaConsumptionReservation(ResourceLocation handlerType, CompoundTag data) {
        this.handlerType = Objects.requireNonNull(handlerType, "handlerType");
        this.data = Objects.requireNonNull(data, "data").copy();
    }

    @Override
    public CompoundTag data() {
        return data.copy();
    }
}

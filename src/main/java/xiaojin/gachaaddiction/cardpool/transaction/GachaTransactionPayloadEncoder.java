package xiaojin.gachaaddiction.cardpool.transaction;

import net.minecraft.nbt.CompoundTag;

import java.util.List;

public interface GachaTransactionPayloadEncoder {
    CompoundTag prepared(GachaTransactionContext context, List<GachaConsumptionSpec> consumption);

    CompoundTag reserved(GachaTransactionContext context, List<GachaConsumptionReservation> reservations);

    CompoundTag rewardComputed(GachaTransactionContext context, GachaTransactionResult result,
                               List<GachaConsumptionReservation> reservations);
}

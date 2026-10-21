package xiaojin.gachaaddiction.cardpool.transaction;

import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public interface IGachaConsumptionHandler {
    ResourceLocation type();

    void validate(GachaTransactionContext context, GachaConsumptionSpec consumption);

    GachaConsumptionReservation reserve(GachaTransactionContext context, GachaConsumptionSpec consumption);

    Optional<GachaConsumptionReservation> findReservation(GachaTransactionContext context,
                                                          GachaConsumptionSpec consumption);

    void commit(GachaTransactionContext context, GachaConsumptionReservation reservation);

    void rollback(GachaTransactionContext context, GachaConsumptionReservation reservation);

    void recover(GachaTransactionContext context, GachaConsumptionReservation reservation,
                 GachaTransactionStage durableStage);
}

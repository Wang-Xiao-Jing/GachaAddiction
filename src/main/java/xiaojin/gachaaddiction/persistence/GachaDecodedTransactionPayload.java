package xiaojin.gachaaddiction.persistence;

import xiaojin.gachaaddiction.cardpool.transaction.GachaConsumptionReservation;
import xiaojin.gachaaddiction.cardpool.transaction.GachaConsumptionSpec;
import xiaojin.gachaaddiction.cardpool.transaction.GachaTransactionContext;
import xiaojin.gachaaddiction.cardpool.transaction.GachaTransactionResult;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record GachaDecodedTransactionPayload(GachaTransactionContext context,
                                             List<GachaConsumptionSpec> consumption,
                                             List<GachaConsumptionReservation> reservations,
                                             Optional<GachaTransactionResult> result) {
    public GachaDecodedTransactionPayload {
        Objects.requireNonNull(context, "context");
        consumption = List.copyOf(consumption);
        reservations = List.copyOf(reservations);
        result = Objects.requireNonNull(result, "result");
    }
}

package xiaojin.gachaaddiction.reward.quantity;

import java.util.List;
import java.util.Objects;

public final class GachaQuantityPipeline {
    private final List<IGachaQuantityOperation> operations;

    public GachaQuantityPipeline(List<IGachaQuantityOperation> operations) {
        if (operations.isEmpty()) {
            throw new IllegalArgumentException("Quantity pipeline requires at least one operation");
        }
        this.operations = List.copyOf(operations);
    }

    public long apply(long initialQuantity, GachaQuantityContext context) {
        if (initialQuantity <= 0) {
            throw new IllegalArgumentException("Initial logical quantity must be positive");
        }
        Objects.requireNonNull(context, "context");
        long quantity = initialQuantity;
        for (IGachaQuantityOperation operation : operations) {
            quantity = operation.apply(quantity, context);
            if (quantity < 0) {
                throw new IllegalArgumentException("Quantity operation produced a negative value: "
                        + operation.type());
            }
            if (operation.stopAfter()) {
                break;
            }
        }
        return quantity;
    }
}

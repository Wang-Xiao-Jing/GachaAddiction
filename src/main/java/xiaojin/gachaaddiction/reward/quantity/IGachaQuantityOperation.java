package xiaojin.gachaaddiction.reward.quantity;

public interface IGachaQuantityOperation {
    GachaQuantityOperationType type();

    long apply(long current, GachaQuantityContext context);

    boolean stopAfter();
}

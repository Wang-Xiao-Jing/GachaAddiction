package xiaojin.gachaaddiction.cardpool.parameter;

@FunctionalInterface
public interface GachaPlayerChoiceConstraint<T> {
    boolean allows(T value);
}

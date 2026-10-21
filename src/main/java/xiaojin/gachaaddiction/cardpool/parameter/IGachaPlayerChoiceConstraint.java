package xiaojin.gachaaddiction.cardpool.parameter;

@FunctionalInterface
public interface IGachaPlayerChoiceConstraint<T> {
    boolean allows(T value);
}

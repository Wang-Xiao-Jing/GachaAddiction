package xiaojin.gachaaddiction.cardpool.parameter;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class GachaEnumeratedChoiceConstraint<T> implements IGachaPlayerChoiceConstraint<T> {
    private final List<T> values;
    private final Set<T> lookup;

    public GachaEnumeratedChoiceConstraint(List<T> values) {
        this.values = List.copyOf(values);
        if (this.values.isEmpty()) {
            throw new IllegalArgumentException("Player choice values cannot be empty");
        }
        this.values.forEach(value -> Objects.requireNonNull(value, "player choice value"));
        this.lookup = Set.copyOf(new LinkedHashSet<>(this.values));
        if (lookup.size() != this.values.size()) {
            throw new IllegalArgumentException("Player choice values cannot contain duplicates");
        }
    }

    @Override
    public boolean allows(T value) {
        return lookup.contains(value);
    }

    public List<T> values() {
        return values;
    }
}

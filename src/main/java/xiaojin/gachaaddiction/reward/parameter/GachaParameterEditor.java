package xiaojin.gachaaddiction.reward.parameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class GachaParameterEditor {
    private final String inputFingerprint;
    private final List<GachaParameterOperation> operations = new ArrayList<>();
    private GachaParameterSet.Builder values;

    public GachaParameterEditor(String inputFingerprint, GachaParameterSet initialValues) {
        this.inputFingerprint = Objects.requireNonNull(inputFingerprint, "inputFingerprint");
        this.values = Objects.requireNonNull(initialValues, "initialValues").toBuilder();
    }

    private static void requireWritable(GachaParameterDefinition<?> definition) {
        Objects.requireNonNull(definition, "definition");
        if (definition.direction() == GachaParameterDirection.INPUT) {
            throw new IllegalArgumentException("Input parameter cannot be modified: " + definition.key());
        }
    }

    public <T> void set(GachaParameterDefinition<T> definition, T value) {
        requireWritable(definition);
        Objects.requireNonNull(value, "value");
        GachaParameterSet currentValues = values.build();
        Object before = currentValues.get(definition.key()).orElse(null);
        if (value.equals(before)) {
            return;
        }
        values.put(definition, value);
        operations.add(GachaParameterOperation.set(definition.key(), before, value));
    }

    public void remove(GachaParameterDefinition<?> definition) {
        requireWritable(definition);
        GachaParameterSet currentValues = values.build();
        currentValues.get(definition.key()).ifPresent(before -> {
            values.remove(definition.key());
            operations.add(GachaParameterOperation.remove(definition.key(), before));
        });
    }

    public GachaParameterSet currentValues() {
        return values.build();
    }

    public GachaParameterSnapshot snapshot() {
        return new GachaParameterSnapshot(values.build(), operations.size());
    }

    public void restore(GachaParameterSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot");
        values = snapshot.values().toBuilder();
        while (operations.size() > snapshot.operationCount()) {
            operations.removeLast();
        }
    }

    public GachaParameterChangeSet buildChangeSet() {
        return new GachaParameterChangeSet(inputFingerprint, operations, values.build());
    }
}

package xiaojin.gachaaddiction.execution;

import xiaojin.gachaaddiction.reward.number.GachaNumberContext;
import xiaojin.gachaaddiction.reward.parameter.*;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public record GachaParameterMapping(List<GachaParameterMappingEntry> entries) {
    public static final GachaParameterMapping EMPTY = new GachaParameterMapping(List.of());

    public GachaParameterMapping {
        entries = List.copyOf(entries);
        Set<Object> targets = new HashSet<>();
        for (GachaParameterMappingEntry entry : entries) {
            Objects.requireNonNull(entry, "entry");
            if (!targets.add(entry.target())) {
                throw new IllegalArgumentException("Duplicate mapped target parameter: " + entry.target());
            }
        }
    }

    private static <T> void set(xiaojin.gachaaddiction.reward.parameter.GachaParameterEditor editor,
                                GachaParameterDefinition<T> definition, Object value) {
        editor.set(definition, definition.type().valueClass().cast(value));
    }

    private static void requireSameType(GachaParameterDefinition<?> source, GachaParameterDefinition<?> target) {
        requireType(source.type(), target);
    }

    private static void requireType(xiaojin.gachaaddiction.reward.parameter.GachaParameterType<?> type,
                                    GachaParameterDefinition<?> target) {
        if (!type.id().equals(target.type().id())) {
            throw new IllegalArgumentException("Mapped parameter type mismatch for " + target.key()
                    + ": expected " + target.type().id() + ", got " + type.id());
        }
    }

    public GachaParameterSet apply(GachaParameterSchema targetSchema, GachaNumberContext context) {
        Objects.requireNonNull(targetSchema, "targetSchema");
        Objects.requireNonNull(context, "context");
        GachaParameterSet.Builder mapped = GachaParameterSet.builder();
        for (GachaParameterMappingEntry entry : entries) {
            GachaParameterDefinition<?> definition = targetSchema.require(entry.target());
            if (definition.direction() == GachaParameterDirection.OUTPUT) {
                throw new IllegalArgumentException("Cannot map an input value to output parameter: " + entry.target());
            }
            mapped.put(definition, entry.value().resolve(context));
        }
        return targetSchema.normalizeInputs(mapped.build());
    }

    public void validate(GachaParameterSchema sourceSchema, GachaParameterSchema targetSchema) {
        Objects.requireNonNull(sourceSchema, "sourceSchema");
        Objects.requireNonNull(targetSchema, "targetSchema");
        Set<Object> mappedTargets = new HashSet<>();
        for (GachaParameterMappingEntry entry : entries) {
            GachaParameterDefinition<?> target = targetSchema.require(entry.target());
            mappedTargets.add(target.key());
            GachaParameterDefinition<?> source = null;
            if (entry.value() instanceof GachaDirectParameterValue(GachaParameterKey source1)) {
                source = sourceSchema.require(source1);
                requireSameType(source, target);
            } else if (entry.value() instanceof GachaConstantParameterValue<?> constant) {
                requireType(constant.type(), target);
            } else if (entry.value() instanceof GachaLongParameterValue) {
                requireType(GachaParameterTypes.LONG, target);
            } else if (entry.value() instanceof GachaDoubleParameterValue) {
                requireType(GachaParameterTypes.DOUBLE, target);
            }
            if (target.direction() != GachaParameterDirection.INPUT) {
                if (source == null) {
                    throw new IllegalArgumentException("Output parameter requires a direct mapping: " + target.key());
                }
                if (source.direction() == GachaParameterDirection.INPUT) {
                    throw new IllegalArgumentException("Mapped output cannot write an input parameter: " + source.key());
                }
            }
        }
        for (GachaParameterDefinition<?> target : targetSchema.definitions()) {
            if (target.direction() != GachaParameterDirection.OUTPUT && target.required()
                    && target.defaultValue().isEmpty() && !mappedTargets.contains(target.key())) {
                throw new IllegalArgumentException("Required target parameter is not mapped: " + target.key());
            }
        }
    }

    public void applyOutputs(GachaParameterSchema targetSchema, GachaParameterSet targetValues,
                             GachaParameterSchema sourceSchema, xiaojin.gachaaddiction.reward.parameter.GachaParameterEditor source) {
        Objects.requireNonNull(targetSchema, "targetSchema");
        Objects.requireNonNull(targetValues, "targetValues");
        Objects.requireNonNull(sourceSchema, "sourceSchema");
        Objects.requireNonNull(source, "source");
        for (GachaParameterMappingEntry entry : entries) {
            GachaParameterDefinition<?> target = targetSchema.require(entry.target());
            if (target.direction() == GachaParameterDirection.INPUT) {
                continue;
            }
            if (!(entry.value() instanceof GachaDirectParameterValue(GachaParameterKey source1))) {
                throw new IllegalArgumentException("Output parameter requires a direct mapping: " + entry.target());
            }
            Object value = targetValues.get(entry.target()).orElse(null);
            if (value != null) {
                set(source, sourceSchema.require(source1), value);
            } else {
                source.remove(sourceSchema.require(source1));
            }
        }
    }
}

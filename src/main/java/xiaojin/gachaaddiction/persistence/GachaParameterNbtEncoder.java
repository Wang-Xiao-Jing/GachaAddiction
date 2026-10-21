package xiaojin.gachaaddiction.persistence;

import com.mojang.serialization.DynamicOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import xiaojin.gachaaddiction.reward.parameter.*;

import java.util.Map;
import java.util.Objects;

public final class GachaParameterNbtEncoder {
    private static final String OWNER_KEY = "owner";
    private static final String NAME_KEY = "name";
    private static final String TYPE_KEY = "type";
    private static final String VALUE_KEY = "value";
    private static final String VALUES_KEY = "values";
    private static final String OPERATIONS_KEY = "operations";
    private static final String BEFORE_KEY = "before";
    private static final String AFTER_KEY = "after";
    private static final String INPUT_FINGERPRINT_KEY = "input_fingerprint";

    private final GachaParameterSchema schema;
    private final DynamicOps<Tag> ops;

    public GachaParameterNbtEncoder(GachaParameterSchema schema, HolderLookup.Provider registries) {
        this.schema = Objects.requireNonNull(schema, "schema");
        this.ops = RegistryOps.create(NbtOps.INSTANCE, Objects.requireNonNull(registries, "registries"));
    }

    private static CompoundTag encodeKey(GachaParameterKey key) {
        CompoundTag encoded = new CompoundTag();
        encoded.putString(OWNER_KEY, key.owner().toString());
        encoded.putString(NAME_KEY, key.name());
        return encoded;
    }

    public CompoundTag encode(GachaParameterSet parameters) {
        CompoundTag encoded = new CompoundTag();
        ListTag values = new ListTag();
        parameters.asMap().entrySet().stream().sorted(Map.Entry.comparingByKey())
                .map(entry -> encodeEntry(entry.getKey(), entry.getValue())).forEach(values::add);
        encoded.put(VALUES_KEY, values);
        return encoded;
    }

    public CompoundTag encode(GachaParameterChangeSet changes) {
        CompoundTag encoded = encode(changes.finalValues());
        encoded.putString(INPUT_FINGERPRINT_KEY, changes.inputFingerprint());
        ListTag operations = new ListTag();
        for (GachaParameterOperation operation : changes.operations()) {
            CompoundTag operationTag = encodeKey(operation.key());
            operation.before().ifPresent(value -> operationTag.put(BEFORE_KEY,
                    encodeValue(schema.require(operation.key()), value)));
            operation.after().ifPresent(value -> operationTag.put(AFTER_KEY,
                    encodeValue(schema.require(operation.key()), value)));
            operations.add(operationTag);
        }
        encoded.put(OPERATIONS_KEY, operations);
        return encoded;
    }

    private CompoundTag encodeEntry(GachaParameterKey key, Object value) {
        GachaParameterDefinition<?> definition = schema.require(key);
        CompoundTag encoded = encodeKey(key);
        encoded.putString(TYPE_KEY, definition.type().id().toString());
        encoded.put(VALUE_KEY, encodeValue(definition, value));
        return encoded;
    }

    private Tag encodeValue(GachaParameterDefinition<?> definition, Object value) {
        return encodeTypedValue(definition, value);
    }

    private <T> Tag encodeTypedValue(GachaParameterDefinition<T> definition, Object value) {
        if (!definition.type().valueClass().isInstance(value)) {
            throw new IllegalArgumentException("Parameter value does not match schema: " + definition.key());
        }
        T typedValue = definition.type().valueClass().cast(value);
        return definition.type().codec().encodeStart(ops, typedValue).getOrThrow(IllegalArgumentException::new);
    }
}

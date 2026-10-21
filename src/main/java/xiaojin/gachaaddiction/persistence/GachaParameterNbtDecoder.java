package xiaojin.gachaaddiction.persistence;

import com.mojang.serialization.DynamicOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.reward.parameter.*;

import java.util.*;

public final class GachaParameterNbtDecoder {
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

    public GachaParameterNbtDecoder(GachaParameterSchema schema, HolderLookup.Provider registries) {
        this.schema = Objects.requireNonNull(schema, "schema");
        this.ops = RegistryOps.create(NbtOps.INSTANCE, Objects.requireNonNull(registries, "registries"));
    }

    private static GachaParameterKey decodeKey(CompoundTag encoded) {
        return new GachaParameterKey(ResourceLocation.parse(encoded.getString(OWNER_KEY)),
                encoded.getString(NAME_KEY));
    }

    public GachaParameterSet decodeSet(CompoundTag encoded) {
        return decodeSet(encoded, Integer.MAX_VALUE);
    }

    public GachaParameterSet decodeSet(CompoundTag encoded, int maximumEntries) {
        if (maximumEntries < 0) {
            throw new IllegalArgumentException("Maximum parameter entry count cannot be negative");
        }
        GachaParameterSet.Builder values = GachaParameterSet.builder();
        Set<GachaParameterKey> decodedKeys = new HashSet<>();
        ListTag entries = encoded.getList(VALUES_KEY, Tag.TAG_COMPOUND);
        if (entries.size() > maximumEntries) {
            throw new IllegalArgumentException("Encoded parameter set contains too many entries");
        }
        for (int index = 0; index < entries.size(); index++) {
            CompoundTag entry = entries.getCompound(index);
            GachaParameterKey key = decodeKey(entry);
            if (!decodedKeys.add(key)) {
                throw new IllegalArgumentException("Duplicate encoded parameter: " + key);
            }
            GachaParameterDefinition<?> definition = schema.require(key);
            ResourceLocation encodedType = ResourceLocation.parse(entry.getString(TYPE_KEY));
            if (!definition.type().id().equals(encodedType)) {
                throw new IllegalArgumentException("Stored parameter type does not match schema: " + key);
            }
            values.put(definition, decodeValue(definition, entry.get(VALUE_KEY)));
        }
        return values.build();
    }

    public GachaParameterChangeSet decodeChangeSet(CompoundTag encoded) {
        GachaParameterSet finalValues = decodeSet(encoded);
        List<GachaParameterOperation> operations = new ArrayList<>();
        ListTag encodedOperations = encoded.getList(OPERATIONS_KEY, Tag.TAG_COMPOUND);
        for (int index = 0; index < encodedOperations.size(); index++) {
            CompoundTag operation = encodedOperations.getCompound(index);
            GachaParameterKey key = decodeKey(operation);
            GachaParameterDefinition<?> definition = schema.require(key);
            Optional<Object> before = operation.contains(BEFORE_KEY)
                    ? Optional.of(decodeValue(definition, operation.get(BEFORE_KEY))) : Optional.empty();
            Optional<Object> after = operation.contains(AFTER_KEY)
                    ? Optional.of(decodeValue(definition, operation.get(AFTER_KEY))) : Optional.empty();
            operations.add(new GachaParameterOperation(key, before, after));
        }
        return new GachaParameterChangeSet(encoded.getString(INPUT_FINGERPRINT_KEY), operations, finalValues);
    }

    private Object decodeValue(GachaParameterDefinition<?> definition, Tag encoded) {
        if (encoded == null) {
            throw new IllegalArgumentException("Missing stored parameter value: " + definition.key());
        }
        return decodeTypedValue(definition, encoded);
    }

    private <T> T decodeTypedValue(GachaParameterDefinition<T> definition, Tag encoded) {
        return definition.type().codec().parse(ops, encoded).getOrThrow(IllegalArgumentException::new);
    }
}

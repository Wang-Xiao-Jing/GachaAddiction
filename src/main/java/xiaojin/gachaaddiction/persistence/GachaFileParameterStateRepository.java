package xiaojin.gachaaddiction.persistence;

import com.mojang.serialization.DynamicOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.cardpool.parameter.*;
import xiaojin.gachaaddiction.cardpool.transaction.GachaTransactionContext;
import xiaojin.gachaaddiction.cardpool.transaction.GachaTransactionId;
import xiaojin.gachaaddiction.reward.parameter.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.*;

public final class GachaFileParameterStateRepository implements IGachaParameterStateRepository {
    public static final int FORMAT_VERSION = 1;

    private static final String FORMAT_VERSION_KEY = "format_version";
    private static final String VALUES_KEY = "values";
    private static final String APPLIED_TRANSACTIONS_KEY = "applied_transactions";
    private static final String SOURCE_KEY = "source";
    private static final String CARD_POOL_KEY = "card_pool";
    private static final String PLAYER_KEY = "player";
    private static final String OWNER_KEY = "owner";
    private static final String NAME_KEY = "name";
    private static final String TYPE_KEY = "type";
    private static final String VALUE_KEY = "value";
    private static final String TRANSACTION_KEY = "transaction";
    private static final String TEMPORARY_FILE_PREFIX = "parameter-state-";
    private static final String TEMPORARY_FILE_SUFFIX = ".tmp";

    private final Path file;
    private final int maximumBytes;
    private final IGachaParameterFingerprint fingerprint;
    private final IGachaParameterTypeResolver parameterTypes;
    private final DynamicOps<Tag> ops;
    private final Map<GachaParameterStateAddress, StoredValue> values = new HashMap<>();
    private final Set<GachaTransactionId> appliedTransactions = new HashSet<>();
    private final Map<GachaTransactionId, List<StateUpdate>> pendingTransactions = new HashMap<>();
    private final Map<GachaParameterStateAddress, GachaTransactionId> reservedAddresses = new HashMap<>();

    public GachaFileParameterStateRepository(Path file, int maximumBytes,
                                             IGachaParameterFingerprint fingerprint,
                                             IGachaParameterTypeResolver parameterTypes,
                                             HolderLookup.Provider registries) throws IOException {
        this.file = Objects.requireNonNull(file, "file").toAbsolutePath().normalize();
        if (maximumBytes <= 0) {
            throw new IllegalArgumentException("Maximum parameter state size must be positive");
        }
        this.maximumBytes = maximumBytes;
        this.fingerprint = Objects.requireNonNull(fingerprint, "fingerprint");
        this.parameterTypes = Objects.requireNonNull(parameterTypes, "parameterTypes");
        this.ops = RegistryOps.create(NbtOps.INSTANCE, Objects.requireNonNull(registries, "registries"));
        Files.createDirectories(this.file.getParent());
        load();
    }

    private static CompoundTag encodeAddress(GachaParameterStateAddress address) {
        CompoundTag encoded = new CompoundTag();
        encoded.putString(SOURCE_KEY, address.source().name());
        address.cardPoolId().ifPresent(value -> encoded.putString(CARD_POOL_KEY, value.toString()));
        address.playerId().ifPresent(value -> encoded.putUUID(PLAYER_KEY, value));
        encoded.putString(OWNER_KEY, address.parameterKey().owner().toString());
        encoded.putString(NAME_KEY, address.parameterKey().name());
        return encoded;
    }

    private static GachaParameterStateAddress decodeAddress(CompoundTag encoded) {
        return new GachaParameterStateAddress(GachaParameterBindingSource.valueOf(encoded.getString(SOURCE_KEY)),
                encoded.contains(CARD_POOL_KEY) ? Optional.of(ResourceLocation.parse(encoded.getString(CARD_POOL_KEY)))
                        : Optional.empty(),
                encoded.hasUUID(PLAYER_KEY) ? Optional.of(encoded.getUUID(PLAYER_KEY)) : Optional.empty(),
                new GachaParameterKey(ResourceLocation.parse(encoded.getString(OWNER_KEY)),
                        encoded.getString(NAME_KEY)));
    }

    private static boolean isPersistent(GachaParameterBindingSource source) {
        return switch (source) {
            case PLAYER_BANNER, PLAYER_SHARED, SERVER_BANNER, SERVER_SHARED -> true;
            case CONSTANT, REQUEST -> false;
        };
    }

    @Override
    public synchronized <T> Optional<T> read(GachaParameterBinding<T> binding,
                                             GachaParameterBindingContext context) {
        if (!isPersistent(binding.source())) {
            return Optional.empty();
        }
        StoredValue stored = values.get(GachaParameterStateAddress.of(binding, context));
        if (stored == null) {
            return Optional.empty();
        }
        GachaParameterDefinition<T> definition = binding.parameter();
        if (!stored.type.equals(definition.type().id())
                || !definition.type().valueClass().isInstance(stored.value)) {
            throw new IllegalStateException("Stored value does not match parameter type: " + definition.key());
        }
        return Optional.of(definition.type().copy(definition.type().valueClass().cast(stored.value)));
    }

    @Override
    public synchronized void prepare(GachaTransactionContext context, GachaParameterSchema schema,
                                     List<GachaParameterBinding<?>> bindings,
                                     GachaParameterChangeSet changes) {
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(schema, "schema");
        Objects.requireNonNull(bindings, "bindings");
        Objects.requireNonNull(changes, "changes");
        if (appliedTransactions.contains(context.transactionId())
                || pendingTransactions.containsKey(context.transactionId())) {
            return;
        }
        GachaParameterSet initialValues = reconstructInitialValues(changes);
        if (!fingerprint.create(schema, initialValues).equals(changes.inputFingerprint())) {
            throw new IllegalArgumentException("Parameter change set input fingerprint does not match its values");
        }
        GachaParameterBindingContext bindingContext = new GachaParameterBindingContext(context.source().id(),
                context.playerId());
        List<StateUpdate> updates = new ArrayList<>();
        for (GachaParameterBinding<?> binding : bindings) {
            if (isPersistent(binding.source())
                    && binding.parameter().direction() != GachaParameterDirection.INPUT) {
                collectUpdate(binding, bindingContext, initialValues, changes.finalValues(), updates);
            }
        }
        for (StateUpdate update : updates) {
            GachaTransactionId owner = reservedAddresses.get(update.address);
            if (owner != null && !owner.equals(context.transactionId())) {
                throw new GachaStateConflictException(update.address.parameterKey());
            }
        }
        updates.forEach(update -> reservedAddresses.put(update.address, context.transactionId()));
        pendingTransactions.put(context.transactionId(), List.copyOf(updates));
    }

    @Override
    public synchronized void commitPrepared(GachaTransactionContext context) {
        if (appliedTransactions.contains(context.transactionId())) {
            return;
        }
        List<StateUpdate> updates = pendingTransactions.remove(context.transactionId());
        if (updates == null) {
            throw new IllegalStateException("Parameter state transaction was not prepared: "
                    + context.transactionId().value());
        }
        Map<GachaParameterStateAddress, StoredValue> previousValues = new HashMap<>(values);
        updates.forEach(this::apply);
        appliedTransactions.add(context.transactionId());
        try {
            save();
        } catch (RuntimeException failure) {
            values.clear();
            values.putAll(previousValues);
            appliedTransactions.remove(context.transactionId());
            pendingTransactions.put(context.transactionId(), updates);
            throw failure;
        }
        release(context.transactionId(), updates);
    }

    @Override
    public synchronized void rollbackPrepared(GachaTransactionContext context) {
        List<StateUpdate> updates = pendingTransactions.remove(context.transactionId());
        if (updates != null) {
            release(context.transactionId(), updates);
        }
    }

    @Override
    public synchronized void recover(GachaTransactionContext context, GachaParameterSchema schema,
                                     List<GachaParameterBinding<?>> bindings,
                                     GachaParameterChangeSet changes) {
        if (!appliedTransactions.contains(context.transactionId())) {
            prepare(context, schema, bindings, changes);
            commitPrepared(context);
        }
    }

    @Override
    public synchronized boolean wasApplied(GachaTransactionContext context) {
        return appliedTransactions.contains(context.transactionId());
    }

    private GachaParameterSet reconstructInitialValues(GachaParameterChangeSet changes) {
        GachaParameterSet.Builder initial = changes.finalValues().toBuilder();
        List<GachaParameterOperation> reversed = new ArrayList<>(changes.operations());
        Collections.reverse(reversed);
        for (GachaParameterOperation operation : reversed) {
            if (operation.before().isPresent()) {
                initial.put(operation.key(), operation.before().get());
            } else {
                initial.remove(operation.key());
            }
        }
        return initial.build();
    }

    private <T> void collectUpdate(GachaParameterBinding<T> binding,
                                   GachaParameterBindingContext context,
                                   GachaParameterSet initialValues, GachaParameterSet finalValues,
                                   List<StateUpdate> updates) {
        GachaParameterDefinition<T> definition = binding.parameter();
        Optional<T> expected = initialValues.get(definition).or(definition::defaultValue);
        Optional<T> current = read(binding, context).or(definition::defaultValue);
        if (!current.equals(expected)) {
            throw new GachaStateConflictException(definition.key());
        }
        Optional<StoredValue> replacement = finalValues.get(definition)
                .map(value -> new StoredValue(definition.type().id(), definition.type().copy(value)));
        updates.add(new StateUpdate(GachaParameterStateAddress.of(binding, context), replacement));
    }

    private void apply(StateUpdate update) {
        if (update.value.isPresent()) {
            values.put(update.address, update.value.get());
        } else {
            values.remove(update.address);
        }
    }

    private void release(GachaTransactionId transactionId, List<StateUpdate> updates) {
        updates.forEach(update -> reservedAddresses.remove(update.address, transactionId));
    }

    private void load() throws IOException {
        if (!Files.exists(file)) {
            return;
        }
        requireSize(Files.size(file));
        CompoundTag root;
        try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(Files.readAllBytes(file)))) {
            root = NbtIo.read(input, NbtAccounter.create(maximumBytes));
        }
        if (root.getInt(FORMAT_VERSION_KEY) != FORMAT_VERSION) {
            throw new IOException("Unsupported parameter state version: " + root.getInt(FORMAT_VERSION_KEY));
        }
        ListTag encodedValues = root.getList(VALUES_KEY, Tag.TAG_COMPOUND);
        for (int index = 0; index < encodedValues.size(); index++) {
            CompoundTag entry = encodedValues.getCompound(index);
            GachaParameterStateAddress address = decodeAddress(entry);
            ResourceLocation typeId = ResourceLocation.parse(entry.getString(TYPE_KEY));
            GachaParameterType<?> type = parameterTypes.resolve(typeId).orElseThrow(() ->
                    new IOException("Unknown stored parameter type: " + typeId));
            Object value = decodeValue(type, entry.get(VALUE_KEY));
            if (values.putIfAbsent(address, new StoredValue(typeId, value)) != null) {
                throw new IOException("Duplicate stored parameter state address: " + address);
            }
        }
        ListTag transactions = root.getList(APPLIED_TRANSACTIONS_KEY, Tag.TAG_COMPOUND);
        for (int index = 0; index < transactions.size(); index++) {
            GachaTransactionId transactionId = new GachaTransactionId(
                    transactions.getCompound(index).getUUID(TRANSACTION_KEY));
            if (!appliedTransactions.add(transactionId)) {
                throw new IOException("Duplicate applied parameter state transaction: " + transactionId.value());
            }
        }
    }

    private void save() {
        try {
            byte[] encoded = encode();
            requireSize(encoded.length);
            Path temporary = Files.createTempFile(file.getParent(), TEMPORARY_FILE_PREFIX, TEMPORARY_FILE_SUFFIX);
            try {
                try (FileChannel channel = FileChannel.open(temporary, StandardOpenOption.WRITE,
                        StandardOpenOption.TRUNCATE_EXISTING)) {
                    ByteBuffer buffer = ByteBuffer.wrap(encoded);
                    while (buffer.hasRemaining()) {
                        int written = channel.write(buffer);
                        if (written <= 0) {
                            throw new IOException("Unable to write parameter state");
                        }
                    }
                    channel.force(true);
                }
                try {
                    Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING,
                            StandardCopyOption.ATOMIC_MOVE);
                } catch (AtomicMoveNotSupportedException unsupported) {
                    Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
                }
            } finally {
                Files.deleteIfExists(temporary);
            }
        } catch (IOException failure) {
            throw new UncheckedIOException("Unable to persist gacha parameter state", failure);
        }
    }

    private byte[] encode() throws IOException {
        CompoundTag root = new CompoundTag();
        root.putInt(FORMAT_VERSION_KEY, FORMAT_VERSION);
        ListTag encodedValues = new ListTag();
        values.entrySet().stream().sorted(Comparator.comparing(entry -> entry.getKey().toString()))
                .forEach(entry -> encodedValues.add(encodeValue(entry.getKey(), entry.getValue())));
        root.put(VALUES_KEY, encodedValues);
        ListTag transactions = new ListTag();
        appliedTransactions.stream().sorted(Comparator.comparing(transaction -> transaction.value().toString()))
                .forEach(transaction -> {
                    CompoundTag entry = new CompoundTag();
                    entry.putUUID(TRANSACTION_KEY, transaction.value());
                    transactions.add(entry);
                });
        root.put(APPLIED_TRANSACTIONS_KEY, transactions);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream output = new DataOutputStream(bytes)) {
            NbtIo.write(root, output);
        }
        return bytes.toByteArray();
    }

    private CompoundTag encodeValue(GachaParameterStateAddress address, StoredValue stored) {
        CompoundTag encoded = encodeAddress(address);
        encoded.putString(TYPE_KEY, stored.type.toString());
        GachaParameterType<?> type = parameterTypes.resolve(stored.type).orElseThrow(() ->
                new IllegalStateException("Unknown parameter state type: " + stored.type));
        encoded.put(VALUE_KEY, encodeTypedValue(type, stored.value));
        return encoded;
    }

    private <T> Tag encodeTypedValue(GachaParameterType<T> type, Object value) {
        if (!type.valueClass().isInstance(value)) {
            throw new IllegalStateException("Stored parameter state value has the wrong type: " + type.id());
        }
        return type.codec().encodeStart(ops, type.valueClass().cast(value))
                .getOrThrow(IllegalStateException::new);
    }

    private <T> T decodeValue(GachaParameterType<T> type, Tag value) throws IOException {
        if (value == null) {
            throw new IOException("Stored parameter state value is missing");
        }
        try {
            return type.codec().parse(ops, value).getOrThrow(IllegalArgumentException::new);
        } catch (RuntimeException failure) {
            throw new IOException("Invalid stored parameter state value for type " + type.id(), failure);
        }
    }

    private void requireSize(long size) throws IOException {
        if (size <= 0 || size > maximumBytes) {
            throw new IOException("Parameter state file size is outside the configured limit: " + size);
        }
    }

    private record StoredValue(ResourceLocation type, Object value) {
    }

    private record StateUpdate(GachaParameterStateAddress address, Optional<StoredValue> value) {
    }
}

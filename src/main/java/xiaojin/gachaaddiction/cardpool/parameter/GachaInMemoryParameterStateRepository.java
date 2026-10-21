package xiaojin.gachaaddiction.cardpool.parameter;

import xiaojin.gachaaddiction.cardpool.transaction.GachaTransactionContext;
import xiaojin.gachaaddiction.cardpool.transaction.GachaTransactionId;
import xiaojin.gachaaddiction.reward.parameter.*;

import java.util.*;

public final class GachaInMemoryParameterStateRepository implements IGachaParameterStateRepository {
    private final IGachaParameterFingerprint fingerprint;
    private final Map<GachaParameterStateAddress, Object> values = new HashMap<>();
    private final Set<GachaTransactionId> appliedTransactions = new HashSet<>();
    private final Map<GachaTransactionId, List<StateUpdate>> pendingTransactions = new HashMap<>();
    private final Map<GachaParameterStateAddress, GachaTransactionId> reservedAddresses = new HashMap<>();

    public GachaInMemoryParameterStateRepository(IGachaParameterFingerprint fingerprint) {
        this.fingerprint = Objects.requireNonNull(fingerprint, "fingerprint");
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
        Object value = values.get(GachaParameterStateAddress.of(binding, context));
        if (value == null) {
            return Optional.empty();
        }
        GachaParameterDefinition<T> definition = binding.parameter();
        if (!definition.type().valueClass().isInstance(value)) {
            throw new IllegalStateException("Stored value does not match parameter type: " + definition.key());
        }
        return Optional.of(definition.type().copy(definition.type().valueClass().cast(value)));
    }

    @Override
    public synchronized void prepare(GachaTransactionContext context, GachaParameterSchema schema,
                                     List<GachaParameterBinding<?>> bindings,
                                     GachaParameterChangeSet changes) {
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(bindings, "bindings");
        Objects.requireNonNull(changes, "changes");
        if (appliedTransactions.contains(context.transactionId())) {
            return;
        }
        if (pendingTransactions.containsKey(context.transactionId())) {
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
        updates.forEach(this::apply);
        release(context.transactionId(), updates);
        appliedTransactions.add(context.transactionId());
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
        Optional<T> replacement = finalValues.get(definition);
        updates.add(new StateUpdate(GachaParameterStateAddress.of(binding, context),
                replacement.map(value -> (Object) definition.type().copy(value))));
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

    private record StateUpdate(GachaParameterStateAddress address, Optional<Object> value) {
    }
}

package xiaojin.gachaaddiction.cardpool;

import net.minecraft.world.item.ItemStack;
import xiaojin.gachaaddiction.cardpool.parameter.*;
import xiaojin.gachaaddiction.cardpool.transaction.*;
import xiaojin.gachaaddiction.execution.*;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterDefinition;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterDirection;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterKey;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public final class GachaCardPoolService {
    private final IGachaCardPoolDefinitionResolver cardPools;
    private final IGachaExecutionComponentResolver executionComponents;
    private final IGachaCardPoolSnapshotManager snapshots;
    private final GachaCardPoolParameterResolver parameterResolver;
    private final IGachaParameterStateRepository parameterState;
    private final IGachaParameterFingerprint parameterFingerprint;
    private final GachaTransactionCoordinator transactions;
    private final IGachaTransactionEffects effects;

    public GachaCardPoolService(IGachaCardPoolDefinitionResolver cardPools,
                                IGachaExecutionComponentResolver executionComponents,
                                IGachaCardPoolSnapshotManager snapshots,
                                GachaCardPoolParameterResolver parameterResolver,
                                IGachaParameterStateRepository parameterState,
                                IGachaParameterFingerprint parameterFingerprint,
                                GachaTransactionCoordinator transactions,
                                IGachaTransactionEffects effects) {
        this.cardPools = Objects.requireNonNull(cardPools, "cardPools");
        this.executionComponents = Objects.requireNonNull(executionComponents, "executionComponents");
        this.snapshots = Objects.requireNonNull(snapshots, "snapshots");
        this.parameterResolver = Objects.requireNonNull(parameterResolver, "parameterResolver");
        this.parameterState = Objects.requireNonNull(parameterState, "parameterState");
        this.parameterFingerprint = Objects.requireNonNull(parameterFingerprint, "parameterFingerprint");
        this.transactions = Objects.requireNonNull(transactions, "transactions");
        this.effects = Objects.requireNonNull(effects, "effects");
    }

    private static void validateBindings(GachaCardPoolDefinition cardPool,
                                         xiaojin.gachaaddiction.reward.parameter.GachaParameterSchema planSchema) {
        if (cardPool.parameterSchema().definitions().size() != planSchema.definitions().size()) {
            throw new IllegalArgumentException("Card pool parameter schema does not match execution plan schema: "
                    + cardPool.id());
        }
        for (GachaParameterDefinition<?> definition : cardPool.parameterSchema().definitions()) {
            GachaParameterDefinition<?> poolDefinition = planSchema.require(definition.key());
            if (!sameDefinition(definition, poolDefinition)) {
                throw new IllegalArgumentException("Card pool parameter schema does not match execution plan schema: "
                        + definition.key());
            }
        }
        Set<GachaParameterKey> bound = new HashSet<>();
        cardPool.parameterBindings().forEach(binding -> {
            GachaParameterDefinition<?> poolDefinition = planSchema.require(binding.parameter().key());
            if (!poolDefinition.type().id().equals(binding.parameter().type().id())
                    || poolDefinition.direction() != binding.parameter().direction()) {
                throw new IllegalArgumentException("Card pool parameter binding does not match execution plan schema: "
                        + binding.parameter().key());
            }
            bound.add(binding.parameter().key());
        });
        for (GachaParameterDefinition<?> definition : planSchema.definitions()) {
            if (definition.required() && definition.direction() != GachaParameterDirection.OUTPUT
                    && !bound.contains(definition.key())) {
                throw new IllegalArgumentException("Card pool does not bind required execution plan parameter: "
                        + definition.key());
            }
        }
    }

    private static boolean sameDefinition(GachaParameterDefinition<?> left,
                                          GachaParameterDefinition<?> right) {
        return left.type().id().equals(right.type().id())
                && left.direction() == right.direction()
                && left.required() == right.required()
                && left.defaultValue().equals(right.defaultValue());
    }

    public CompletionStage<GachaCommittedTransaction> draw(GachaCardPoolDrawRequest request) {
        return draw(request, Optional.empty());
    }

    public CompletionStage<GachaCommittedTransaction> draw(GachaCardPoolDrawRequest request,
                                                           Optional<String> expectedSnapshotHash) {
        return draw(request, expectedSnapshotHash, ItemStack.EMPTY);
    }

    public CompletionStage<GachaCommittedTransaction> draw(GachaCardPoolDrawRequest request,
                                                           Optional<String> expectedSnapshotHash,
                                                           ItemStack activationCost) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(expectedSnapshotHash, "expectedSnapshotHash");
        try {
            GachaCardPoolDefinition cardPool = cardPools.resolveCardPool(request.cardPoolId()).orElseThrow(() ->
                    new IllegalArgumentException("Unknown card pool: " + request.cardPoolId()));
            GachaDrawPackage drawPackage = cardPool.packages().get(request.packageId());
            if (drawPackage == null) {
                return CompletableFuture.failedFuture(new IllegalArgumentException("Unknown draw package: "
                        + request.packageId()));
            }
            var executionPlan = executionComponents.resolveExecutionPlan(drawPackage.executionPlan()).orElseThrow(() ->
                    new IllegalArgumentException("Unknown execution plan: " + drawPackage.executionPlan()));
            validateBindings(cardPool, executionPlan.parameterSchema());
            GachaParameterBindingContext bindingContext = new GachaParameterBindingContext(cardPool.id(),
                    request.playerId());
            GachaParameterResolution parameters = parameterResolver.resolve(cardPool.parameterBindings(),
                    cardPool.parameterSchema(), drawPackage.parameters(), request.requestParameters(),
                    request.authority(), bindingContext,
                    parameterState, parameterFingerprint);
            String snapshotHash = snapshots.store(cardPool);
            expectedSnapshotHash.ifPresent(expected -> {
                if (!expected.equals(snapshotHash)) {
                    throw new IllegalStateException("Card pool changed after draw confirmation was prepared");
                }
            });
            GachaTransactionContext transactionContext = new GachaTransactionContext(GachaTransactionId.create(),
                    request.playerId(), GachaCardPoolTransactionSource.create(
                    cardPool.id(), drawPackage.id(), snapshotHash,
                    GachaCardPoolTransactionSource.deliveryHandler(cardPool)));
            effects.validate(transactionContext);
            var executionResult = new GachaExecutionEngine(executionComponents).execute(drawPackage.executionPlan(),
                    new GachaExecutionRequest(new GachaExecutionSource(cardPool.id(), Optional.of(snapshotHash)),
                            GachaExecutionMode.EXECUTE, parameters.parameters(), parameters.inputFingerprint(),
                            request.lootParams(), request.lootTableResolver(), request.random(),
                            request.roundsOverride()));
            GachaDurability durability = GachaDurability.strongest(request.durability(),
                    drawPackage.minimumDurability());
            var consumption = GachaPlayerInventoryConsumptionHandler.withItemCost(
                    executionResult.consumption(), activationCost, request.lootParams().getLevel().registryAccess());
            return transactions.execute(transactionContext, consumption, durability,
                    () -> GachaTransactionResult.from(executionResult), effects);
        } catch (RuntimeException failure) {
            return CompletableFuture.failedFuture(failure);
        }
    }

    public GachaCardPoolDrawPreview preview(GachaCardPoolDrawRequest request, long previewSeed) {
        Objects.requireNonNull(request, "request");
        GachaCardPoolDefinition cardPool = cardPools.resolveCardPool(request.cardPoolId()).orElseThrow(() ->
                new IllegalArgumentException("Unknown card pool: " + request.cardPoolId()));
        GachaDrawPackage drawPackage = cardPool.packages().get(request.packageId());
        if (drawPackage == null) {
            throw new IllegalArgumentException("Unknown draw package: " + request.packageId());
        }
        var executionPlan = executionComponents.resolveExecutionPlan(drawPackage.executionPlan()).orElseThrow(() ->
                new IllegalArgumentException("Unknown execution plan: " + drawPackage.executionPlan()));
        validateBindings(cardPool, executionPlan.parameterSchema());
        GachaParameterResolution parameters = parameterResolver.resolve(cardPool.parameterBindings(),
                cardPool.parameterSchema(), drawPackage.parameters(), request.requestParameters(),
                request.authority(), new GachaParameterBindingContext(cardPool.id(), request.playerId()),
                parameterState, parameterFingerprint);
        return new GachaCardPoolDrawPreview(cardPool.id(), drawPackage.id(),
                new GachaExecutionPreviewService(executionComponents).preview(
                        drawPackage.executionPlan(), parameters.parameters(), previewSeed,
                        request.roundsOverride()));
    }
}

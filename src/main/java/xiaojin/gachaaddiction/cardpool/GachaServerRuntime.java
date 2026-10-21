package xiaojin.gachaaddiction.cardpool;

import net.minecraft.core.HolderLookup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import xiaojin.gachaaddiction.cardpool.delivery.GachaRewardInbox;
import xiaojin.gachaaddiction.cardpool.parameter.GachaCardPoolParameterResolver;
import xiaojin.gachaaddiction.cardpool.parameter.GachaParameterStateRepository;
import xiaojin.gachaaddiction.cardpool.transaction.*;
import xiaojin.gachaaddiction.data.GachaDefinitionRepository;
import xiaojin.gachaaddiction.persistence.*;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterTypeRegistry;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.Predicate;

public final class GachaServerRuntime implements AutoCloseable {
    public static final String WAL_FILE_NAME = "transactions.wal";
    public static final String SNAPSHOT_DIRECTORY_NAME = "pool_snapshots";
    public static final String INBOX_DIRECTORY_NAME = "reward_inbox";
    public static final String DELIVERY_LEDGER_DIRECTORY_NAME = "reward_delivery_ledger";
    public static final String PARAMETER_STATE_FILE_NAME = "parameter_state.nbt";

    private final GachaFileTransactionJournal journal;
    private final GachaTransactionRecovery recovery;
    private final GachaCardPoolService cardPoolService;
    private final GachaDurability recoveryDurability;
    private final GachaRewardInbox rewardInbox;
    private final GachaRewardDeliveryServices rewardDeliveryServices;
    private final GachaCardPoolSnapshotManager snapshots;
    private final GachaParameterStateRepository parameterState;
    private final GachaPendingDrawService pendingDraws;
    private CompletionStage<Void> recoveryTail = CompletableFuture.completedFuture(null);

    private GachaServerRuntime(GachaFileTransactionJournal journal, GachaTransactionRecovery recovery,
                               GachaCardPoolService cardPoolService, GachaDurability recoveryDurability,
                               GachaRewardInbox rewardInbox, GachaRewardDeliveryServices rewardDeliveryServices,
                               GachaCardPoolSnapshotManager snapshots,
                               GachaParameterStateRepository parameterState,
                               GachaPendingDrawService pendingDraws) {
        this.journal = journal;
        this.recovery = recovery;
        this.cardPoolService = cardPoolService;
        this.recoveryDurability = recoveryDurability;
        this.rewardInbox = rewardInbox;
        this.rewardDeliveryServices = rewardDeliveryServices;
        this.snapshots = snapshots;
        this.parameterState = parameterState;
        this.pendingDraws = pendingDraws;
    }

    public static GachaServerRuntime open(Path storageDirectory, HolderLookup.Provider registries,
                                          MinecraftServer server,
                                          GachaParameterTypeRegistry parameterTypes,
                                          GachaConsumptionHandlerResolver consumptionHandlers,
                                          GachaRewardDeliveryHandlerResolver rewardDeliveryHandlers,
                                          GachaDefinitionRepository definitions,
                                          Executor mainThreadExecutor,
                                          GachaRuntimeSettings settings) throws IOException {
        Objects.requireNonNull(storageDirectory, "storageDirectory");
        Objects.requireNonNull(registries, "registries");
        Objects.requireNonNull(server, "server");
        Objects.requireNonNull(parameterTypes, "parameterTypes");
        Objects.requireNonNull(consumptionHandlers, "consumptionHandlers");
        Objects.requireNonNull(rewardDeliveryHandlers, "rewardDeliveryHandlers");
        Objects.requireNonNull(definitions, "definitions");
        Objects.requireNonNull(mainThreadExecutor, "mainThreadExecutor");
        Objects.requireNonNull(settings, "settings");
        Path normalizedStorage = storageDirectory.toAbsolutePath().normalize();
        GachaCardPoolSnapshotStore snapshots = new GachaCardPoolSnapshotStore(
                new GachaFilePoolSnapshotArchive(normalizedStorage.resolve(SNAPSHOT_DIRECTORY_NAME),
                        settings.maximumSnapshotBytes()),
                new GachaCardPoolNbtSnapshotCodec(registries, parameterTypes));
        GachaFileTransactionJournal journal = new GachaFileTransactionJournal(
                normalizedStorage.resolve(WAL_FILE_NAME), settings.maximumWalPayloadBytes(),
                settings.groupCommitDelay());
        try {
            GachaRewardInbox rewardInbox = new GachaFileRewardInbox(
                    normalizedStorage.resolve(INBOX_DIRECTORY_NAME), settings.maximumInboxEntryBytes(), registries);
            GachaFileRewardDeliveryLedger deliveryLedger = new GachaFileRewardDeliveryLedger(
                    normalizedStorage.resolve(DELIVERY_LEDGER_DIRECTORY_NAME),
                    settings.maximumInboxEntryBytes(), registries);
            GachaSha256ParameterFingerprint fingerprint = new GachaSha256ParameterFingerprint(registries);
            GachaParameterStateRepository parameterState = new GachaFileParameterStateRepository(
                    normalizedStorage.resolve(PARAMETER_STATE_FILE_NAME), settings.maximumParameterStateBytes(),
                    fingerprint, parameterTypes, registries);
            GachaPendingDrawService pendingDraws = new GachaPendingDrawService(
                    settings.publicDrawConfirmationLifetime());
            GachaParameterStateTransactionParticipant participant =
                    new GachaParameterStateTransactionParticipant(snapshots, parameterState);
            GachaRewardDeliveryServices rewardDeliveryServices =
                    new GachaRewardDeliveryServices(server, rewardInbox, deliveryLedger);
            GachaCardPoolTransactionEffects effects = new GachaCardPoolTransactionEffects(rewardDeliveryHandlers,
                    rewardDeliveryServices);
            GachaCardPoolTransactionSchemaResolver transactionSchemas =
                    new GachaCardPoolTransactionSchemaResolver(snapshots);
            GachaTransactionNbtPayloadEncoder payloadEncoder =
                    new GachaTransactionNbtPayloadEncoder(registries, transactionSchemas);
            GachaTransactionCoordinator coordinator = new GachaTransactionCoordinator(journal, consumptionHandlers,
                    payloadEncoder, participant, mainThreadExecutor);
            GachaTransactionRecovery recovery = new GachaTransactionRecovery(journal, consumptionHandlers,
                    new GachaTransactionNbtPayloadDecoder(registries, transactionSchemas), participant, effects,
                    mainThreadExecutor);
            GachaCardPoolService service = new GachaCardPoolService(definitions, definitions, snapshots,
                    new GachaCardPoolParameterResolver(), parameterState, fingerprint, coordinator, effects);
            return new GachaServerRuntime(journal, recovery, service, settings.recoveryDurability(), rewardInbox,
                    rewardDeliveryServices, snapshots, parameterState, pendingDraws);
        } catch (IOException | RuntimeException | Error failure) {
            journal.close();
            throw failure;
        }
    }

    public synchronized CompletionStage<Void> recover() {
        return enqueueRecovery(context -> true);
    }

    public synchronized CompletionStage<Void> recover(Predicate<GachaTransactionContext> commitEligibility) {
        return enqueueRecovery(Objects.requireNonNull(commitEligibility, "commitEligibility"));
    }

    private CompletionStage<Void> enqueueRecovery(Predicate<GachaTransactionContext> commitEligibility) {
        recoveryTail = recoveryTail.handle((ignored, failure) -> null)
                .thenCompose(ignored -> recovery.recover(recoveryDurability, commitEligibility));
        return recoveryTail;
    }

    public GachaCardPoolService cardPoolService() {
        return cardPoolService;
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
        ItemStack cost = activationCost.copy();
        return recover(context -> context.playerId().equals(request.playerId()))
                .thenCompose(ignored -> cardPoolService.draw(request, expectedSnapshotHash, cost));
    }

    public GachaCardPoolDrawPreview preview(GachaCardPoolDrawRequest request, long previewSeed) {
        return cardPoolService.preview(Objects.requireNonNull(request, "request"), previewSeed);
    }

    public GachaRewardInbox rewardInbox() {
        return rewardInbox;
    }

    public void reconcilePlayerDeliveries(ServerPlayer player) {
        rewardDeliveryServices.reconcilePlayer(player);
    }

    public void acknowledgePlayerSave(ServerPlayer player) {
        rewardDeliveryServices.acknowledgePlayerSave(player);
    }

    public GachaParameterStateRepository parameterState() {
        return parameterState;
    }


    public GachaPendingDrawService pendingDraws() {
        return pendingDraws;
    }

    public Optional<GachaCardPoolDefinition> resolveSnapshot(String contentHash) {
        return snapshots.resolveSnapshot(contentHash);
    }

    public String storeSnapshot(GachaCardPoolDefinition cardPool) {
        return snapshots.store(Objects.requireNonNull(cardPool, "cardPool"));
    }

    @Override
    public void close() {
        journal.close();
    }
}

package xiaojin.gachaaddiction.cardpool;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import xiaojin.gachaaddiction.cardpool.parameter.GachaParameterBinding;
import xiaojin.gachaaddiction.cardpool.parameter.GachaPlayerChoiceLimits;
import xiaojin.gachaaddiction.cardpool.parameter.GachaRequestAuthority;
import xiaojin.gachaaddiction.cardpool.transaction.GachaCommittedTransaction;
import xiaojin.gachaaddiction.config.GachaAddictionConfig;
import xiaojin.gachaaddiction.data.GachaDefinitionRepository;
import xiaojin.gachaaddiction.persistence.GachaParameterNbtDecoder;
import xiaojin.gachaaddiction.reward.IGachaLootTableResolver;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterSet;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public final class GachaPublicCardPoolEndpoint {
    private GachaPublicCardPoolEndpoint() {
    }

    public static GachaPreparedCardPoolDraw prepare(ServerPlayer player, ResourceLocation cardPoolId,
                                                    ResourceLocation packageId,
                                                    CompoundTag encodedPlayerChoices) {
        return prepare(player, cardPoolId, packageId, OptionalInt.empty(), encodedPlayerChoices);
    }

    public static GachaPreparedCardPoolDraw prepare(ServerPlayer player, ResourceLocation cardPoolId,
                                                    ResourceLocation packageId, int rounds,
                                                    CompoundTag encodedPlayerChoices) {
        return prepare(player, cardPoolId, packageId, OptionalInt.of(rounds), encodedPlayerChoices);
    }

    private static GachaPreparedCardPoolDraw prepare(ServerPlayer player, ResourceLocation cardPoolId,
                                                     ResourceLocation packageId, OptionalInt roundsOverride,
                                                     CompoundTag encodedPlayerChoices) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(cardPoolId, "cardPoolId");
        Objects.requireNonNull(packageId, "packageId");
        Objects.requireNonNull(encodedPlayerChoices, "encodedPlayerChoices");
        GachaServerRuntime runtime = GachaServerRuntimeManager.current().orElseThrow(() ->
                new IllegalStateException("Gacha server runtime is unavailable"));
        GachaCardPoolDefinition cardPool = requireAvailableCardPool(player, cardPoolId);
        GachaDrawPackage drawPackage = cardPool.packages().get(packageId);
        if (drawPackage == null) {
            throw new IllegalArgumentException("Unknown card pool draw package: " + packageId);
        }
        GachaParameterSet playerChoices = validateEncodedPlayerChoices(player.serverLevel(), cardPool,
                encodedPlayerChoices);
        GachaCardPoolDrawRequest request = createRequest(player, cardPoolId, packageId, playerChoices,
                roundsOverride);
        GachaCardPoolDrawPreview preview = runtime.preview(request,
                previewSeed(player, cardPoolId, packageId, roundsOverride));
        return runtime.pendingDraws().prepare(player.getUUID(), cardPoolId, packageId, encodedPlayerChoices,
                runtime.storeSnapshot(cardPool),
                cardPool.presentation().titleTranslationKey(), preview, roundsOverride);
    }

    public static CompletionStage<GachaCommittedTransaction> confirm(ServerPlayer player, UUID token) {
        return confirm(player, token, ItemStack.EMPTY);
    }

    public static CompletionStage<GachaCommittedTransaction> confirm(ServerPlayer player, UUID token,
                                                                     ItemStack activationCost) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(token, "token");
        GachaServerRuntime runtime = GachaServerRuntimeManager.current().orElse(null);
        if (runtime == null) {
            return CompletableFuture.failedFuture(new IllegalStateException("Gacha server runtime is unavailable"));
        }
        GachaPreparedCardPoolDraw prepared;
        try {
            prepared = runtime.pendingDraws().peek(token, player.getUUID()).orElseThrow(() ->
                    new IllegalArgumentException("Pending draw is missing, expired, consumed, or belongs to another player"));
            GachaCardPoolDefinition current = requireAvailableCardPool(player, prepared.cardPoolId());
            if (!runtime.storeSnapshot(current).equals(prepared.cardPoolSnapshotHash())) {
                throw new IllegalStateException("Card pool changed after draw confirmation was prepared");
            }
            runtime.pendingDraws().consume(token, player.getUUID());
        } catch (RuntimeException failure) {
            return CompletableFuture.failedFuture(failure);
        }
        try {
            return draw(player, prepared.cardPoolId(), prepared.packageId(), prepared.roundsOverride(),
                    prepared.playerChoices(), Optional.of(prepared.cardPoolSnapshotHash()), activationCost)
                    .whenComplete((ignored, failure) -> {
                        if (failure != null) {
                            runtime.pendingDraws().restoreAfterFailure(prepared);
                        }
                    });
        } catch (RuntimeException failure) {
            runtime.pendingDraws().restoreAfterFailure(prepared);
            return CompletableFuture.failedFuture(failure);
        }
    }

    public static CompletionStage<GachaCommittedTransaction> draw(ServerPlayer player,
                                                                  ResourceLocation cardPoolId,
                                                                  ResourceLocation packageId) {
        return draw(player, cardPoolId, packageId, new CompoundTag());
    }

    public static CompletionStage<GachaCommittedTransaction> draw(ServerPlayer player,
                                                                  ResourceLocation cardPoolId,
                                                                  ResourceLocation packageId,
                                                                  CompoundTag encodedPlayerChoices) {
        return draw(player, cardPoolId, packageId, OptionalInt.empty(), encodedPlayerChoices);
    }

    public static CompletionStage<GachaCommittedTransaction> draw(ServerPlayer player,
                                                                  ResourceLocation cardPoolId,
                                                                  ResourceLocation packageId,
                                                                  int rounds,
                                                                  CompoundTag encodedPlayerChoices) {
        return draw(player, cardPoolId, packageId, OptionalInt.of(rounds), encodedPlayerChoices);
    }

    private static CompletionStage<GachaCommittedTransaction> draw(ServerPlayer player,
                                                                   ResourceLocation cardPoolId,
                                                                   ResourceLocation packageId,
                                                                   OptionalInt roundsOverride,
                                                                   CompoundTag encodedPlayerChoices) {
        return draw(player, cardPoolId, packageId, roundsOverride, encodedPlayerChoices, Optional.empty());
    }

    private static CompletionStage<GachaCommittedTransaction> draw(ServerPlayer player,
                                                                   ResourceLocation cardPoolId,
                                                                   ResourceLocation packageId,
                                                                   OptionalInt roundsOverride,
                                                                   CompoundTag encodedPlayerChoices,
                                                                   Optional<String> expectedSnapshotHash) {
        return draw(player, cardPoolId, packageId, roundsOverride, encodedPlayerChoices, expectedSnapshotHash,
                ItemStack.EMPTY);
    }

    private static CompletionStage<GachaCommittedTransaction> draw(ServerPlayer player,
                                                                   ResourceLocation cardPoolId,
                                                                   ResourceLocation packageId,
                                                                   OptionalInt roundsOverride,
                                                                   CompoundTag encodedPlayerChoices,
                                                                   Optional<String> expectedSnapshotHash,
                                                                   ItemStack activationCost) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(cardPoolId, "cardPoolId");
        Objects.requireNonNull(packageId, "packageId");
        Objects.requireNonNull(encodedPlayerChoices, "encodedPlayerChoices");
        Objects.requireNonNull(expectedSnapshotHash, "expectedSnapshotHash");
        GachaServerRuntime runtime = GachaServerRuntimeManager.current().orElse(null);
        if (runtime == null) {
            return CompletableFuture.failedFuture(new IllegalStateException("Gacha server runtime is unavailable"));
        }
        GachaCardPoolDefinition cardPool;
        try {
            cardPool = requireAvailableCardPool(player, cardPoolId);
        } catch (RuntimeException failure) {
            return CompletableFuture.failedFuture(failure);
        }
        GachaParameterSet playerChoices;
        try {
            playerChoices = validateEncodedPlayerChoices(player.serverLevel(), cardPool, encodedPlayerChoices);
        } catch (RuntimeException failure) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Invalid card pool player choices",
                    failure));
        }
        return runtime.draw(createRequest(player, cardPoolId, packageId, playerChoices, roundsOverride),
                expectedSnapshotHash, activationCost);
    }

    private static GachaCardPoolDrawRequest createRequest(ServerPlayer player, ResourceLocation cardPoolId,
                                                          ResourceLocation packageId,
                                                          GachaParameterSet playerChoices,
                                                          OptionalInt roundsOverride) {
        ServerLevel level = player.serverLevel();
        var lootTables = level.getServer().reloadableRegistries().get().registryOrThrow(Registries.LOOT_TABLE);
        IGachaLootTableResolver lootTableResolver = key -> resolve(lootTables.get(key));
        LootParams lootParams = new LootParams.Builder(level)
                .withParameter(LootContextParams.THIS_ENTITY, player)
                .withParameter(LootContextParams.ORIGIN, player.position())
                .create(LootContextParamSets.COMMAND);
        return new GachaCardPoolDrawRequest(player.getUUID(), cardPoolId, packageId,
                playerChoices, GachaRequestAuthority.PLAYER,
                GachaAddictionConfig.SERVER.publicDrawDurability.get(), lootParams, lootTableResolver,
                player.getRandom(), roundsOverride);
    }

    private static long previewSeed(ServerPlayer player, ResourceLocation cardPoolId, ResourceLocation packageId,
                                    OptionalInt roundsOverride) {
        UUID playerId = player.getUUID();
        return playerId.getMostSignificantBits() ^ playerId.getLeastSignificantBits()
                ^ cardPoolId.hashCode() ^ packageId.hashCode()
                ^ (roundsOverride.isPresent() ? roundsOverride.getAsInt() : 0);
    }

    private static GachaCardPoolDefinition requireAvailableCardPool(ServerPlayer player,
                                                                    ResourceLocation cardPoolId) {
        GachaCardPoolDefinition cardPool = GachaDefinitionRepository.INSTANCE.resolveCardPool(cardPoolId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown card pool: " + cardPoolId));
        if (!cardPool.availability().allows(player, Instant.now())) {
            throw new IllegalStateException("Card pool is not available: " + cardPoolId);
        }
        return cardPool;
    }

    private static GachaParameterSet validateEncodedPlayerChoices(ServerLevel level,
                                                                  GachaCardPoolDefinition cardPool,
                                                                  CompoundTag encodedPlayerChoices) {
        return validatePlayerChoices(cardPool,
                new GachaParameterNbtDecoder(cardPool.parameterSchema(), level.registryAccess())
                        .decodeSet(encodedPlayerChoices,
                                GachaPlayerChoiceLimits.MAXIMUM_CHOICES_PER_CARD_POOL));
    }

    private static GachaParameterSet validatePlayerChoices(GachaCardPoolDefinition cardPool,
                                                           GachaParameterSet requested) {
        for (var entry : requested.asMap().entrySet()) {
            GachaParameterBinding<?> binding = cardPool.parameterBindings().stream()
                    .filter(candidate -> candidate.parameter().key().equals(entry.getKey()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unbound card pool parameter: "
                            + entry.getKey()));
            if (binding.playerChoice().isEmpty()) {
                throw new IllegalArgumentException("Parameter is not player-selectable: " + entry.getKey());
            }
            requireAllowed(binding, entry.getValue());
        }
        return requested;
    }

    private static <T> void requireAllowed(GachaParameterBinding<T> binding, Object value) {
        if (!binding.parameter().type().valueClass().isInstance(value)) {
            throw new IllegalArgumentException("Player choice value has the wrong type: "
                    + binding.parameter().key());
        }
        binding.playerChoice().orElseThrow().requireAllowed(binding.parameter().type().valueClass().cast(value));
    }

    private static Optional<LootTable> resolve(LootTable table) {
        return table == null || table == LootTable.EMPTY ? Optional.empty() : Optional.of(table);
    }
}

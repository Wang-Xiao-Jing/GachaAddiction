package xiaojin.gachaaddiction.command;

import com.google.gson.JsonParser;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.neoforge.network.PacketDistributor;
import xiaojin.gachaaddiction.GachaAddiction;
import xiaojin.gachaaddiction.admin.GachaAdminCapability;
import xiaojin.gachaaddiction.cardpool.GachaPublicCardPoolCatalog;
import xiaojin.gachaaddiction.cardpool.GachaServerRuntimeManager;
import xiaojin.gachaaddiction.cardpool.delivery.GachaRewardInboxClaim;
import xiaojin.gachaaddiction.cardpool.transaction.GachaTransactionId;
import xiaojin.gachaaddiction.data.GachaDefinitionRepository;
import xiaojin.gachaaddiction.data.GachaRequestPresetCompiler;
import xiaojin.gachaaddiction.data.GachaRewardPoolRequestPreset;
import xiaojin.gachaaddiction.execution.GachaExecutionEngine;
import xiaojin.gachaaddiction.execution.GachaExecutionMode;
import xiaojin.gachaaddiction.execution.GachaExecutionRequest;
import xiaojin.gachaaddiction.execution.GachaExecutionSource;
import xiaojin.gachaaddiction.network.stc.OpenCardPoolCatalogPayload;
import xiaojin.gachaaddiction.network.stc.OpenLocalWorkbenchPayload;
import xiaojin.gachaaddiction.network.stc.OpenRewardInboxPayload;
import xiaojin.gachaaddiction.persistence.GachaSha256ParameterFingerprint;
import xiaojin.gachaaddiction.resourcepack.GachaResourcePackDescriptor;
import xiaojin.gachaaddiction.resourcepack.GachaVanillaResourcePackDispatcher;
import xiaojin.gachaaddiction.reward.GachaRewardPool;
import xiaojin.gachaaddiction.reward.GachaRewardPoolRequest;
import xiaojin.gachaaddiction.reward.GachaRewardPoolResult;
import xiaojin.gachaaddiction.reward.probability.GachaProbabilityDescribable;
import xiaojin.gachaaddiction.reward.probability.GachaProbabilityDescription;
import xiaojin.gachaaddiction.util.TranslationKeys;

import java.util.*;

public final class GachaCommand {
    private static final String RESOURCE_PACK_SENT_LOG = "{} sent resource pack {} to {} player(s)";
    private static final GachaVanillaResourcePackDispatcher RESOURCE_PACK_DISPATCHER =
            new GachaVanillaResourcePackDispatcher();

    private GachaCommand() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("gacha")
                .then(GachaDrawItemCommand.register())
                .then(Commands.literal("open")
                        .executes(context -> openCatalog(context.getSource())))
                .then(Commands.literal("inbox")
                        .executes(context -> openInbox(context.getSource()))
                        .then(Commands.literal("claim")
                                .then(Commands.argument("transaction", StringArgumentType.word())
                                        .executes(context -> claimInbox(context.getSource(),
                                                StringArgumentType.getString(context, "transaction"))))))
                .then(Commands.literal("reload")
                        .requires(GachaAdminCapability.MANAGE_ADMIN::allows)
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();
                            source.getServer().getCommands().performPrefixedCommand(source, "reload");
                            return 1;
                        }))
                .then(Commands.literal("admin")
                        .requires(GachaAdminCapability.VIEW_ADMIN_DATA::allows)
                        .then(Commands.literal("workbench")
                                .executes(context -> openAdminWorkbench(context.getSource())))
                        .then(Commands.literal("inbox")
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .executes(context -> listInboxes(context.getSource(),
                                                EntityArgument.getPlayers(context, "targets")))))
                        .then(Commands.literal("list")
                                .then(Commands.literal("reward_pools")
                                        .executes(context -> list(context.getSource(), "reward_pools",
                                                GachaDefinitionRepository.INSTANCE.compiled().rewardPools())))
                                .then(Commands.literal("card_pools")
                                        .executes(context -> list(context.getSource(), "card_pools",
                                                GachaDefinitionRepository.INSTANCE.compiled().cardPools())))
                                .then(Commands.literal("execution_plans")
                                        .executes(context -> list(context.getSource(), "execution_plans",
                                                GachaDefinitionRepository.INSTANCE.compiled().executionPlans())))
                                .then(Commands.literal("request_presets")
                                        .executes(context -> list(context.getSource(), "request_presets",
                                                GachaDefinitionRepository.INSTANCE.compiled().requestPresets()))))
                        .then(Commands.literal("describe")
                                .then(Commands.literal("probability")
                                        .then(Commands.argument("kind", StringArgumentType.word())
                                                .suggests((context, builder) -> {
                                                    builder.suggest("reward_pool");
                                                    builder.suggest("table");
                                                    return builder.buildFuture();
                                                })
                                                .then(Commands.argument("id", ResourceLocationArgument.id())
                                                        .executes(context -> describeProbability(
                                                                context.getSource(),
                                                                StringArgumentType.getString(context, "kind"),
                                                                ResourceLocationArgument.getId(context, "id")))))))
                        .then(Commands.literal("resource_pack")
                                .requires(GachaAdminCapability.MANAGE_ADMIN::allows)
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .then(Commands.argument("url", StringArgumentType.string())
                                                .then(Commands.argument("sha1", StringArgumentType.word())
                                                        .then(Commands.argument("required", BoolArgumentType.bool())
                                                                .executes(context -> sendExternalResourcePack(
                                                                        context.getSource(),
                                                                        EntityArgument.getPlayers(context, "targets"),
                                                                        StringArgumentType.getString(context, "url"),
                                                                        StringArgumentType.getString(context, "sha1"),
                                                                        BoolArgumentType.getBool(context,
                                                                                "required")))))))))
                .then(Commands.literal("test")
                        .requires(GachaAdminCapability.TEST_DRAW::allows)
                        .then(Commands.literal("reward_pool")
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .then(Commands.argument("preset", ResourceLocationArgument.id())
                                                .suggests((context, builder) -> {
                                                    GachaDefinitionRepository.INSTANCE.compiled().requestPresets()
                                                            .keySet().forEach(id -> builder.suggest(id.toString()));
                                                    return builder.buildFuture();
                                                })
                                                .executes(context -> executeRewardPoolTest(
                                                        context.getSource(),
                                                        EntityArgument.getPlayers(context, "targets"),
                                                        ResourceLocationArgument.getId(context, "preset"))))))
                        .then(Commands.literal("execution_plan")
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .then(Commands.argument("execution_plan", ResourceLocationArgument.id())
                                                .suggests((context, builder) -> {
                                                    GachaDefinitionRepository.INSTANCE.compiled().executionPlans()
                                                            .keySet().forEach(id -> builder.suggest(id.toString()));
                                                    return builder.buildFuture();
                                                })
                                                .then(Commands.argument("parameters",
                                                                StringArgumentType.greedyString())
                                                        .executes(context -> executeExecutionPlanTest(
                                                                context.getSource(),
                                                                EntityArgument.getPlayers(context, "targets"),
                                                                ResourceLocationArgument.getId(context,
                                                                        "execution_plan"),
                                                                StringArgumentType.getString(context,
                                                                        "parameters")))))))
                        .then(Commands.literal("execution_plan_rounds")
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .then(Commands.argument("execution_plan", ResourceLocationArgument.id())
                                                .suggests((context, builder) -> {
                                                    GachaDefinitionRepository.INSTANCE.compiled().executionPlans()
                                                            .keySet().forEach(id -> builder.suggest(id.toString()));
                                                    return builder.buildFuture();
                                                })
                                                .then(Commands.argument("rounds", IntegerArgumentType.integer(1))
                                                        .then(Commands.argument("parameters",
                                                                        StringArgumentType.greedyString())
                                                                .executes(context -> executeExecutionPlanTest(
                                                                        context.getSource(),
                                                                        EntityArgument.getPlayers(context,
                                                                                "targets"),
                                                                        ResourceLocationArgument.getId(context,
                                                                                "execution_plan"),
                                                                        IntegerArgumentType.getInteger(context,
                                                                                "rounds"),
                                                                        StringArgumentType.getString(context,
                                                                                "parameters"))))))))
                        .then(Commands.literal("reward_pool_direct")
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .then(Commands.argument("reward_pool", ResourceLocationArgument.id())
                                                .suggests((context, builder) -> {
                                                    GachaDefinitionRepository.INSTANCE.compiled().rewardPools()
                                                            .keySet().forEach(id -> builder.suggest(id.toString()));
                                                    return builder.buildFuture();
                                                })
                                                .then(Commands.argument("rounds", IntegerArgumentType.integer(1))
                                                        .then(Commands.argument("parameters",
                                                                        StringArgumentType.greedyString())
                                                                .executes(context -> executeDirectRewardPoolTest(
                                                                        context.getSource(),
                                                                        EntityArgument.getPlayers(context,
                                                                                "targets"),
                                                                        ResourceLocationArgument.getId(context,
                                                                                "reward_pool"),
                                                                        IntegerArgumentType.getInteger(context,
                                                                                "rounds"),
                                                                        StringArgumentType.getString(context,
                                                                                "parameters")))))))));
    }

    private static int openCatalog(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        PacketDistributor.sendToPlayer(player,
                new OpenCardPoolCatalogPayload(GachaPublicCardPoolCatalog.visibleTo(player)));
        return 1;
    }

    private static int openAdminWorkbench(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        if (!player.connection.hasChannel(OpenLocalWorkbenchPayload.TYPE)) {
            source.sendFailure(Component.translatable(
                    TranslationKeys.COMMANDS_GACHA_ADMIN_WORKBENCH_CLIENT_UNAVAILABLE));
            return 0;
        }
        PacketDistributor.sendToPlayer(player, OpenLocalWorkbenchPayload.INSTANCE);
        return 1;
    }


    private static int sendExternalResourcePack(CommandSourceStack source, Collection<ServerPlayer> targets,
                                                String url, String sha1, boolean required) {
        try {
            GachaResourcePackDescriptor descriptor = GachaResourcePackDescriptor.external(url, sha1, required);
            UUID requestId = RESOURCE_PACK_DISPATCHER.push(targets, descriptor);
            GachaAddiction.LOGGER.info(RESOURCE_PACK_SENT_LOG, source.getTextName(), requestId, targets.size());
            source.sendSuccess(() -> Component.translatable(TranslationKeys.COMMANDS_GACHA_RESOURCE_PACK_SENT,
                    requestId.toString(), targets.size()), true);
            return targets.size();
        } catch (RuntimeException failure) {
            source.sendFailure(Component.literal(failure.getMessage()));
            return 0;
        }
    }


    private static int openInbox(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        var runtime = GachaServerRuntimeManager.current().orElse(null);
        if (runtime == null) {
            source.sendFailure(Component.translatable(TranslationKeys.COMMANDS_GACHA_RUNTIME_UNAVAILABLE));
            return 0;
        }
        PacketDistributor.sendToPlayer(player,
                new OpenRewardInboxPayload(runtime.rewardInbox().pending(player.getUUID())));
        return 1;
    }

    private static int claimInbox(CommandSourceStack source, String encodedTransaction)
            throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        var runtime = GachaServerRuntimeManager.current().orElse(null);
        if (runtime == null) {
            source.sendFailure(Component.translatable(TranslationKeys.COMMANDS_GACHA_RUNTIME_UNAVAILABLE));
            return 0;
        }
        try {
            GachaTransactionId transactionId = new GachaTransactionId(UUID.fromString(encodedTransaction));
            int stackCount = GachaRewardInboxClaim.claim(player, runtime.rewardInbox(), transactionId);
            source.sendSuccess(() -> Component.translatable(TranslationKeys.COMMANDS_GACHA_INBOX_CLAIMED,
                    stackCount, transactionId.value().toString()), false);
            return stackCount;
        } catch (RuntimeException failure) {
            source.sendFailure(Component.translatable(TranslationKeys.COMMANDS_GACHA_INBOX_CLAIM_FAILED,
                    failure.getMessage()));
            return 0;
        }
    }

    private static int listInboxes(CommandSourceStack source, Collection<ServerPlayer> targets) {
        var runtime = GachaServerRuntimeManager.current().orElse(null);
        if (runtime == null) {
            source.sendFailure(Component.translatable(TranslationKeys.COMMANDS_GACHA_RUNTIME_UNAVAILABLE));
            return 0;
        }
        int entries = 0;
        for (ServerPlayer player : targets) {
            var pending = runtime.rewardInbox().pending(player.getUUID());
            entries += pending.size();
            source.sendSuccess(() -> Component.translatable(TranslationKeys.COMMANDS_GACHA_INBOX_HEADER,
                    player.getGameProfile().getName(), pending.size()), false);
            pending.forEach(entry -> source.sendSuccess(() -> Component.translatable(
                    TranslationKeys.COMMANDS_GACHA_INBOX_ENTRY, entry.transactionId().value().toString(),
                    entry.rewards().size()), false));
        }
        return entries;
    }

    private static int list(CommandSourceStack source, String definitionKind,
                            Map<ResourceLocation, ?> definitions) {
        source.sendSuccess(() -> Component.translatable(TranslationKeys.COMMANDS_GACHA_LIST_HEADER,
                definitionKind, definitions.size()), false);
        definitions.keySet().forEach(id -> source.sendSuccess(() -> Component.literal(id.toString()), false));
        return definitions.size();
    }

    private static int describeProbability(CommandSourceStack source, String kind, ResourceLocation id) {
        if (!kind.equals("reward_pool") && !kind.equals("table")) {
            source.sendFailure(Component.translatable(TranslationKeys.COMMANDS_GACHA_PROBABILITY_UNKNOWN_KIND, kind));
            return 0;
        }
        Optional<GachaProbabilityDescription> description = switch (kind) {
            case "reward_pool" -> GachaDefinitionRepository.INSTANCE.resolveRewardPool(id)
                    .filter(GachaProbabilityDescribable.class::isInstance)
                    .map(GachaProbabilityDescribable.class::cast)
                    .map(GachaProbabilityDescribable::describeProbability);
            case "table" -> GachaDefinitionRepository.INSTANCE.resolveTable(id)
                    .map(table -> table.describeProbability());
            default -> Optional.empty();
        };
        if (description.isEmpty()) {
            source.sendFailure(Component.translatable(TranslationKeys.COMMANDS_GACHA_PROBABILITY_NOT_FOUND,
                    id.toString()));
            return 0;
        }
        GachaProbabilityDescription value = description.get();
        source.sendSuccess(() -> Component.translatable(TranslationKeys.COMMANDS_GACHA_PROBABILITY_HEADER,
                value.id().toString(), value.selectionMode().name(), value.selectionCount().toString()), false);
        value.candidates().forEach(candidate -> source.sendSuccess(() -> Component.translatable(
                TranslationKeys.COMMANDS_GACHA_PROBABILITY_CANDIDATE,
                candidate.id().toString(), candidate.weight().toString()), false));
        return value.candidates().size();
    }

    private static int executeRewardPoolTest(CommandSourceStack source, Collection<ServerPlayer> targets,
                                             ResourceLocation presetId) {
        GachaRewardPoolRequestPreset preset = GachaDefinitionRepository.INSTANCE
                .resolveRequestPreset(presetId).orElse(null);
        if (preset == null) {
            source.sendFailure(Component.translatable(TranslationKeys.COMMANDS_GACHA_INVALID_REQUEST_PRESET,
                    presetId.toString()));
            return 0;
        }
        GachaRewardPool rewardPool = GachaDefinitionRepository.INSTANCE
                .resolveRewardPool(preset.rewardPool()).orElse(null);
        if (rewardPool == null) {
            source.sendFailure(Component.translatable(TranslationKeys.COMMANDS_GACHA_INVALID_REWARD_POOL,
                    preset.rewardPool().toString()));
            return 0;
        }
        ServerLevel level = source.getLevel();
        try {
            Registry<LootTable> lootTables = level.getServer().reloadableRegistries().get()
                    .registryOrThrow(Registries.LOOT_TABLE);
            GachaSha256ParameterFingerprint fingerprint = new GachaSha256ParameterFingerprint(level.registryAccess());
            for (ServerPlayer player : targets) {
                LootParams lootParams = new LootParams.Builder(level)
                        .withParameter(LootContextParams.THIS_ENTITY, player)
                        .withParameter(LootContextParams.ORIGIN, player.position())
                        .create(LootContextParamSets.COMMAND);
                GachaRewardPoolResult result = rewardPool.draw(new GachaRewardPoolRequest(preset.rounds(), lootParams,
                        preset.parameters(), fingerprint.create(rewardPool.parameterSchema(), preset.parameters()),
                        key -> resolve(lootTables, key),
                        player.getRandom()));
                result.rewards().forEach(stack -> deliver(player, stack));
            }
            source.sendSuccess(() -> Component.translatable(TranslationKeys.COMMANDS_GACHA_REWARD_POOL_TEST_SUCCESS,
                    targets.size(), presetId.toString()), true);
            return targets.size();
        } catch (RuntimeException failure) {
            source.sendFailure(Component.translatable(TranslationKeys.COMMANDS_GACHA_REWARD_POOL_TEST_FAILED,
                    failure.getMessage()));
            return 0;
        }
    }

    private static int executeDirectRewardPoolTest(CommandSourceStack source,
                                                   Collection<ServerPlayer> targets,
                                                   ResourceLocation rewardPoolId,
                                                   int rounds, String encodedParameters) {
        GachaRewardPool rewardPool = GachaDefinitionRepository.INSTANCE
                .resolveRewardPool(rewardPoolId).orElse(null);
        if (rewardPool == null) {
            source.sendFailure(Component.translatable(TranslationKeys.COMMANDS_GACHA_INVALID_REWARD_POOL,
                    rewardPoolId.toString()));
            return 0;
        }
        ServerLevel level = source.getLevel();
        try {
            var parameters = new GachaRequestPresetCompiler(level.registryAccess())
                    .decodeExternalParameters(rewardPoolId, rewardPool.parameterSchema(),
                            JsonParser.parseString(encodedParameters).getAsJsonObject());
            Registry<LootTable> lootTables = level.getServer().reloadableRegistries().get()
                    .registryOrThrow(Registries.LOOT_TABLE);
            GachaSha256ParameterFingerprint fingerprint =
                    new GachaSha256ParameterFingerprint(level.registryAccess());
            for (ServerPlayer player : targets) {
                LootParams lootParams = new LootParams.Builder(level)
                        .withParameter(LootContextParams.THIS_ENTITY, player)
                        .withParameter(LootContextParams.ORIGIN, player.position())
                        .create(LootContextParamSets.COMMAND);
                GachaRewardPoolResult result = rewardPool.draw(new GachaRewardPoolRequest(rounds, lootParams,
                        parameters, fingerprint.create(rewardPool.parameterSchema(), parameters),
                        key -> resolve(lootTables, key), player.getRandom()));
                result.rewards().forEach(stack -> deliver(player, stack));
            }
            source.sendSuccess(() -> Component.translatable(
                    TranslationKeys.COMMANDS_GACHA_REWARD_POOL_DIRECT_TEST_SUCCESS,
                    targets.size(), rewardPoolId.toString(), rounds), true);
            return targets.size();
        } catch (RuntimeException failure) {
            source.sendFailure(Component.translatable(
                    TranslationKeys.COMMANDS_GACHA_REWARD_POOL_DIRECT_TEST_FAILED, failure.getMessage()));
            return 0;
        }
    }

    private static int executeExecutionPlanTest(CommandSourceStack source,
                                                Collection<ServerPlayer> targets,
                                                ResourceLocation planId,
                                                String encodedParameters) {
        return executeExecutionPlanTest(source, targets, planId, OptionalInt.empty(), encodedParameters);
    }

    private static int executeExecutionPlanTest(CommandSourceStack source,
                                                Collection<ServerPlayer> targets,
                                                ResourceLocation planId,
                                                int rounds,
                                                String encodedParameters) {
        return executeExecutionPlanTest(source, targets, planId, OptionalInt.of(rounds), encodedParameters);
    }

    private static int executeExecutionPlanTest(CommandSourceStack source,
                                                Collection<ServerPlayer> targets,
                                                ResourceLocation planId,
                                                OptionalInt roundsOverride,
                                                String encodedParameters) {
        var plan = GachaDefinitionRepository.INSTANCE.resolveExecutionPlan(planId).orElse(null);
        if (plan == null) {
            source.sendFailure(Component.translatable(TranslationKeys.COMMANDS_GACHA_INVALID_EXECUTION_PLAN,
                    planId.toString()));
            return 0;
        }
        ServerLevel level = source.getLevel();
        try {
            var parameters = new GachaRequestPresetCompiler(level.registryAccess())
                    .decodeExternalParameters(planId, plan.parameterSchema(),
                            JsonParser.parseString(encodedParameters).getAsJsonObject());
            Registry<LootTable> lootTables = level.getServer().reloadableRegistries().get()
                    .registryOrThrow(Registries.LOOT_TABLE);
            GachaSha256ParameterFingerprint fingerprint =
                    new GachaSha256ParameterFingerprint(level.registryAccess());
            GachaExecutionEngine engine = new GachaExecutionEngine(GachaDefinitionRepository.INSTANCE);
            for (ServerPlayer player : targets) {
                LootParams lootParams = new LootParams.Builder(level)
                        .withParameter(LootContextParams.THIS_ENTITY, player)
                        .withParameter(LootContextParams.ORIGIN, player.position())
                        .create(LootContextParamSets.COMMAND);
                var result = engine.execute(planId, new GachaExecutionRequest(
                        GachaExecutionSource.live(planId), GachaExecutionMode.SIMULATE, parameters,
                        fingerprint.create(plan.parameterSchema(), parameters), lootParams,
                        key -> resolve(lootTables, key), player.getRandom(), roundsOverride));
                result.rewards().forEach(stack -> deliver(player, stack));
            }
            source.sendSuccess(() -> Component.translatable(
                    TranslationKeys.COMMANDS_GACHA_EXECUTION_PLAN_TEST_SUCCESS,
                    targets.size(), planId.toString()), true);
            return targets.size();
        } catch (RuntimeException failure) {
            source.sendFailure(Component.translatable(
                    TranslationKeys.COMMANDS_GACHA_EXECUTION_PLAN_TEST_FAILED, failure.getMessage()));
            return 0;
        }
    }

    private static Optional<LootTable> resolve(Registry<LootTable> lootTables, ResourceKey<LootTable> key) {
        LootTable table = lootTables.get(key);
        return table == null || table == LootTable.EMPTY ? Optional.empty() : Optional.of(table);
    }

    private static void deliver(ServerPlayer player, ItemStack reward) {
        ItemStack remainder = reward.copy();
        boolean inserted = player.getInventory().add(remainder);
        player.getInventory().setChanged();
        if (inserted && remainder.isEmpty()) {
            ItemEntity displayDrop = player.drop(reward.copy(), false);
            if (displayDrop != null) {
                displayDrop.makeFakeItem();
            }
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_PICKUP,
                    SoundSource.PLAYERS, 0.2F,
                    (player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7F + 1.0F);
            player.containerMenu.broadcastChanges();
            return;
        }
        ItemEntity drop = player.drop(remainder, false);
        if (drop != null) {
            drop.setNoPickUpDelay();
            drop.setTarget(player.getUUID());
        }
    }
}

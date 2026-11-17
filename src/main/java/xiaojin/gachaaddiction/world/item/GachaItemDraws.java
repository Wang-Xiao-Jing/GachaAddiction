package xiaojin.gachaaddiction.world.item;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import xiaojin.gachaaddiction.GachaConstants;
import xiaojin.gachaaddiction.api.GachaRandomType;
import xiaojin.gachaaddiction.api.GachaSecondaryType;
import xiaojin.gachaaddiction.api.GachaType;
import xiaojin.gachaaddiction.cardpool.GachaPreparedCardPoolDraw;
import xiaojin.gachaaddiction.cardpool.GachaPublicCardPoolEndpoint;
import xiaojin.gachaaddiction.cardpool.GachaServerRuntimeManager;
import xiaojin.gachaaddiction.data.GachaDefinitionRepository;
import xiaojin.gachaaddiction.data.GachaPossibleRewardItems;
import xiaojin.gachaaddiction.network.cts.FinishGachaItemDrawPayload;
import xiaojin.gachaaddiction.network.stc.GachaItemPresentationPayload;
import xiaojin.gachaaddiction.presentation.GachaPresentations;
import xiaojin.gachaaddiction.registry.GachaSources;
import xiaojin.gachaaddiction.util.TranslationKeys;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class GachaItemDraws {
    private static final String CARD_POOL_KEY = "card_pool";
    private static final String PACKAGE_KEY = "package";
    private static final String PREVIEW_KEY = "preview_items";
    private static final int MAXIMUM_PREVIEW_ITEMS = 24;
    private static final Map<UUID, Session> SESSIONS = new HashMap<>();

    public static boolean begin(ServerPlayer player, InteractionHand hand, GachaType animation) {
        Session previous = SESSIONS.get(player.getUUID());
        if (previous != null && !previous.committing && !Instant.now().isBefore(previous.prepared.expiresAt())) {
            forget(player.getUUID());
            previous = null;
        }
        if (previous != null || !player.isAlive() || player.isSpectator()
                || GachaPresentations.isForced(player)
                || !player.connection.hasChannel(GachaItemPresentationPayload.TYPE)
                || !player.connection.hasChannel(FinishGachaItemDrawPayload.TYPE)) {
            player.sendSystemMessage(Component.translatable(TranslationKeys.GUI_DRAW_ITEM_UNAVAILABLE));
            return false;
        }

        ItemStack cost = player.getItemInHand(hand).copyWithCount(1);
        try {
            CompoundTag binding = GachaDrawItemFactory.resolveBinding(cost);
            ResourceLocation pool = ResourceLocation.tryParse(binding.getString(CARD_POOL_KEY));
            ResourceLocation drawPackage = ResourceLocation.tryParse(binding.getString(PACKAGE_KEY));
            if (pool == null || drawPackage == null) {
                player.sendSystemMessage(Component.translatable(TranslationKeys.GUI_DRAW_ITEM_UNBOUND));
                return false;
            }

            GachaPreparedCardPoolDraw prepared = GachaPublicCardPoolEndpoint.prepare(player, pool, drawPackage,
                    new CompoundTag());
            Session session = new Session(UUID.randomUUID(), prepared, hand, cost, animation,
                    player.level().dimension().location());
            SESSIONS.put(player.getUUID(), session);
            long roundCount = prepared.preview().execution().steps().stream()
                    .filter(step -> step.rounds().isPresent()).mapToLong(step -> step.rounds().getAsLong()).sum();
            List<ItemStack> possibleItems = possibleItems(player, cost, pool, drawPackage);
            GachaPresentations.beginDrawItem(player, player.getItemInHand(hand), session.id, animation,
                    Math.clamp(roundCount, 1, GachaSecondaryType.MAXIMUM_ROUNDS));
            PacketDistributor.sendToPlayer(player, new GachaItemPresentationPayload(session.id, animation, false,
                    true, Math.max(0, Duration.between(Instant.now(), prepared.expiresAt()).toMillis()),
                    previewItems(binding, pool), List.of(), possibleItems));
            return true;
        } catch (RuntimeException failure) {
            forget(player.getUUID());
            GachaConstants.LOGGER.warn("Cannot begin item draw for player {}", player.getUUID(), failure);
            player.sendSystemMessage(Component.translatable(TranslationKeys.GUI_DRAW_ITEM_UNAVAILABLE));
            return false;
        }
    }

    public static void finish(ServerPlayer player, UUID id, boolean cancel) {
        Session session = SESSIONS.get(player.getUUID());
        if (session == null || !session.id.equals(id) || session.committing) {
            return;
        }
        if (cancel) {
            if (GachaPresentations.isForced(player)) {
                return;
            }
            GachaPresentations.release(player, false);
            forget(player.getUUID());
            return;
        }
        if (!Instant.now().isBefore(session.prepared.expiresAt()) || !player.isAlive() || player.isSpectator()
                || !session.dimension.equals(player.level().dimension().location())
                || !ItemStack.isSameItemSameComponents(player.getItemInHand(session.hand), session.cost)) {
            reply(player, session, false, List.of());
            return;
        }

        // 展示会话号不暴露实际确认令牌，普通确认通道无法绕过道具消耗。
        session.committing = true;
        var server = player.serverLevel().getServer();
        GachaPublicCardPoolEndpoint.confirm(player, session.prepared.token(), session.cost)
                .whenComplete((committed, failure) -> server.execute(() -> {
                    if (failure != null) {
                        GachaConstants.LOGGER.warn("Item draw failed for player {}", player.getUUID(), failure);
                        reply(player, session, false, List.of());
                        return;
                    }
                    committed.effectsCompletion().whenComplete((ignored, deliveryFailure) ->
                            server.execute(() -> {
                                if (deliveryFailure != null) {
                                    GachaConstants.LOGGER.error("Item draw delivery failed for transaction {}",
                                            committed.context().transactionId().value(), deliveryFailure);
                                }
                                reply(player, session, deliveryFailure == null, committed.result().rewards(), committed.result().rewardRounds());
                            }));
                }));
    }

    private static List<ItemStack> possibleItems(ServerPlayer player, ItemStack cost, ResourceLocation pool,
                                                 ResourceLocation drawPackage) {
        var source = cost.getCapability(GachaSources.ITEM);
        if (source == null || source.getSettings().randomType() == GachaRandomType.NONE) {
            return List.of();
        }

        var repository = GachaDefinitionRepository.INSTANCE;
        var definition = repository.resolveCardPool(pool).orElseThrow();
        return new GachaPossibleRewardItems(player.serverLevel(), repository.raw())
                .collect(definition.packages().get(drawPackage).executionPlan());
    }

    private static List<ItemStack> previewItems(CompoundTag binding, ResourceLocation pool) {
        List<ItemStack> result = new ArrayList<>();
        var entries = binding.getList(PREVIEW_KEY, Tag.TAG_STRING);
        for (int index = 0; index < Math.min(MAXIMUM_PREVIEW_ITEMS, entries.size()); index++) {
            ResourceLocation id = ResourceLocation.tryParse(entries.getString(index));
            if (id != null) {
                BuiltInRegistries.ITEM.getOptional(id).ifPresent(item -> {
                    ItemStack icon = new ItemStack(item);
                    if (!icon.isEmpty()) {
                        result.add(icon);
                    }
                });
            }
        }
        if (result.isEmpty()) {
            GachaDefinitionRepository.INSTANCE.resolveCardPool(pool).ifPresent(definition -> {
                ItemStack icon = definition.presentation().icon().copy();
                if (!icon.isEmpty()) {
                    result.add(icon);
                }
            });
        }
        return List.copyOf(result);
    }

    private static void reply(ServerPlayer player, Session session, boolean success, List<ItemStack> items) {
        reply(player, session, success, items, List.of());
    }

    private static void reply(ServerPlayer player, Session session, boolean success, List<ItemStack> items, List<List<ItemStack>> rounds) {
        if (SESSIONS.get(player.getUUID()) != session) {
            return;
        }
        SESSIONS.remove(player.getUUID());
        if (!success) {
            GachaPresentations.release(player, true);
        }
        discardPending(session);
        if (player.serverLevel().getServer().getPlayerList().getPlayer(player.getUUID()) == player
                && player.connection.hasChannel(GachaItemPresentationPayload.TYPE)) {
            PacketDistributor.sendToPlayer(player, new GachaItemPresentationPayload(session.id, session.animation,
                    true, success, 0, success ? items : List.of(), success ? rounds : List.of()));
        }
    }

    private static void discardPending(Session session) {
        GachaServerRuntimeManager.current().ifPresent(runtime -> {
            if (runtime.pendingDraws().peek(session.prepared.token(), session.prepared.playerId()).isPresent()) {
                runtime.pendingDraws().consume(session.prepared.token(), session.prepared.playerId());
            }
        });
    }

    public static void forget(UUID player) {
        Session session = SESSIONS.get(player);
        if (session != null && !session.committing) {
            SESSIONS.remove(player);
            discardPending(session);
        }
    }

    public static void clear() {
        SESSIONS.clear();
    }

    private static class Session {
        private final UUID id;
        private final GachaPreparedCardPoolDraw prepared;
        private final InteractionHand hand;
        private final ItemStack cost;
        private final GachaType animation;
        private final ResourceLocation dimension;
        private boolean committing;

        private Session(UUID id, GachaPreparedCardPoolDraw prepared, InteractionHand hand, ItemStack cost,
                        GachaType animation, ResourceLocation dimension) {
            this.id = id;
            this.prepared = prepared;
            this.hand = hand;
            this.cost = cost.copy();
            this.animation = animation;
            this.dimension = dimension;
        }
    }
}

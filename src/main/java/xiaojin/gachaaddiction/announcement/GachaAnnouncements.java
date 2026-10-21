package xiaojin.gachaaddiction.announcement;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import xiaojin.gachaaddiction.presentation.GachaPresentations;
import xiaojin.gachaaddiction.registry.GachaContainerAdapters;
import xiaojin.gachaaddiction.api.GachaAnnouncement;
import xiaojin.gachaaddiction.api.IGachaAnnouncementApi.Trigger;
import xiaojin.gachaaddiction.mixed.IMenuProviderLootExtension;
import xiaojin.gachaaddiction.network.stc.GachaAnnouncementPayload;
import xiaojin.gachaaddiction.network.stc.GachaAnnouncementSessionPayload;
import xiaojin.gachaaddiction.util.RarityUtil;

import java.util.*;

public class GachaAnnouncements {
    private static final int MANUAL_COOLDOWN_TICKS = 20;
    private static final Map<UUID, Session> SESSIONS = new HashMap<>();

    public static void openContainer(ServerPlayer player, MenuProvider provider, AbstractContainerMenu menu) {
        BlockPos position = player.blockPosition();
        Component name = provider.getDisplayName();
        Optional<Component> customName = Optional.empty();
        if (provider instanceof BlockEntity blockEntity) {
            position = blockEntity.getBlockPos();
            name = blockEntity.getBlockState().getBlock().getName();
        } else if (provider instanceof Entity entity) {
            position = entity.blockPosition();
            name = entity.getType().getDescription();
        } else if (provider instanceof IMenuProviderLootExtension extension) {
            if (extension.gachaaddiction$getAnnouncementPosition() != null) {
                position = extension.gachaaddiction$getAnnouncementPosition();
            }
            name = extension.gachaaddiction$getAnnouncementName();
            customName = Optional.ofNullable(extension.gachaaddiction$getAnnouncementCustomName());
        } else {
            var adapter = GachaContainerAdapters.find(provider);
            if (adapter != null) {
                position = adapter.announcementPosition(provider);
                BlockEntity blockEntity = player.serverLevel().getBlockEntity(position);
                if (blockEntity != null) {
                    name = blockEntity.getBlockState().getBlock().getName();
                    if (blockEntity instanceof Nameable nameable) {
                        customName = Optional.ofNullable(nameable.getCustomName());
                    }
                }
            }
        }
        if (provider instanceof Nameable nameable) {
            customName = Optional.ofNullable(nameable.getCustomName());
        }
        List<ItemStack> items = new ArrayList<>();
        for (var slot : menu.slots) {
            if (slot.container != player.getInventory() && !slot.getItem().isEmpty() && slot.mayPickup(player)) {
                items.add(slot.getItem().copy());
            }
        }
        open(player, menu.containerId, position, name, customName, items);
    }

    /**
     * 扩展奖励来源在生成真实奖励后调用，随后发送原有奖励展示数据。
     */
    public static void openRewards(ServerPlayer player, ItemStack container, List<ItemStack> items) {
        GachaPresentations.beginItem(player, container, UUID.randomUUID());
        open(player, GachaAnnouncement.EXPLICIT_REWARDS_MENU, player.blockPosition(),
                container.getItem().getName(container),
                Optional.ofNullable(container.get(DataComponents.CUSTOM_NAME)), items);
    }

    private static void open(ServerPlayer player, int menuId, BlockPos position, Component name,
                             Optional<Component> customName, List<ItemStack> rewards) {
        SESSIONS.remove(player.getUUID());
        if (!player.connection.hasChannel(GachaAnnouncementSessionPayload.TYPE)) {
            return;
        }
        var level = player.serverLevel();
        var structureId = structureAt(player, position);
        var source = new GachaAnnouncement.Source(level.dimension().location(), structureId,
                level.getDescription(), name, customName);
        List<ItemStack> items = GachaAnnouncement.mergeItems(rewards);
        UUID token = UUID.randomUUID();
        SESSIONS.put(player.getUUID(), new Session(token, menuId, source, items, level.getServer().getTickCount()));
        // 会话包只发 token 和菜单 ID，奖励沿用原有同步，避免重复发送整个背包。
        PacketDistributor.sendToPlayer(player, new GachaAnnouncementSessionPayload(token, menuId));
    }

    private static Optional<ResourceLocation> structureAt(ServerPlayer player, BlockPos position) {
        var level = player.serverLevel();
        var chunks = level.getChunkSource();
        var chunk = chunks.getChunkNow(position.getX() >> 4, position.getZ() >> 4);
        if (chunk == null) {
            return Optional.empty();
        }
        var registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        List<ResourceLocation> matches = new ArrayList<>();
        // 只读取已经加载的区块，查不到结构起始区块时使用维度名，避免播报触发区块加载。
        for (var entry : chunk.getAllReferences().entrySet()) {
            for (long reference : entry.getValue()) {
                var startChunk = chunks.getChunkNow(ChunkPos.getX(reference), ChunkPos.getZ(reference));
                if (startChunk == null) {
                    continue;
                }
                var start = startChunk.getStartForStructure(entry.getKey());
                if (start != null && start.isValid() && level.structureManager().structureHasPieceAt(position, start)) {
                    ResourceLocation id = registry.getKey(entry.getKey());
                    if (id != null) {
                        matches.add(id);
                    }
                    break;
                }
            }
        }
        return matches.stream().min(Comparator.comparing(ResourceLocation::toString));
    }

    public static void request(ServerPlayer player, UUID token, List<ItemStack> requested, Trigger trigger) {
        Session session = SESSIONS.get(player.getUUID());
        var server = player.serverLevel().getServer();
        int now = server.getTickCount();
        if (session == null || !session.token.equals(token) || player.isSpectator()
                || now - session.openedAt > GachaAnnouncement.SESSION_LIFETIME_TICKS
                || !player.serverLevel().dimension().location().equals(session.source.dimension())) {
            return;
        }
        if (session.menuId != GachaAnnouncement.EXPLICIT_REWARDS_MENU
                && (player.containerMenu == player.inventoryMenu || player.containerMenu.containerId != session.menuId
                || !player.containerMenu.stillValid(player))) {
            return;
        }
        if (trigger == Trigger.AUTOMATIC && session.automaticSent
                || trigger == Trigger.MANUAL && now - session.lastManualAt < MANUAL_COOLDOWN_TICKS) {
            return;
        }
        List<ItemStack> approved = new ArrayList<>();
        for (ItemStack request : requested.stream().limit(GachaAnnouncement.MAXIMUM_ITEMS).toList()) {
            if (request.isEmpty() || approved.stream().anyMatch(item -> ItemStack.isSameItemSameComponents(item, request))) {
                continue;
            }
            session.items.stream().filter(item -> ItemStack.isSameItemSameComponents(item, request))
                    .findFirst().ifPresent(item -> approved.add(item.copyWithCount(Math.min(item.getCount(), request.getCount()))));
            if (trigger == Trigger.MANUAL && !approved.isEmpty()) {
                break;
            }
        }
        if (approved.isEmpty()) {
            return;
        }
        approved.sort(Comparator.comparingInt(RarityUtil::getRarityLevel).reversed());
        if (trigger == Trigger.AUTOMATIC) {
            session.automaticSent = true;
        } else {
            session.lastManualAt = now;
        }
        var payload = new GachaAnnouncementPayload(
                new GachaAnnouncement(player.getUUID(), player.getDisplayName(), session.source, approved));
        for (ServerPlayer receiver : server.getPlayerList().getPlayers()) {
            if (!receiver.getUUID().equals(player.getUUID())
                    && receiver.connection.hasChannel(GachaAnnouncementPayload.TYPE)) {
                PacketDistributor.sendToPlayer(receiver, payload);
            }
        }
    }

    public static void broadcast(ServerPlayer player, ItemStack item, Trigger trigger) {
        Session session = SESSIONS.get(player.getUUID());
        if (session != null) {
            request(player, session.token, List.of(item), trigger);
        }
    }

    public static void forget(UUID playerId) {
        SESSIONS.remove(playerId);
    }

    public static void close(ServerPlayer player, UUID token) {
        Session session = SESSIONS.get(player.getUUID());
        if (session != null && session.token.equals(token)) {
            forget(player.getUUID());
        }
    }

    public static void closeMenu(ServerPlayer player, int menuId) {
        Session session = SESSIONS.get(player.getUUID());
        if (session != null && session.menuId == menuId) {
            forget(player.getUUID());
        }
    }

    public static void clear() {
        SESSIONS.clear();
    }

    private static class Session {
        private final UUID token;
        private final int menuId;
        private final GachaAnnouncement.Source source;
        private final List<ItemStack> items;
        private final int openedAt;
        private int lastManualAt;
        private boolean automaticSent;

        private Session(UUID token, int menuId, GachaAnnouncement.Source source, List<ItemStack> items, int openedAt) {
            this.token = token;
            this.menuId = menuId;
            this.source = source;
            this.items = items;
            this.openedAt = openedAt;
            this.lastManualAt = openedAt - MANUAL_COOLDOWN_TICKS;
        }
    }
}

package xiaojin.gachaaddiction.presentation;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import xiaojin.gachaaddiction.api.GachaRandomType;
import xiaojin.gachaaddiction.api.GachaSecondaryType;
import xiaojin.gachaaddiction.api.GachaSourceSettings;
import xiaojin.gachaaddiction.api.GachaType;
import xiaojin.gachaaddiction.api.IGachaSource;
import xiaojin.gachaaddiction.config.GachaAddictionConfig;
import xiaojin.gachaaddiction.mixin.accessor.CompoundContainerAccessor;
import xiaojin.gachaaddiction.network.stc.GachaPresentationPayload;
import xiaojin.gachaaddiction.network.cts.GachaPresentationPreferencesPayload;
import xiaojin.gachaaddiction.registry.GachaSources;
import xiaojin.gachaaddiction.registry.GachaTypes;

import java.util.*;

public class GachaPresentations {
    public static final int ITEM_MENU = -1;
    private static final long MAXIMUM_SESSION_MILLIS = 300_000;
    private static final Map<UUID, Session> SESSIONS = new HashMap<>();
    private static final Map<UUID, GachaPresentationPreferencesPayload> PREFERENCES = new HashMap<>();
    private static final Map<Object, UUID> LOCKS = new IdentityHashMap<>();

    public static void preferences(ServerPlayer player, GachaPresentationPreferencesPayload preferences) {
        PREFERENCES.put(player.getUUID(), preferences);
    }

    public static boolean isLocked(Object target) {
        if (LOCKS.containsKey(target)) {
            return true;
        }
        if (target instanceof CompoundContainerAccessor compound) {
            return isLocked(compound.gachaaddiction$getFirst()) || isLocked(compound.gachaaddiction$getSecond());
        }
        return false;
    }

    private static void addTarget(Set<Object> targets, Object target) {
        if (!targets.add(target)) {
            return;
        }
        if (target instanceof CompoundContainerAccessor compound) {
            addTarget(targets, compound.gachaaddiction$getFirst());
            addTarget(targets, compound.gachaaddiction$getSecond());
        }
    }

    private static Set<Object> targets(ServerPlayer player, AbstractContainerMenu menu) {
        Set<Object> result = Collections.newSetFromMap(new IdentityHashMap<>());
        menu.slots.forEach(slot -> {
            if (slot.container != player.getInventory()) {
                addTarget(result, slot.container);
            }
        });
        return result;
    }

    public static boolean mayAccess(ServerPlayer player, Object target) {
        UUID owner = LOCKS.get(target);
        if (owner != null && !owner.equals(player.getUUID())) {
            return false;
        }
        if (target instanceof CompoundContainerAccessor compound) {
            return mayAccess(player, compound.gachaaddiction$getFirst()) && mayAccess(player, compound.gachaaddiction$getSecond());
        }
        return true;
    }

    public static boolean mayAccess(ServerPlayer player, AbstractContainerMenu menu) {
        return targets(player, menu).stream().allMatch(target -> mayAccess(player, target));
    }

    public static boolean mayOpen(ServerPlayer player, MenuProvider provider) {
        IGachaSource source = GachaSources.find(provider);
        return source == null || mayAccess(player, source.getLockTarget());
    }

    public static void beginContainer(ServerPlayer player, MenuProvider provider, AbstractContainerMenu menu) {
        begin(player, GachaSources.find(provider), menu.containerId, targets(player, menu), UUID.randomUUID());
    }

    public static void beginItem(ServerPlayer player, ItemStack stack, UUID token) {
        begin(player, stack.getCapability(GachaSources.ITEM), ITEM_MENU,
                Collections.newSetFromMap(new IdentityHashMap<>()), token, false);
    }

    public static void beginDrawItem(ServerPlayer player, ItemStack stack, UUID token) {
        begin(player, stack.getCapability(GachaSources.ITEM), ITEM_MENU,
                Collections.newSetFromMap(new IdentityHashMap<>()), token, true);
    }

    public static void beginDrawItem(ServerPlayer player, ItemStack stack, UUID token, GachaType animation, int rounds) {
        begin(player, stack.getCapability(GachaSources.ITEM), ITEM_MENU,
                Collections.newSetFromMap(new IdentityHashMap<>()), token, true, animation, rounds, ItemStack.EMPTY);
    }

    public static void beginCommittedRewards(ServerPlayer player, ItemStack icon, int rounds) {
        GachaPresentationPreferencesPayload preferences = PREFERENCES.get(player.getUUID());
        GachaType animation = preferences == null ? GachaTypes.MODERN : preferences.animation();
        if (animation != GachaTypes.TOTEM && animation != GachaTypes.CHEST) {
            return;
        }
        begin(player, null, ITEM_MENU, Collections.newSetFromMap(new IdentityHashMap<>()), UUID.randomUUID(),
                false, animation, rounds, icon);
    }

    private static void begin(ServerPlayer player, @Nullable IGachaSource source, int menuId, Set<Object> targets, UUID token) {
        begin(player, source, menuId, targets, token, false);
    }

    private static void begin(ServerPlayer player, @Nullable IGachaSource source, int menuId, Set<Object> targets, UUID token,
                              boolean explicitAnimation) {
        begin(player, source, menuId, targets, token, explicitAnimation, null, 0, ItemStack.EMPTY);
    }

    private static void begin(ServerPlayer player, @Nullable IGachaSource source, int menuId, Set<Object> targets, UUID token,
                              boolean explicitAnimation, @Nullable GachaType launchAnimation, int rounds, ItemStack display) {
        release(player, false);
        if (!player.connection.hasChannel(GachaPresentationPayload.TYPE)) {
            return;
        }
        GachaSourceSettings settings = source == null ? GachaSourceSettings.DEFAULT : source.getSettings();
        boolean forced = GachaAddictionConfig.SERVER.forceWatch.get() || settings.forceWatch();
        Optional<GachaType> animation = settings.gachaType().map(GachaTypes::getDefault).or(() -> Optional.ofNullable(launchAnimation));
        if (!forced && animation.filter(GachaType::isEmpty).isPresent()) {
            return;
        }
        GachaPresentationPreferencesPayload preferences = PREFERENCES.get(player.getUUID());
        if (!explicitAnimation && !forced && animation.orElse(preferences == null ? GachaTypes.MODERN : preferences.animation()).isEmpty()) {
            return;
        }
        if (forced && animation.filter(GachaType::isEmpty).isPresent()) {
            animation = Optional.of(GachaTypes.MODERN);
        }
        GachaType effective = animation.orElse(preferences == null ? GachaTypes.MODERN : preferences.animation());
        GachaSecondaryType secondary = settings.secondaryType().orElse(preferences == null
                ? GachaSecondaryType.NONE : preferences.secondaryType());
        if (effective != GachaTypes.TOTEM && effective != GachaTypes.CHEST
                || secondary == GachaSecondaryType.MULTIPLE && menuId != ITEM_MENU) {
            secondary = GachaSecondaryType.NONE;
        }
        int choices = settings.secondaryType().isPresent() ? settings.choiceCount()
                : preferences == null ? GachaSecondaryType.DEFAULT_CHOICES : preferences.choiceCount();
        int count = secondary == GachaSecondaryType.SELECT_ONE ? Math.clamp(choices, 2, GachaSecondaryType.MAXIMUM_CHOICES)
                : secondary == GachaSecondaryType.MULTIPLE ? Math.clamp(rounds, 1, GachaSecondaryType.MAXIMUM_ROUNDS) : 1;
        if (source != null) {
            addTarget(targets, source.getLockTarget());
        }
        boolean protect = GachaAddictionConfig.SERVER.protectLoot.get() && preferences != null && preferences.protectLoot();
        if (protect) {
            targets.forEach(target -> LOCKS.put(target, player.getUUID()));
        }
        GachaPresentationPayload rules = new GachaPresentationPayload(token, menuId, forced, false,
                source == null ? display : source.getDisplayItem(), animation, secondary, count,
                effective == GachaTypes.MODERN || effective == GachaTypes.MODERN_2 ? settings.randomType() : GachaRandomType.NONE);
        SESSIONS.put(player.getUUID(), new Session(rules, targets, System.currentTimeMillis(), player.level().dimension()));
        PacketDistributor.sendToPlayer(player, rules);
    }

    public static boolean isForced(ServerPlayer player) {
        Session session = SESSIONS.get(player.getUUID());
        return session != null && session.rules.forceWatch();
    }

    public static void finish(ServerPlayer player, UUID token, boolean completed) {
        Session session = SESSIONS.get(player.getUUID());
        if (session != null && session.rules.token().equals(token) && (completed || !session.rules.forceWatch())) {
            release(player, false);
        }
    }

    public static void closeMenu(ServerPlayer player, int menuId) {
        Session session = SESSIONS.get(player.getUUID());
        if (session != null && session.rules.menuId() == menuId) {
            release(player, true);
        }
    }

    public static void release(ServerPlayer player, boolean notify) {
        Session session = SESSIONS.remove(player.getUUID());
        if (session == null) {
            return;
        }
        session.targets.forEach(target -> LOCKS.remove(target, player.getUUID()));
        if (notify && player.connection.hasChannel(GachaPresentationPayload.TYPE)) {
            GachaPresentationPayload rule = session.rules;
            PacketDistributor.sendToPlayer(player, new GachaPresentationPayload(rule.token(), rule.menuId(),
                    false, true, ItemStack.EMPTY, Optional.empty()));
        }
    }

    public static void tick(MinecraftServer server) {
        for (UUID id : new ArrayList<>(SESSIONS.keySet())) {
            Session session = SESSIONS.get(id);
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (player == null) {
                forget(id);
                continue;
            }
            if (!player.isAlive() || player.isSpectator() || player.level().dimension() != session.dimension
                    || System.currentTimeMillis() - session.startedAt > MAXIMUM_SESSION_MILLIS
                    || session.targets.stream().anyMatch(target -> target instanceof BlockEntity holder && holder.isRemoved()
                    || target instanceof Entity entity && entity.isRemoved())) {
                release(player, true);
            }
        }
    }

    public static void forget(UUID id) {
        Session session = SESSIONS.remove(id);
        if (session != null) {
            session.targets.forEach(target -> LOCKS.remove(target, id));
        }
        PREFERENCES.remove(id);
    }

    public static void clear() {
        SESSIONS.clear();
        PREFERENCES.clear();
        LOCKS.clear();
    }

    private record Session(GachaPresentationPayload rules, Set<Object> targets, long startedAt, Object dimension) {
    }
}

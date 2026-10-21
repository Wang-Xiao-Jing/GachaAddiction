package xiaojin.gachaaddiction.cardpool.transaction;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import xiaojin.gachaaddiction.GachaAddiction;
import xiaojin.gachaaddiction.cardpool.GachaServerRuntimeManager;
import xiaojin.gachaaddiction.cardpool.delivery.GachaRewardInbox;
import xiaojin.gachaaddiction.cardpool.delivery.GachaRewardInboxEntry;
import xiaojin.gachaaddiction.cardpool.delivery.GachaRewardInboxEntryKind;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class GachaPlayerInventoryConsumptionHandler implements GachaConsumptionHandler {
    public static final ResourceLocation TYPE = ResourceLocation.fromNamespaceAndPath(
            GachaAddiction.MODID, "player_inventory");

    private static final String REQUIREMENTS_KEY = "requirements";
    private static final String SELECTOR_KEY = "selector";
    private static final String ID_KEY = "id";
    private static final String QUANTITY_KEY = "quantity";
    private static final String COMPONENT_MATCH_KEY = "component_match";
    private static final String TEMPLATE_KEY = "template";
    private static final String STACKS_KEY = "stacks";
    private static final String CREATIVE_KEY = "creative";
    private static final String CONFIRMED_KEY = "confirmed";
    private static final int MAXIMUM_REQUIREMENTS = 256;

    public static List<GachaConsumptionSpec> withItemCost(List<GachaConsumptionSpec> consumption,
                                                          ItemStack cost, HolderLookup.Provider registries) {
        if (cost.isEmpty()) {
            return consumption;
        }

        List<GachaConsumptionSpec> result = new ArrayList<>();
        ListTag requirements = new ListTag();
        CompoundTag merged = new CompoundTag();
        int inventoryIndex = -1;
        for (GachaConsumptionSpec spec : consumption) {
            if (spec.type().equals(TYPE)) {
                decodeRequirements(spec.data(), registries);
                if (inventoryIndex < 0) {
                    inventoryIndex = result.size();
                    merged = spec.data();
                }
                for (Tag requirement : spec.data().getList(REQUIREMENTS_KEY, Tag.TAG_COMPOUND)) {
                    requirements.add(requirement.copy());
                }
            } else {
                result.add(spec);
            }
        }

        CompoundTag requirement = new CompoundTag();
        requirement.putString(SELECTOR_KEY, Selector.ITEM.name());
        requirement.putString(ID_KEY, BuiltInRegistries.ITEM.getKey(cost.getItem()).toString());
        requirement.putInt(QUANTITY_KEY, cost.getCount());
        requirement.putString(COMPONENT_MATCH_KEY, ComponentMatch.EXACT.name());
        requirement.put(TEMPLATE_KEY, cost.save(registries));
        requirements.add(0, requirement);
        if (requirements.size() > MAXIMUM_REQUIREMENTS) {
            throw new IllegalArgumentException("Too many inventory requirements including activation item");
        }
        merged.put(REQUIREMENTS_KEY, requirements);
        result.add(inventoryIndex < 0 ? result.size() : inventoryIndex, new GachaConsumptionSpec(TYPE, merged));
        return List.copyOf(result);
    }

    private static List<Requirement> decodeRequirements(CompoundTag data, HolderLookup.Provider registries) {
        ListTag encoded = data.getList(REQUIREMENTS_KEY, Tag.TAG_COMPOUND);
        if (encoded.isEmpty() || encoded.size() > MAXIMUM_REQUIREMENTS) {
            throw new IllegalArgumentException("Player inventory consumption requirements count is invalid");
        }
        List<Requirement> requirements = new ArrayList<>();
        for (int index = 0; index < encoded.size(); index++) {
            CompoundTag value = encoded.getCompound(index);
            Selector selector = Selector.valueOf(value.getString(SELECTOR_KEY).toUpperCase(java.util.Locale.ROOT));
            ResourceLocation id = ResourceLocation.parse(value.getString(ID_KEY));
            int quantity = value.getInt(QUANTITY_KEY);
            if (quantity <= 0) {
                throw new IllegalArgumentException("Player inventory consumption quantity must be positive");
            }
            ComponentMatch componentMatch = ComponentMatch.valueOf(
                    value.getString(COMPONENT_MATCH_KEY).toUpperCase(java.util.Locale.ROOT));
            ItemStack template = value.contains(TEMPLATE_KEY, Tag.TAG_COMPOUND)
                    ? ItemStack.parseOptional(registries, value.getCompound(TEMPLATE_KEY)) : ItemStack.EMPTY;
            if (componentMatch != ComponentMatch.IGNORE && template.isEmpty()) {
                throw new IllegalArgumentException("Component matching requires an item template");
            }
            requirements.add(new Requirement(selector, id, quantity, componentMatch, template));
        }
        return List.copyOf(requirements);
    }

    private static List<Removal> plan(Container inventory, List<Requirement> requirements) {
        int[] remaining = new int[inventory.getContainerSize()];
        for (int slot = 0; slot < remaining.length; slot++) {
            remaining[slot] = inventory.getItem(slot).getCount();
        }
        List<Removal> removals = new ArrayList<>();
        for (Requirement requirement : requirements) {
            int needed = requirement.quantity();
            for (int slot = 0; slot < remaining.length && needed > 0; slot++) {
                ItemStack stack = inventory.getItem(slot);
                if (remaining[slot] <= 0 || !requirement.matches(stack)) {
                    continue;
                }
                int count = Math.min(remaining[slot], needed);
                remaining[slot] -= count;
                needed -= count;
                removals.add(new Removal(slot, stack.copyWithCount(count)));
            }
            if (needed > 0) {
                throw new IllegalStateException("Player does not have enough items for gacha consumption");
            }
        }
        return List.copyOf(removals);
    }

    private static GachaConsumptionReservation reservation(List<ItemStack> stacks, boolean creative,
                                                           boolean confirmed, HolderLookup.Provider registries) {
        CompoundTag data = new CompoundTag();
        data.putBoolean(CREATIVE_KEY, creative);
        data.putBoolean(CONFIRMED_KEY, confirmed);
        ListTag encoded = new ListTag();
        stacks.forEach(stack -> encoded.add(stack.save(registries)));
        data.put(STACKS_KEY, encoded);
        return new GachaConsumptionReservation(TYPE, data);
    }

    private static Reservation decodeReservation(GachaConsumptionReservation reservation,
                                                 HolderLookup.Provider registries) {
        if (!TYPE.equals(reservation.handlerType())) {
            throw new IllegalArgumentException("Consumption reservation has the wrong handler type");
        }
        CompoundTag data = reservation.data();
        List<ItemStack> stacks = new ArrayList<>();
        ListTag encoded = data.getList(STACKS_KEY, Tag.TAG_COMPOUND);
        for (int index = 0; index < encoded.size(); index++) {
            ItemStack stack = ItemStack.parseOptional(registries, encoded.getCompound(index));
            if (!stack.isEmpty()) {
                stacks.add(stack);
            }
        }
        return new Reservation(stacks, data.getBoolean(CREATIVE_KEY), data.getBoolean(CONFIRMED_KEY));
    }

    private static void restore(ServerPlayer player, GachaTransactionContext context, List<ItemStack> stacks) {
        List<ItemStack> overflow = new ArrayList<>();
        for (ItemStack value : stacks) {
            ItemStack stack = value.copy();
            player.getInventory().add(stack);
            if (!stack.isEmpty()) {
                overflow.add(stack);
            }
        }
        player.getInventory().setChanged();
        if (!overflow.isEmpty()) {
            requireInbox().restore(new GachaRewardInboxEntry(context.transactionId(), context.playerId(),
                    GachaRewardInboxEntryKind.RETURN, overflow));
        }
    }

    private static void requireType(GachaConsumptionSpec consumption) {
        Objects.requireNonNull(consumption, "consumption");
        if (!TYPE.equals(consumption.type())) {
            throw new IllegalArgumentException("Consumption specification has the wrong handler type");
        }
    }

    private static ServerPlayer requirePlayer(GachaTransactionContext context) {
        ServerPlayer player = findPlayer(context);
        if (player == null) {
            throw new IllegalStateException("Player must be online for inventory consumption");
        }
        return player;
    }

    private static ServerPlayer findPlayer(GachaTransactionContext context) {
        Objects.requireNonNull(context, "context");
        var server = ServerLifecycleHooks.getCurrentServer();
        return server == null ? null : server.getPlayerList().getPlayer(context.playerId());
    }

    private static HolderLookup.Provider registries() {
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            throw new IllegalStateException("Minecraft server is unavailable");
        }
        return server.registryAccess();
    }

    private static GachaRewardInbox requireInbox() {
        return GachaServerRuntimeManager.current().orElseThrow(() ->
                new IllegalStateException("Gacha server runtime is unavailable")).rewardInbox();
    }

    @Override
    public ResourceLocation type() {
        return TYPE;
    }

    @Override
    public void validate(GachaTransactionContext context, GachaConsumptionSpec consumption) {
        requireType(consumption);
        ServerPlayer player = requirePlayer(context);
        List<Requirement> requirements = decodeRequirements(consumption.data(), player.registryAccess());
        if (!player.isCreative()) {
            plan(player.getInventory(), requirements);
        }
    }

    @Override
    public GachaConsumptionReservation reserve(GachaTransactionContext context,
                                               GachaConsumptionSpec consumption) {
        requireType(consumption);
        ServerPlayer player = requirePlayer(context);
        if (player.isCreative()) {
            return reservation(List.of(), true, true, player.registryAccess());
        }
        List<Removal> removals = plan(player.getInventory(),
                decodeRequirements(consumption.data(), player.registryAccess()));
        List<ItemStack> expected = removals.stream().map(Removal::stack).toList();
        GachaRewardInbox inbox = requireInbox();
        inbox.restore(new GachaRewardInboxEntry(context.transactionId(), context.playerId(),
                GachaRewardInboxEntryKind.RETURN, expected));
        List<ItemStack> removed = new ArrayList<>();
        try {
            for (Removal removal : removals) {
                ItemStack value = player.getInventory().removeItem(removal.slot(), removal.stack().getCount());
                if (!ItemStack.isSameItemSameComponents(value, removal.stack())
                        || value.getCount() != removal.stack().getCount()) {
                    throw new IllegalStateException("Player inventory changed while reserving consumption");
                }
                removed.add(value);
            }
            player.getInventory().setChanged();
            return reservation(removed, false, true, player.registryAccess());
        } catch (RuntimeException failure) {
            inbox.take(context.playerId(), context.transactionId());
            restore(player, context, removed);
            throw failure;
        }
    }

    @Override
    public Optional<GachaConsumptionReservation> findReservation(GachaTransactionContext context,
                                                                 GachaConsumptionSpec consumption) {
        requireType(consumption);
        return requireInbox().find(context.playerId(), context.transactionId())
                .filter(entry -> entry.kind() == GachaRewardInboxEntryKind.RETURN)
                .map(entry -> reservation(entry.rewards(), false, false, registries()));
    }

    @Override
    public void commit(GachaTransactionContext context, GachaConsumptionReservation reservation) {
        Reservation decoded = decodeReservation(reservation, registries());
        if (!decoded.creative()) {
            requireInbox().take(context.playerId(), context.transactionId());
        }
    }

    @Override
    public void rollback(GachaTransactionContext context, GachaConsumptionReservation reservation) {
        Reservation decoded = decodeReservation(reservation, registries());
        if (decoded.creative() || !decoded.confirmed()) {
            return;
        }
        ServerPlayer player = findPlayer(context);
        if (player == null) {
            return;
        }
        Optional<GachaRewardInboxEntry> entry = requireInbox().take(context.playerId(), context.transactionId());
        entry.ifPresent(value -> restore(player, context, value.rewards()));
    }

    @Override
    public void recover(GachaTransactionContext context, GachaConsumptionReservation reservation,
                        GachaTransactionStage durableStage) {
        if (durableStage == GachaTransactionStage.COMMITTED || durableStage == GachaTransactionStage.DELIVERED) {
            commit(context, reservation);
        } else {
            rollback(context, reservation);
        }
    }

    private enum Selector {
        ITEM,
        TAG
    }

    private enum ComponentMatch {
        IGNORE,
        CONTAINS,
        EXACT
    }

    private record Requirement(Selector selector, ResourceLocation id, int quantity,
                               ComponentMatch componentMatch, ItemStack template) {
        private boolean matches(ItemStack stack) {
            if (stack.isEmpty()) {
                return false;
            }
            boolean selectorMatches = switch (selector) {
                case ITEM -> BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(id);
                case TAG -> stack.is(ItemTags.create(id));
            };
            if (!selectorMatches) {
                return false;
            }
            return switch (componentMatch) {
                case IGNORE -> true;
                case EXACT -> ItemStack.isSameItemSameComponents(stack, template);
                case CONTAINS -> template.getComponentsPatch().entrySet().stream().allMatch(entry ->
                        Objects.equals(entry.getValue(), Optional.ofNullable(stack.get(entry.getKey()))));
            };
        }
    }

    private record Removal(int slot, ItemStack stack) {
    }

    private record Reservation(List<ItemStack> stacks, boolean creative, boolean confirmed) {
        private Reservation {
            stacks = stacks.stream().map(ItemStack::copy).toList();
        }
    }
}

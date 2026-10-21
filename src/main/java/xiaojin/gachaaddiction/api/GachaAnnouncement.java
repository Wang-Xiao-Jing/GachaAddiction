package xiaojin.gachaaddiction.api;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenCustomHashMap;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackLinkedSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record GachaAnnouncement(UUID playerId, Component playerName, Source source, List<ItemStack> items) {
    public static final int MAXIMUM_ITEMS = 3;
    public static final int MAXIMUM_SOURCE_ITEMS = 4096;
    public static final int SESSION_LIFETIME_TICKS = 6000;
    public static final int EXPLICIT_REWARDS_MENU = -1;
    public static final StreamCodec<RegistryFriendlyByteBuf, GachaAnnouncement> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, GachaAnnouncement::playerId,
            ComponentSerialization.STREAM_CODEC, GachaAnnouncement::playerName,
            Source.STREAM_CODEC, GachaAnnouncement::source,
            ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list(MAXIMUM_ITEMS)), GachaAnnouncement::items,
            GachaAnnouncement::new);

    public GachaAnnouncement {
        playerName = playerName.copy();
        items = items.stream().limit(MAXIMUM_ITEMS).map(ItemStack::copy).toList();
    }

    public static List<ItemStack> mergeItems(List<ItemStack> items) {
        var merged = new Object2ObjectLinkedOpenCustomHashMap<ItemStack, ItemStack>(ItemStackLinkedSet.TYPE_AND_TAG);
        for (ItemStack stack : items.stream().limit(MAXIMUM_SOURCE_ITEMS).toList()) {
            if (stack.isEmpty()) {
                continue;
            }
            ItemStack existing = merged.get(stack);
            if (existing == null) {
                ItemStack copy = stack.copy();
                merged.put(copy, copy);
            } else {
                existing.setCount((int) Math.min(Integer.MAX_VALUE, (long) existing.getCount() + stack.getCount()));
            }
        }
        return new ArrayList<>(merged.values());
    }

    @Override
    public List<ItemStack> items() {
        return items.stream().map(ItemStack::copy).toList();
    }

    public record Source(ResourceLocation dimension, Optional<ResourceLocation> structure,
                         Component dimensionName, Component containerName, Optional<Component> customName) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Source> STREAM_CODEC = StreamCodec.composite(
                ResourceLocation.STREAM_CODEC, Source::dimension,
                ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), Source::structure,
                ComponentSerialization.STREAM_CODEC, Source::dimensionName,
                ComponentSerialization.STREAM_CODEC, Source::containerName,
                ComponentSerialization.OPTIONAL_STREAM_CODEC, Source::customName,
                Source::new);

        public Source {
            dimensionName = dimensionName.copy();
            containerName = containerName.copy();
            customName = customName.map(Component::copy);
        }
    }
}

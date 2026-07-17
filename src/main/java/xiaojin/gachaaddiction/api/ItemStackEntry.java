package xiaojin.gachaaddiction.api;

import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import xiaojin.gachaaddiction.mixin.accessor.LootItemAccessor;
import xiaojin.gachaaddiction.mixin.accessor.LootPoolAccessor;
import xiaojin.gachaaddiction.mixin.accessor.LootPoolSingletonContainerAccessor;
import xiaojin.gachaaddiction.mixin.accessor.LootTableAccessor;

import java.util.ArrayList;
import java.util.List;

public record ItemStackEntry(ItemStack stack, int weight) {
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemStackEntry> STREAM_CODEC = StreamCodec.composite(
            ItemStack.OPTIONAL_STREAM_CODEC, ItemStackEntry::stack,
            ByteBufCodecs.VAR_INT, ItemStackEntry::weight,
            ItemStackEntry::new);

    public static final StreamCodec<RegistryFriendlyByteBuf, List<ItemStackEntry>> LIST_STREAM_CODEC =
            STREAM_CODEC.apply(ByteBufCodecs.list());

    public static List<ItemStackEntry> extract(LootTable table) {
        List<ItemStackEntry> result = new ArrayList<>();
        for (LootPool pool : ((LootTableAccessor) table).gachaaddiction$getPools()) {
            for (LootPoolEntryContainer entry : ((LootPoolAccessor) pool).gachaaddiction$getEntries()) {
                if (entry instanceof LootItem lootItem) {
                    Holder<Item> holder = ((LootItemAccessor) lootItem).gachaaddiction$getItem();
                    int w = ((LootPoolSingletonContainerAccessor) lootItem).gachaaddiction$getWeight();
                    result.add(new ItemStackEntry(new ItemStack(holder), w));
                }
            }
        }
        return result;
    }

    public static List<ItemStack> generateGachaResult(List<ItemStackEntry> entries, int totalCount, RandomSource random) {
        if (totalCount <= 0) return new ArrayList<>();
        int totalWeight = entries.stream().mapToInt(ItemStackEntry::weight).sum();
        if (totalWeight <= 0) return new ArrayList<>();

        List<ItemStack> result = new ArrayList<>(totalCount);
        for (int i = 0; i < totalCount; i++) {
            int roll = random.nextInt(totalWeight);
            int accumulated = 0;
            for (ItemStackEntry entry : entries) {
                accumulated += entry.weight();
                if (roll < accumulated) {
                    result.add(entry.stack().copy());
                    break;
                }
            }
        }
        return result;
    }
}

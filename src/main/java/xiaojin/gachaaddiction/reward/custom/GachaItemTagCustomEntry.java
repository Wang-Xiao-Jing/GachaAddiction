package xiaojin.gachaaddiction.reward.custom;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import xiaojin.gachaaddiction.reward.model.GachaLogicalReward;
import xiaojin.gachaaddiction.reward.number.GachaLongProvider;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record GachaItemTagCustomEntry(ResourceLocation id, TagKey<Item> tag,
                                      GachaLongProvider quantity, GachaItemTagResolver resolver)
        implements GachaCustomEntry {
    public GachaItemTagCustomEntry {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(tag, "tag");
        Objects.requireNonNull(quantity, "quantity");
        Objects.requireNonNull(resolver, "resolver");
    }

    @Override
    public List<GachaLogicalReward> generate(GachaCustomEntryContext context) {
        List<Item> items = List.copyOf(resolver.resolve(tag));
        if (items.isEmpty()) {
            return List.of();
        }
        Item item = items.get(context.sourceContext().request().random().nextInt(items.size()));
        long count = quantity.get(context.numberContext());
        return context.sourceContext().rewardFactory()
                .create(new ItemStack(item), count, context.provenance(), Map.of())
                .map(List::of).orElseGet(List::of);
    }
}

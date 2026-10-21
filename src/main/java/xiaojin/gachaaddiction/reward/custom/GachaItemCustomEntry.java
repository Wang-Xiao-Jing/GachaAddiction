package xiaojin.gachaaddiction.reward.custom;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import xiaojin.gachaaddiction.reward.model.GachaLogicalReward;
import xiaojin.gachaaddiction.reward.number.GachaLongProvider;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class GachaItemCustomEntry implements GachaCustomEntry {
    private final ResourceLocation id;
    private final ItemStack template;
    private final GachaLongProvider quantity;

    public GachaItemCustomEntry(ResourceLocation id, ItemStack template, GachaLongProvider quantity) {
        this.id = Objects.requireNonNull(id, "id");
        Objects.requireNonNull(template, "template");
        if (template.isEmpty()) {
            throw new IllegalArgumentException("Custom item entry cannot use an empty item");
        }
        this.template = template.copyWithCount(1);
        this.quantity = Objects.requireNonNull(quantity, "quantity");
    }

    @Override
    public ResourceLocation id() {
        return id;
    }

    @Override
    public List<GachaLogicalReward> generate(GachaCustomEntryContext context) {
        long count = quantity.get(context.numberContext());
        return context.sourceContext().rewardFactory().create(template, count, context.provenance(), Map.of())
                .map(List::of).orElseGet(List::of);
    }
}

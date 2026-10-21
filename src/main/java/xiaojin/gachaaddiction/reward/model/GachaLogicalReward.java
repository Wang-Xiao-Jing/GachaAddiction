package xiaojin.gachaaddiction.reward.model;

import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import xiaojin.gachaaddiction.reward.provenance.GachaRewardProvenance;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class GachaLogicalReward {
    private final ItemStack template;
    private final long quantity;
    private final GachaRewardProvenance provenance;
    private final Map<ResourceLocation, Tag> metadata;
    private int presentationRound = -1;

    public GachaLogicalReward(ItemStack template, long quantity, GachaRewardProvenance provenance,
                              Map<ResourceLocation, Tag> metadata) {
        Objects.requireNonNull(template, "template");
        Objects.requireNonNull(metadata, "metadata");
        if (template.isEmpty()) {
            throw new IllegalArgumentException("Logical reward template cannot be empty");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Logical reward quantity must be positive");
        }
        this.template = template.copyWithCount(1);
        this.quantity = quantity;
        this.provenance = Objects.requireNonNull(provenance, "provenance");
        this.metadata = copyMetadata(metadata);
    }

    private static Map<ResourceLocation, Tag> copyMetadata(Map<ResourceLocation, Tag> metadata) {
        Map<ResourceLocation, Tag> copy = new LinkedHashMap<>();
        metadata.forEach((key, value) -> copy.put(Objects.requireNonNull(key, "metadata key"),
                Objects.requireNonNull(value, "metadata value").copy()));
        return Collections.unmodifiableMap(copy);
    }

    public ItemStack template() {
        return template.copy();
    }

    public long quantity() {
        return quantity;
    }

    public GachaRewardProvenance provenance() {
        return provenance;
    }

    public Map<ResourceLocation, Tag> metadata() {
        return copyMetadata(metadata);
    }

    public GachaLogicalReward withQuantity(long quantity) {
        GachaLogicalReward copy = new GachaLogicalReward(template, quantity, provenance, metadata);
        copy.presentationRound = presentationRound;
        return copy;
    }

    public GachaLogicalReward withProvenance(GachaRewardProvenance provenance) {
        GachaLogicalReward copy = new GachaLogicalReward(template, quantity, provenance, metadata);
        copy.presentationRound = presentationRound;
        return copy;
    }

    public int presentationRound() {
        return presentationRound;
    }

    public GachaLogicalReward withPresentationRound(int round) {
        GachaLogicalReward copy = new GachaLogicalReward(template, quantity, provenance, metadata);
        copy.presentationRound = round;
        return copy;
    }
}

package xiaojin.gachaaddiction.reward.model;

import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import xiaojin.gachaaddiction.reward.provenance.GachaRewardOrigin;
import xiaojin.gachaaddiction.reward.provenance.GachaRewardProvenance;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class GachaRewardFactory {
    private static GachaRewardProvenance provenance(List<GachaRewardOrigin> generationProvenance) {
        Objects.requireNonNull(generationProvenance, "generationProvenance");
        if (generationProvenance.isEmpty()) {
            throw new IllegalArgumentException("Generation provenance cannot be empty");
        }
        GachaRewardProvenance provenance = GachaRewardProvenance.generated(generationProvenance.getFirst());
        for (int index = 1; index < generationProvenance.size(); index++) {
            provenance = provenance.generatedBy(generationProvenance.get(index));
        }
        return provenance;
    }

    public Optional<GachaLogicalReward> create(ItemStack stack, List<GachaRewardOrigin> generationProvenance) {
        return create(stack, generationProvenance, Map.of());
    }

    public Optional<GachaLogicalReward> create(ItemStack stack, List<GachaRewardOrigin> generationProvenance,
                                               Map<ResourceLocation, Tag> metadata) {
        Objects.requireNonNull(stack, "stack");
        Objects.requireNonNull(generationProvenance, "generationProvenance");
        Objects.requireNonNull(metadata, "metadata");
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        if (generationProvenance.isEmpty()) {
            throw new IllegalArgumentException("Generation provenance cannot be empty");
        }

        GachaRewardProvenance provenance = GachaRewardProvenance.generated(generationProvenance.getFirst());
        for (int index = 1; index < generationProvenance.size(); index++) {
            provenance = provenance.generatedBy(generationProvenance.get(index));
        }
        return Optional.of(new GachaLogicalReward(stack, stack.getCount(), provenance, metadata));
    }

    public Optional<GachaLogicalReward> create(ItemStack template, long quantity,
                                               List<GachaRewardOrigin> generationProvenance,
                                               Map<ResourceLocation, Tag> metadata) {
        Objects.requireNonNull(template, "template");
        if (template.isEmpty() || quantity <= 0) {
            return Optional.empty();
        }
        GachaRewardProvenance provenance = provenance(generationProvenance);
        return Optional.of(new GachaLogicalReward(template, quantity, provenance, metadata));
    }
}

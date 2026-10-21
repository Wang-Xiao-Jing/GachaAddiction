package xiaojin.gachaaddiction.reward.custom;

import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.reward.model.GachaLogicalReward;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record GachaCompositeCustomEntry(ResourceLocation id, GachaCompositeEntryMode mode,
                                        List<GachaCustomEntry> children) implements GachaCustomEntry {
    public GachaCompositeCustomEntry {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(mode, "mode");
        children = List.copyOf(children);
        if (children.isEmpty()) {
            throw new IllegalArgumentException("Composite custom entry must contain children: " + id);
        }
    }

    @Override
    public List<GachaLogicalReward> generate(GachaCustomEntryContext context) {
        List<GachaLogicalReward> rewards = new ArrayList<>();
        for (GachaCustomEntry child : children) {
            List<GachaLogicalReward> generated = child.generate(context.enterEntry(child.id()));
            rewards.addAll(generated);
            if (mode == GachaCompositeEntryMode.ALTERNATIVES && !generated.isEmpty()) {
                break;
            }
        }
        return List.copyOf(rewards);
    }
}

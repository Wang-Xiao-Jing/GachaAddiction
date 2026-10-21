package xiaojin.gachaaddiction.reward.custom;

import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.reward.model.GachaLogicalReward;

import java.util.List;
import java.util.Objects;

public record GachaEmptyCustomEntry(ResourceLocation id) implements GachaCustomEntry {
    public GachaEmptyCustomEntry {
        Objects.requireNonNull(id, "id");
    }

    @Override
    public List<GachaLogicalReward> generate(GachaCustomEntryContext context) {
        return List.of();
    }
}

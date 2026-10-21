package xiaojin.gachaaddiction.reward.provenance;

import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public record GachaRewardOrigin(ResourceLocation type, ResourceLocation id) {
    public GachaRewardOrigin {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(id, "id");
    }
}

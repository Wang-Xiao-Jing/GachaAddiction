package xiaojin.gachaaddiction.cardpool;

import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public record GachaDrawPackageSummary(ResourceLocation id) {
    public GachaDrawPackageSummary {
        Objects.requireNonNull(id, "id");
    }
}

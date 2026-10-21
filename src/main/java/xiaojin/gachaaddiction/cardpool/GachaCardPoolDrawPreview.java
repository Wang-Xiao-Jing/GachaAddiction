package xiaojin.gachaaddiction.cardpool;

import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.execution.GachaExecutionPreview;

import java.util.Objects;

public record GachaCardPoolDrawPreview(ResourceLocation cardPoolId, ResourceLocation packageId,
                                       GachaExecutionPreview execution) {
    public GachaCardPoolDrawPreview {
        Objects.requireNonNull(cardPoolId, "cardPoolId");
        Objects.requireNonNull(packageId, "packageId");
        Objects.requireNonNull(execution, "execution");
    }
}

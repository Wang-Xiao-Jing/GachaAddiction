package xiaojin.gachaaddiction.cardpool;

import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.cardpool.transaction.GachaDurability;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterSet;

import java.util.Objects;

public record GachaDrawPackage(ResourceLocation id, ResourceLocation executionPlan,
                               GachaParameterSet parameters, GachaDurability minimumDurability) {
    public GachaDrawPackage {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(executionPlan, "executionPlan");
        Objects.requireNonNull(parameters, "parameters");
        Objects.requireNonNull(minimumDurability, "minimumDurability");
    }
}

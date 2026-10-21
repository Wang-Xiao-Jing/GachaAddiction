package xiaojin.gachaaddiction.reward.custom;

import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.reward.probability.GachaProbabilityDescription;

import java.util.Optional;

@FunctionalInterface
public interface GachaCustomTableResolver {
    Optional<GachaCustomLootTable> resolveTable(ResourceLocation id);

    default Optional<GachaProbabilityDescription> describeProbability(ResourceLocation id) {
        return resolveTable(id).map(GachaCustomLootTable::describeProbability);
    }
}

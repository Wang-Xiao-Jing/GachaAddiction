package xiaojin.gachaaddiction.reward.custom;

import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.reward.model.GachaLogicalReward;

import java.util.List;

public interface GachaCustomEntry {
    ResourceLocation id();

    List<GachaLogicalReward> generate(GachaCustomEntryContext context);

    default boolean isEligible(GachaCustomEntryContext context) {
        return true;
    }

    default List<GachaLogicalReward> generateSelected(GachaCustomEntryContext context) {
        return generate(context);
    }
}

package xiaojin.gachaaddiction.reward.source;

import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.reward.model.GachaLogicalReward;

import java.util.List;

public interface IGachaRewardSource {
    ResourceLocation id();

    List<GachaLogicalReward> generate(GachaSourceGenerationContext context);
}

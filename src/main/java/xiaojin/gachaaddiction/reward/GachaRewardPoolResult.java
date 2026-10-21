package xiaojin.gachaaddiction.reward;

import net.minecraft.world.item.ItemStack;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterChangeSet;
import xiaojin.gachaaddiction.reward.problem.GachaProblem;

import java.util.List;
import java.util.Objects;

public record GachaRewardPoolResult(List<ItemStack> rewards, GachaParameterChangeSet parameterChanges,
                                    List<GachaProblem> problems) {
    public GachaRewardPoolResult {
        Objects.requireNonNull(rewards, "rewards");
        rewards = rewards.stream().map(ItemStack::copy).toList();
        Objects.requireNonNull(parameterChanges, "parameterChanges");
        Objects.requireNonNull(problems, "problems");
        problems = List.copyOf(problems);
    }

    @Override
    public List<ItemStack> rewards() {
        return rewards.stream().map(ItemStack::copy).toList();
    }
}

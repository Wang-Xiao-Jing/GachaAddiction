package xiaojin.gachaaddiction.cardpool.transaction;

import net.minecraft.world.item.ItemStack;
import xiaojin.gachaaddiction.execution.GachaExecutionResult;
import xiaojin.gachaaddiction.reward.GachaRewardPoolResult;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterChangeSet;
import xiaojin.gachaaddiction.reward.problem.GachaProblem;

import java.util.List;
import java.util.Objects;

public record GachaTransactionResult(List<ItemStack> rewards, GachaParameterChangeSet parameterChanges,
                                     List<GachaProblem> problems, List<List<ItemStack>> rewardRounds) {
    public GachaTransactionResult(List<ItemStack> rewards, GachaParameterChangeSet parameterChanges,
                                  List<GachaProblem> problems) {
        this(rewards, parameterChanges, problems, List.of());
    }

    public GachaTransactionResult {
        rewards = Objects.requireNonNull(rewards, "rewards").stream().map(ItemStack::copy).toList();
        Objects.requireNonNull(parameterChanges, "parameterChanges");
        problems = List.copyOf(Objects.requireNonNull(problems, "problems"));
        rewardRounds = rewardRounds.stream().map(round -> round.stream().map(ItemStack::copy).toList()).toList();
    }

    public static GachaTransactionResult from(GachaRewardPoolResult result) {
        Objects.requireNonNull(result, "result");
        return new GachaTransactionResult(result.rewards(), result.parameterChanges(), result.problems());
    }

    public static GachaTransactionResult from(GachaExecutionResult result) {
        Objects.requireNonNull(result, "result");
        return new GachaTransactionResult(result.rewards(), result.parameterChanges(), result.diagnostics(), result.rewardRounds());
    }

    @Override
    public List<ItemStack> rewards() {
        return rewards.stream().map(ItemStack::copy).toList();
    }

    @Override
    public List<List<ItemStack>> rewardRounds() {
        return rewardRounds.stream().map(round -> round.stream().map(ItemStack::copy).toList()).toList();
    }
}

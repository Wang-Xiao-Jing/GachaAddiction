package xiaojin.gachaaddiction.execution;

import net.minecraft.world.item.ItemStack;
import xiaojin.gachaaddiction.cardpool.transaction.GachaConsumptionSpec;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterChangeSet;
import xiaojin.gachaaddiction.reward.problem.GachaProblem;

import java.util.List;
import java.util.Objects;

public record GachaExecutionResult(List<ItemStack> rewards, List<GachaConsumptionSpec> consumption,
                                   GachaParameterChangeSet parameterChanges,
                                   List<GachaProblem> diagnostics, List<List<ItemStack>> rewardRounds) {
    public GachaExecutionResult(List<ItemStack> rewards, List<GachaConsumptionSpec> consumption,
                                GachaParameterChangeSet parameterChanges, List<GachaProblem> diagnostics) {
        this(rewards, consumption, parameterChanges, diagnostics, List.of());
    }

    public GachaExecutionResult {
        rewards = Objects.requireNonNull(rewards, "rewards").stream().map(ItemStack::copy).toList();
        consumption = List.copyOf(Objects.requireNonNull(consumption, "consumption"));
        Objects.requireNonNull(parameterChanges, "parameterChanges");
        diagnostics = List.copyOf(Objects.requireNonNull(diagnostics, "diagnostics"));
        rewardRounds = rewardRounds.stream().map(round -> round.stream().map(ItemStack::copy).toList()).toList();
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

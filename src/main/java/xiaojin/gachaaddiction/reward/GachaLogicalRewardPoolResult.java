package xiaojin.gachaaddiction.reward;

import xiaojin.gachaaddiction.reward.model.GachaLogicalReward;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterChangeSet;
import xiaojin.gachaaddiction.reward.problem.GachaProblem;
import xiaojin.gachaaddiction.reward.quantity.GachaExecutionLimits;
import xiaojin.gachaaddiction.reward.quantity.GachaExecutionUsage;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record GachaLogicalRewardPoolResult(List<GachaLogicalReward> rewards,
                                           GachaParameterChangeSet parameterChanges,
                                           List<GachaProblem> problems,
                                           Optional<GachaExecutionLimits> limits,
                                           GachaExecutionUsage usage, int presentationRounds) {
    public GachaLogicalRewardPoolResult(List<GachaLogicalReward> rewards, GachaParameterChangeSet parameterChanges,
                                        List<GachaProblem> problems, Optional<GachaExecutionLimits> limits,
                                        GachaExecutionUsage usage) {
        this(rewards, parameterChanges, problems, limits, usage, 0);
    }

    public GachaLogicalRewardPoolResult {
        rewards = List.copyOf(Objects.requireNonNull(rewards, "rewards"));
        Objects.requireNonNull(parameterChanges, "parameterChanges");
        problems = List.copyOf(Objects.requireNonNull(problems, "problems"));
        limits = Objects.requireNonNull(limits, "limits");
        Objects.requireNonNull(usage, "usage");
    }

    public GachaLogicalRewardPoolResult(List<GachaLogicalReward> rewards,
                                        GachaParameterChangeSet parameterChanges,
                                        List<GachaProblem> problems,
                                        Optional<GachaExecutionLimits> limits) {
        this(rewards, parameterChanges, problems, limits, GachaExecutionUsage.empty());
    }
}

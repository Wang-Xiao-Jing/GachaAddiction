package xiaojin.gachaaddiction.reward.model;

import java.util.List;

public record GachaResultSnapshot(List<GachaLogicalReward> rewards) {
    public GachaResultSnapshot {
        rewards = List.copyOf(rewards);
    }
}

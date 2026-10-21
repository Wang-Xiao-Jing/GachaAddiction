package xiaojin.gachaaddiction.reward.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class GachaResultEditor {
    private final List<GachaLogicalReward> rewards;

    public GachaResultEditor(List<GachaLogicalReward> rewards) {
        this.rewards = new ArrayList<>(List.copyOf(rewards));
    }

    public int size() {
        return rewards.size();
    }

    public GachaLogicalReward get(int index) {
        return rewards.get(index);
    }

    public void replace(int index, GachaLogicalReward reward) {
        rewards.set(index, Objects.requireNonNull(reward, "reward"));
    }

    public GachaLogicalReward remove(int index) {
        return rewards.remove(index);
    }

    public void append(GachaLogicalReward reward) {
        rewards.add(Objects.requireNonNull(reward, "reward"));
    }

    public GachaResultSnapshot snapshot() {
        return new GachaResultSnapshot(rewards);
    }

    public void restore(GachaResultSnapshot snapshot) {
        rewards.clear();
        rewards.addAll(Objects.requireNonNull(snapshot, "snapshot").rewards());
    }

    public List<GachaLogicalReward> results() {
        return List.copyOf(rewards);
    }
}

package xiaojin.gachaaddiction.reward.pity;

public record GachaPityUpdate(GachaPityOutcome outcome, long nextMissCount) {
    public GachaPityUpdate {
        if (nextMissCount < 0) {
            throw new IllegalArgumentException("Next miss count cannot be negative");
        }
        if (outcome.finalHit() && nextMissCount != 0) {
            throw new IllegalArgumentException("Final hit must reset miss count");
        }
        if (!outcome.finalHit() && nextMissCount != outcome.nextDraw()) {
            throw new IllegalArgumentException("Miss must advance miss count to current draw index");
        }
    }
}

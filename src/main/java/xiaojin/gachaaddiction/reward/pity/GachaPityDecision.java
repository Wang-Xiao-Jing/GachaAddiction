package xiaojin.gachaaddiction.reward.pity;

public record GachaPityDecision(long previousMissCount, long nextDraw, boolean hardPityDue) {
    public GachaPityDecision {
        if (previousMissCount < 0) {
            throw new IllegalArgumentException("Previous miss count cannot be negative");
        }
        if (nextDraw != Math.incrementExact(previousMissCount)) {
            throw new IllegalArgumentException("Next draw must equal previous miss count plus one");
        }
    }
}

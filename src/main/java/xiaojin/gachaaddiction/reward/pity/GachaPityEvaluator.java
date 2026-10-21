package xiaojin.gachaaddiction.reward.pity;

public final class GachaPityEvaluator {
    private GachaPityEvaluator() {
    }

    public static GachaPityDecision beforeDraw(long previousMissCount, long hardPityDraw) {
        if (previousMissCount < 0) {
            throw new IllegalArgumentException("Previous miss count cannot be negative");
        }
        if (hardPityDraw <= 0) {
            throw new IllegalArgumentException("Hard pity draw must be positive");
        }
        long nextDraw = Math.incrementExact(previousMissCount);
        return new GachaPityDecision(previousMissCount, nextDraw, nextDraw >= hardPityDraw);
    }

    public static GachaPityUpdate afterDraw(GachaPityDecision decision, boolean naturalHit,
                                            boolean pityTriggered, boolean finalHit) {
        if (pityTriggered && !decision.hardPityDue()) {
            throw new IllegalArgumentException("Hard pity cannot trigger before its configured draw");
        }
        GachaPityOutcome outcome = new GachaPityOutcome(decision.nextDraw(), naturalHit, pityTriggered, finalHit);
        return new GachaPityUpdate(outcome, finalHit ? 0 : decision.nextDraw());
    }
}

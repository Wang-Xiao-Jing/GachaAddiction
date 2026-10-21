package xiaojin.gachaaddiction.reward.pity;

public record GachaPityOutcome(long nextDraw, boolean naturalHit, boolean pityTriggered, boolean finalHit) {
    public GachaPityOutcome {
        if (nextDraw <= 0) {
            throw new IllegalArgumentException("Next draw index must be positive");
        }
    }
}

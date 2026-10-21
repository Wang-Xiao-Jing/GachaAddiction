package xiaojin.gachaaddiction.reward.source;

public final class GachaGenerationBudget {
    private final int maximumTableDepth;
    private final int maximumEntryVisits;
    private int entryVisits;
    private int maximumDepthObserved;

    public GachaGenerationBudget(int maximumTableDepth, int maximumEntryVisits) {
        if (maximumTableDepth <= 0 || maximumEntryVisits <= 0) {
            throw new IllegalArgumentException("Generation budget limits must be positive");
        }
        this.maximumTableDepth = maximumTableDepth;
        this.maximumEntryVisits = maximumEntryVisits;
    }

    public void requireDepth(int depth) {
        maximumDepthObserved = Math.max(maximumDepthObserved, depth);
        if (depth > maximumTableDepth) {
            throw new IllegalArgumentException("Custom table depth exceeds execution limit");
        }
    }

    public void visitEntry() {
        entryVisits = Math.incrementExact(entryVisits);
        if (entryVisits > maximumEntryVisits) {
            throw new IllegalArgumentException("Custom entry visits exceed execution limit");
        }
    }

    public int entryVisits() {
        return entryVisits;
    }

    public int maximumDepthObserved() {
        return maximumDepthObserved;
    }
}

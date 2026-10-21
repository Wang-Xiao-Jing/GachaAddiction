package xiaojin.gachaaddiction.reward.quantity;

public record GachaExecutionUsage(int sourceSelections, int ruleActions, int customEntryVisits,
                                  int maximumCustomTableDepth) {
    public GachaExecutionUsage {
        if (sourceSelections < 0 || ruleActions < 0 || customEntryVisits < 0 || maximumCustomTableDepth < 0) {
            throw new IllegalArgumentException("Execution usage values cannot be negative");
        }
    }

    public static GachaExecutionUsage empty() {
        return new GachaExecutionUsage(0, 0, 0, 0);
    }

    public GachaExecutionUsage add(GachaExecutionUsage other) {
        return new GachaExecutionUsage(Math.addExact(sourceSelections, other.sourceSelections),
                Math.addExact(ruleActions, other.ruleActions),
                Math.addExact(customEntryVisits, other.customEntryVisits),
                Math.max(maximumCustomTableDepth, other.maximumCustomTableDepth));
    }
}

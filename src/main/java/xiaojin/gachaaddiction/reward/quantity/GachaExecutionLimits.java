package xiaojin.gachaaddiction.reward.quantity;

public record GachaExecutionLimits(long maxLogicalQuantity, int maxLogicalRewards, int maxPhysicalStacks,
                                   int maxRounds, int maxSourceSelections, int maxRuleActions,
                                   int maxCustomTableDepth, int maxCustomEntryVisits) {
    public GachaExecutionLimits {
        if (maxLogicalQuantity <= 0) {
            throw new IllegalArgumentException("Maximum logical quantity must be positive");
        }
        if (maxLogicalRewards <= 0) {
            throw new IllegalArgumentException("Maximum logical rewards must be positive");
        }
        if (maxPhysicalStacks <= 0) {
            throw new IllegalArgumentException("Maximum physical stacks must be positive");
        }
        if (maxRounds <= 0) {
            throw new IllegalArgumentException("Maximum rounds must be positive");
        }
        if (maxSourceSelections <= 0) {
            throw new IllegalArgumentException("Maximum source selections must be positive");
        }
        if (maxRuleActions <= 0) {
            throw new IllegalArgumentException("Maximum rule actions must be positive");
        }
        if (maxCustomTableDepth <= 0) {
            throw new IllegalArgumentException("Maximum custom table depth must be positive");
        }
        if (maxCustomEntryVisits <= 0) {
            throw new IllegalArgumentException("Maximum custom entry visits must be positive");
        }
    }

    public GachaExecutionLimits restrictWith(GachaExecutionLimits other) {
        return new GachaExecutionLimits(Math.min(maxLogicalQuantity, other.maxLogicalQuantity),
                Math.min(maxLogicalRewards, other.maxLogicalRewards),
                Math.min(maxPhysicalStacks, other.maxPhysicalStacks), Math.min(maxRounds, other.maxRounds),
                Math.min(maxSourceSelections, other.maxSourceSelections),
                Math.min(maxRuleActions, other.maxRuleActions),
                Math.min(maxCustomTableDepth, other.maxCustomTableDepth),
                Math.min(maxCustomEntryVisits, other.maxCustomEntryVisits));
    }
}

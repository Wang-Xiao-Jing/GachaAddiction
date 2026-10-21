package xiaojin.gachaaddiction.reward.rule;

public final class GachaRuleExecutionBudget {
    private int maximumActions;
    private int usedActions;

    public GachaRuleExecutionBudget(int maximumActions) {
        if (maximumActions <= 0) {
            throw new IllegalArgumentException("Maximum rule actions must be positive");
        }
        this.maximumActions = maximumActions;
    }

    public void consumeAction() {
        consumeActions(1);
    }

    public void consumeActions(int actions) {
        if (actions < 0) {
            throw new IllegalArgumentException("Actions cannot be negative");
        }
        usedActions = Math.addExact(usedActions, actions);
        if (usedActions > maximumActions) {
            throw new IllegalArgumentException("Rule action count exceeds execution limit");
        }
    }

    public void restrictTo(int maximumActions) {
        if (maximumActions <= 0) {
            throw new IllegalArgumentException("Maximum rule actions must be positive");
        }
        this.maximumActions = Math.min(this.maximumActions, maximumActions);
        if (usedActions > this.maximumActions) {
            throw new IllegalArgumentException("Rule action count exceeds execution limit");
        }
    }

    public int usedActions() {
        return usedActions;
    }
}

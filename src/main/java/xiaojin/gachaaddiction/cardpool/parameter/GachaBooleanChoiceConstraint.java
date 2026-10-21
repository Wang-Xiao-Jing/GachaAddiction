package xiaojin.gachaaddiction.cardpool.parameter;

public final class GachaBooleanChoiceConstraint implements IGachaPlayerChoiceConstraint<Boolean> {
    public static final GachaBooleanChoiceConstraint INSTANCE = new GachaBooleanChoiceConstraint();

    private GachaBooleanChoiceConstraint() {
    }

    @Override
    public boolean allows(Boolean value) {
        return value != null;
    }
}

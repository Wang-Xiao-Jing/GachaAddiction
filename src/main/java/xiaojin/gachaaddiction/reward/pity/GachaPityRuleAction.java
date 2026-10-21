package xiaojin.gachaaddiction.reward.pity;

import xiaojin.gachaaddiction.reward.model.GachaResultEditor;
import xiaojin.gachaaddiction.reward.number.GachaLongProvider;
import xiaojin.gachaaddiction.reward.number.GachaNumberContext;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterDefinition;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterDirection;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterEditor;
import xiaojin.gachaaddiction.reward.rule.GachaRuleAction;
import xiaojin.gachaaddiction.reward.rule.GachaRuleCondition;
import xiaojin.gachaaddiction.reward.rule.GachaRuleContext;

import java.util.Objects;

public record GachaPityRuleAction(GachaParameterDefinition<Long> missCounter,
                                  GachaLongProvider hardPityDraw,
                                  GachaRuleCondition hitCondition,
                                  GachaRuleAction hardPityAction,
                                  GachaPityOutputs outputs) implements GachaRuleAction {
    public GachaPityRuleAction {
        Objects.requireNonNull(missCounter, "missCounter");
        Objects.requireNonNull(hardPityDraw, "hardPityDraw");
        Objects.requireNonNull(hitCondition, "hitCondition");
        Objects.requireNonNull(hardPityAction, "hardPityAction");
        Objects.requireNonNull(outputs, "outputs");
        if (missCounter.direction() != GachaParameterDirection.INOUT) {
            throw new IllegalArgumentException("Pity miss counter must be an INOUT parameter");
        }
        outputs.naturalHit().ifPresent(GachaPityRuleAction::requireWritableOutput);
        outputs.pityTriggered().ifPresent(GachaPityRuleAction::requireWritableOutput);
        outputs.finalHit().ifPresent(GachaPityRuleAction::requireWritableOutput);
    }

    private static void requireWritableOutput(GachaParameterDefinition<Boolean> definition) {
        if (definition.direction() == GachaParameterDirection.INPUT) {
            throw new IllegalArgumentException("Pity result flag cannot use an INPUT parameter: " + definition.key());
        }
    }

    @Override
    public void apply(GachaRuleContext context, GachaResultEditor results, GachaParameterEditor parameters) {
        long previousMissCount = parameters.currentValues().get(missCounter)
                .or(missCounter::defaultValue).orElse(0L);
        GachaNumberContext numbers = new GachaNumberContext(context.request().parameters(),
                parameters.currentValues(), context.request().random());
        GachaPityDecision decision = GachaPityEvaluator.beforeDraw(previousMissCount,
                hardPityDraw.get(numbers));
        boolean naturalHit = hitCondition.test(context, results.results(), parameters.currentValues());
        boolean pityTriggered = !naturalHit && decision.hardPityDue();
        if (pityTriggered) {
            hardPityAction.apply(context, results, parameters);
        }
        boolean finalHit = hitCondition.test(context, results.results(), parameters.currentValues());
        GachaPityUpdate update = GachaPityEvaluator.afterDraw(decision, naturalHit, pityTriggered, finalHit);
        parameters.set(missCounter, update.nextMissCount());
        outputs.naturalHit().ifPresent(definition -> parameters.set(definition, naturalHit));
        outputs.pityTriggered().ifPresent(definition -> parameters.set(definition, pityTriggered));
        outputs.finalHit().ifPresent(definition -> parameters.set(definition, finalHit));
    }
}

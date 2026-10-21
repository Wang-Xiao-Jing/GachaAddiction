package xiaojin.gachaaddiction.event;

import net.neoforged.bus.api.Event;
import xiaojin.gachaaddiction.cardpool.transaction.GachaConsumptionHandlerRegistry;
import xiaojin.gachaaddiction.cardpool.transaction.GachaRewardDeliveryHandlerRegistry;
import xiaojin.gachaaddiction.execution.GachaExecutionStepTypeRegistry;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterTypeRegistry;

import java.util.Objects;

public final class RegisterGachaRuntimeTypesEvent extends Event {
    private final GachaParameterTypeRegistry parameterTypes;
    private final GachaConsumptionHandlerRegistry consumptionHandlers;
    private final GachaRewardDeliveryHandlerRegistry rewardDeliveryHandlers;
    private final GachaExecutionStepTypeRegistry executionStepTypes;

    public RegisterGachaRuntimeTypesEvent(GachaParameterTypeRegistry parameterTypes,
                                          GachaConsumptionHandlerRegistry consumptionHandlers,
                                          GachaRewardDeliveryHandlerRegistry rewardDeliveryHandlers,
                                          GachaExecutionStepTypeRegistry executionStepTypes) {
        this.parameterTypes = Objects.requireNonNull(parameterTypes, "parameterTypes");
        this.consumptionHandlers = Objects.requireNonNull(consumptionHandlers, "consumptionHandlers");
        this.rewardDeliveryHandlers = Objects.requireNonNull(rewardDeliveryHandlers, "rewardDeliveryHandlers");
        this.executionStepTypes = Objects.requireNonNull(executionStepTypes, "executionStepTypes");
    }

    public GachaParameterTypeRegistry parameterTypes() {
        return parameterTypes;
    }

    public GachaConsumptionHandlerRegistry consumptionHandlers() {
        return consumptionHandlers;
    }

    public GachaRewardDeliveryHandlerRegistry rewardDeliveryHandlers() {
        return rewardDeliveryHandlers;
    }

    public GachaExecutionStepTypeRegistry executionStepTypes() {
        return executionStepTypes;
    }
}

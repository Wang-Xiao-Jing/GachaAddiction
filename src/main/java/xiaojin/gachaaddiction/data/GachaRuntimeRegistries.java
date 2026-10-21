package xiaojin.gachaaddiction.data;

import net.neoforged.neoforge.common.NeoForge;
import xiaojin.gachaaddiction.api.event.RegisterGachaRuntimeTypesEvent;
import xiaojin.gachaaddiction.cardpool.GachaRewardDeliveryMode;
import xiaojin.gachaaddiction.cardpool.delivery.GachaRewardInboxEntryKind;
import xiaojin.gachaaddiction.cardpool.transaction.*;
import xiaojin.gachaaddiction.execution.GachaBuiltinExecutionSteps;
import xiaojin.gachaaddiction.execution.GachaExecutionStepTypeRegistry;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterTypeRegistry;

public final class GachaRuntimeRegistries {
    public static final GachaParameterTypeRegistry PARAMETER_TYPES = new GachaParameterTypeRegistry();
    public static final GachaConsumptionHandlerRegistry CONSUMPTION_HANDLERS = createConsumptionHandlers();
    public static final GachaRewardDeliveryHandlerRegistry REWARD_DELIVERY_HANDLERS = createRewardDeliveryHandlers();
    public static final GachaExecutionStepTypeRegistry EXECUTION_STEP_TYPES = createExecutionStepTypes();
    private static boolean frozen;

    private GachaRuntimeRegistries() {
    }

    public static synchronized void registerAndFreeze() {
        if (frozen) {
            return;
        }
        NeoForge.EVENT_BUS.post(new RegisterGachaRuntimeTypesEvent(
                PARAMETER_TYPES, CONSUMPTION_HANDLERS, REWARD_DELIVERY_HANDLERS, EXECUTION_STEP_TYPES));
        PARAMETER_TYPES.freeze();
        CONSUMPTION_HANDLERS.freeze();
        REWARD_DELIVERY_HANDLERS.freeze();
        EXECUTION_STEP_TYPES.freeze();
        frozen = true;
    }

    private static GachaExecutionStepTypeRegistry createExecutionStepTypes() {
        GachaExecutionStepTypeRegistry registry = new GachaExecutionStepTypeRegistry();
        GachaBuiltinExecutionSteps.register(registry);
        return registry;
    }

    private static GachaConsumptionHandlerRegistry createConsumptionHandlers() {
        GachaConsumptionHandlerRegistry registry = new GachaConsumptionHandlerRegistry();
        registry.register(new GachaPlayerInventoryConsumptionHandler());
        return registry;
    }

    private static GachaRewardDeliveryHandlerRegistry createRewardDeliveryHandlers() {
        GachaRewardDeliveryHandlerRegistry registry = new GachaRewardDeliveryHandlerRegistry();
        registry.register(new GachaInboxRewardDeliveryHandler(GachaBuiltinRewardDeliveryTypes.INBOX,
                GachaRewardInboxEntryKind.REWARD));
        registry.register(new GachaInboxRewardDeliveryHandler(GachaBuiltinRewardDeliveryTypes.RETURN,
                GachaRewardInboxEntryKind.RETURN));
        registry.register(new GachaPlayerRewardDeliveryHandler(GachaBuiltinRewardDeliveryTypes.INVENTORY,
                GachaRewardDeliveryMode.INVENTORY));
        registry.register(new GachaPlayerRewardDeliveryHandler(GachaBuiltinRewardDeliveryTypes.DROP,
                GachaRewardDeliveryMode.DROP));
        return registry;
    }
}

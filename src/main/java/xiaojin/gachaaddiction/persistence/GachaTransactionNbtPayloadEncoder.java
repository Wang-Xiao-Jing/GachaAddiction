package xiaojin.gachaaddiction.persistence;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import xiaojin.gachaaddiction.cardpool.transaction.*;
import xiaojin.gachaaddiction.reward.problem.GachaProblem;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class GachaTransactionNbtPayloadEncoder implements GachaTransactionPayloadEncoder {
    public static final int PAYLOAD_VERSION = 2;

    private static final String PAYLOAD_VERSION_KEY = "payload_version";
    private static final String TRANSACTION_ID_KEY = "transaction_id";
    private static final String PLAYER_ID_KEY = "player_id";
    private static final String EXECUTION_SOURCE_KEY = "execution_source";
    private static final String SOURCE_SNAPSHOT_KEY = "source_snapshot";
    private static final String SOURCE_DATA_KEY = "source_data";
    private static final String TYPE_KEY = "type";
    private static final String DATA_KEY = "data";
    private static final String CONSUMPTION_KEY = "consumption";
    private static final String RESERVATIONS_KEY = "reservations";
    private static final String REWARDS_KEY = "rewards";
    private static final String PARAMETER_CHANGES_KEY = "parameter_changes";
    private static final String PROBLEMS_KEY = "problems";
    private static final String CODE_KEY = "code";
    private static final String SEVERITY_KEY = "severity";
    private static final String TRANSLATION_KEY = "translation_key";
    private static final String CONTEXT_KEY = "context";

    private final HolderLookup.Provider registries;
    private final GachaTransactionParameterSchemaResolver parameterSchemas;

    public GachaTransactionNbtPayloadEncoder(HolderLookup.Provider registries,
                                             GachaTransactionParameterSchemaResolver parameterSchemas) {
        this.registries = Objects.requireNonNull(registries, "registries");
        this.parameterSchemas = Objects.requireNonNull(parameterSchemas, "parameterSchemas");
    }

    private static CompoundTag base(GachaTransactionContext context) {
        CompoundTag payload = new CompoundTag();
        payload.putInt(PAYLOAD_VERSION_KEY, PAYLOAD_VERSION);
        payload.putUUID(TRANSACTION_ID_KEY, context.transactionId().value());
        payload.putUUID(PLAYER_ID_KEY, context.playerId());
        payload.putString(EXECUTION_SOURCE_KEY, context.source().id().toString());
        context.source().snapshotHash().ifPresent(value -> payload.putString(SOURCE_SNAPSHOT_KEY, value));
        payload.put(SOURCE_DATA_KEY, context.source().data());
        return payload;
    }

    private static ListTag encodeReservations(List<GachaConsumptionReservation> reservations) {
        ListTag encoded = new ListTag();
        for (GachaConsumptionReservation reservation : reservations) {
            encoded.add(typedData(reservation.handlerType().toString(), reservation.data()));
        }
        return encoded;
    }

    private static CompoundTag typedData(String type, CompoundTag data) {
        CompoundTag encoded = new CompoundTag();
        encoded.putString(TYPE_KEY, type);
        encoded.put(DATA_KEY, data);
        return encoded;
    }

    private static ListTag encodeProblems(List<GachaProblem> problems) {
        ListTag encoded = new ListTag();
        for (GachaProblem problem : problems) {
            CompoundTag problemTag = new CompoundTag();
            problemTag.putString(CODE_KEY, problem.code().toString());
            problemTag.putString(SEVERITY_KEY, problem.severity().name());
            problemTag.putString(TRANSLATION_KEY, problem.translationKey());
            CompoundTag context = new CompoundTag();
            for (Map.Entry<String, String> entry : problem.context().entrySet()) {
                context.putString(entry.getKey(), entry.getValue());
            }
            problemTag.put(CONTEXT_KEY, context);
            encoded.add(problemTag);
        }
        return encoded;
    }

    @Override
    public CompoundTag prepared(GachaTransactionContext context, List<GachaConsumptionSpec> consumption) {
        CompoundTag payload = base(context);
        ListTag encodedConsumption = new ListTag();
        for (GachaConsumptionSpec specification : consumption) {
            encodedConsumption.add(typedData(specification.type().toString(), specification.data()));
        }
        payload.put(CONSUMPTION_KEY, encodedConsumption);
        return payload;
    }

    @Override
    public CompoundTag reserved(GachaTransactionContext context,
                                List<GachaConsumptionReservation> reservations) {
        CompoundTag payload = base(context);
        payload.put(RESERVATIONS_KEY, encodeReservations(reservations));
        return payload;
    }

    @Override
    public CompoundTag rewardComputed(GachaTransactionContext context, GachaTransactionResult result,
                                      List<GachaConsumptionReservation> reservations) {
        CompoundTag payload = base(context);
        payload.put(RESERVATIONS_KEY, encodeReservations(reservations));
        ListTag rewards = new ListTag();
        result.rewards().forEach(stack -> rewards.add(stack.save(registries)));
        payload.put(REWARDS_KEY, rewards);
        payload.put(PARAMETER_CHANGES_KEY, new GachaParameterNbtEncoder(
                parameterSchemas.resolve(context.source()), registries)
                .encode(result.parameterChanges()));
        payload.put(PROBLEMS_KEY, encodeProblems(result.problems()));
        return payload;
    }
}

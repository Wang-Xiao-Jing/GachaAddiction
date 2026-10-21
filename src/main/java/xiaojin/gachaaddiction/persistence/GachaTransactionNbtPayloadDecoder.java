package xiaojin.gachaaddiction.persistence;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import xiaojin.gachaaddiction.cardpool.transaction.*;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterChangeSet;
import xiaojin.gachaaddiction.reward.problem.GachaProblem;
import xiaojin.gachaaddiction.reward.problem.GachaProblemSeverity;

import java.util.*;

public final class GachaTransactionNbtPayloadDecoder {
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
    private final IGachaTransactionParameterSchemaResolver parameterSchemas;

    public GachaTransactionNbtPayloadDecoder(HolderLookup.Provider registries,
                                             IGachaTransactionParameterSchemaResolver parameterSchemas) {
        this.registries = Objects.requireNonNull(registries, "registries");
        this.parameterSchemas = Objects.requireNonNull(parameterSchemas, "parameterSchemas");
    }

    private static GachaTransactionSource decodeSource(CompoundTag payload) {
        Optional<String> snapshot = payload.contains(SOURCE_SNAPSHOT_KEY, Tag.TAG_STRING)
                ? Optional.of(payload.getString(SOURCE_SNAPSHOT_KEY)) : Optional.empty();
        return new GachaTransactionSource(ResourceLocation.parse(payload.getString(EXECUTION_SOURCE_KEY)),
                snapshot, payload.getCompound(SOURCE_DATA_KEY));
    }

    private static List<GachaConsumptionSpec> decodeConsumption(CompoundTag payload) {
        List<GachaConsumptionSpec> decoded = new ArrayList<>();
        ListTag entries = payload.getList(CONSUMPTION_KEY, Tag.TAG_COMPOUND);
        for (int index = 0; index < entries.size(); index++) {
            CompoundTag entry = entries.getCompound(index);
            decoded.add(new GachaConsumptionSpec(ResourceLocation.parse(entry.getString(TYPE_KEY)),
                    entry.getCompound(DATA_KEY)));
        }
        return List.copyOf(decoded);
    }

    private static List<GachaConsumptionReservation> decodeReservations(CompoundTag payload) {
        List<GachaConsumptionReservation> decoded = new ArrayList<>();
        ListTag entries = payload.getList(RESERVATIONS_KEY, Tag.TAG_COMPOUND);
        for (int index = 0; index < entries.size(); index++) {
            CompoundTag entry = entries.getCompound(index);
            decoded.add(new GachaConsumptionReservation(ResourceLocation.parse(entry.getString(TYPE_KEY)),
                    entry.getCompound(DATA_KEY)));
        }
        return List.copyOf(decoded);
    }

    private static List<GachaProblem> decodeProblems(CompoundTag payload) {
        List<GachaProblem> decoded = new ArrayList<>();
        ListTag problems = payload.getList(PROBLEMS_KEY, Tag.TAG_COMPOUND);
        for (int index = 0; index < problems.size(); index++) {
            CompoundTag problem = problems.getCompound(index);
            CompoundTag encodedContext = problem.getCompound(CONTEXT_KEY);
            Map<String, String> context = new LinkedHashMap<>();
            for (String key : encodedContext.getAllKeys()) {
                context.put(key, encodedContext.getString(key));
            }
            decoded.add(new GachaProblem(ResourceLocation.parse(problem.getString(CODE_KEY)),
                    GachaProblemSeverity.valueOf(problem.getString(SEVERITY_KEY)),
                    problem.getString(TRANSLATION_KEY), context));
        }
        return List.copyOf(decoded);
    }

    public GachaDecodedTransactionPayload decode(CompoundTag payload) {
        requireVersion(payload);
        GachaTransactionContext context = new GachaTransactionContext(
                new GachaTransactionId(payload.getUUID(TRANSACTION_ID_KEY)),
                payload.getUUID(PLAYER_ID_KEY),
                decodeSource(payload));
        List<GachaConsumptionSpec> consumption = decodeConsumption(payload);
        List<GachaConsumptionReservation> reservations = decodeReservations(payload);
        Optional<GachaTransactionResult> result = payload.contains(REWARDS_KEY)
                ? Optional.of(decodeResult(context, payload)) : Optional.empty();
        return new GachaDecodedTransactionPayload(context, consumption, reservations, result);
    }

    private void requireVersion(CompoundTag payload) {
        int version = payload.getInt(PAYLOAD_VERSION_KEY);
        if (version != GachaTransactionNbtPayloadEncoder.PAYLOAD_VERSION) {
            throw new IllegalArgumentException("Unsupported transaction payload version: " + version);
        }
    }

    private GachaTransactionResult decodeResult(GachaTransactionContext context, CompoundTag payload) {
        List<ItemStack> rewards = new ArrayList<>();
        ListTag encodedRewards = payload.getList(REWARDS_KEY, Tag.TAG_COMPOUND);
        for (int index = 0; index < encodedRewards.size(); index++) {
            ItemStack stack = ItemStack.parseOptional(registries, encodedRewards.getCompound(index));
            if (!stack.isEmpty()) {
                rewards.add(stack);
            }
        }
        GachaParameterChangeSet changes = new GachaParameterNbtDecoder(
                parameterSchemas.resolve(context.source()), registries)
                .decodeChangeSet(
                        payload.getCompound(PARAMETER_CHANGES_KEY));
        return new GachaTransactionResult(rewards, changes, decodeProblems(payload));
    }
}

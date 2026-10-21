package xiaojin.gachaaddiction.execution;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.storage.loot.LootParams;
import xiaojin.gachaaddiction.reward.IGachaLootTableResolver;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterSet;

import java.util.Objects;
import java.util.OptionalInt;

public record GachaExecutionRequest(GachaExecutionSource source, GachaExecutionMode mode,
                                    GachaParameterSet parameters, String inputFingerprint,
                                    LootParams lootParams, IGachaLootTableResolver lootTableResolver,
                                    RandomSource random, OptionalInt roundsOverride) {
    public GachaExecutionRequest(GachaExecutionSource source, GachaExecutionMode mode,
                                 GachaParameterSet parameters, String inputFingerprint,
                                 LootParams lootParams, IGachaLootTableResolver lootTableResolver,
                                 RandomSource random) {
        this(source, mode, parameters, inputFingerprint, lootParams, lootTableResolver, random,
                OptionalInt.empty());
    }

    public GachaExecutionRequest(GachaExecutionSource source, GachaExecutionMode mode,
                                 GachaParameterSet parameters, String inputFingerprint,
                                 LootParams lootParams, IGachaLootTableResolver lootTableResolver,
                                 RandomSource random, int rounds) {
        this(source, mode, parameters, inputFingerprint, lootParams, lootTableResolver, random,
                OptionalInt.of(rounds));
    }

    public GachaExecutionRequest {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(mode, "mode");
        Objects.requireNonNull(parameters, "parameters");
        Objects.requireNonNull(inputFingerprint, "inputFingerprint");
        Objects.requireNonNull(lootParams, "lootParams");
        Objects.requireNonNull(lootTableResolver, "lootTableResolver");
        Objects.requireNonNull(random, "random");
        Objects.requireNonNull(roundsOverride, "roundsOverride");
        if (roundsOverride.isPresent() && roundsOverride.getAsInt() <= 0) {
            throw new IllegalArgumentException("Rounds override must be positive");
        }
    }
}

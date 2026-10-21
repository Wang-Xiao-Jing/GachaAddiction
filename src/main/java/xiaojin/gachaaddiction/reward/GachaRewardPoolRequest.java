package xiaojin.gachaaddiction.reward;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterSet;

import java.util.Objects;
import java.util.Optional;

public record GachaRewardPoolRequest(int rounds, LootParams lootParams, GachaParameterSet parameters,
                                     String inputFingerprint, GachaLootTableResolver lootTableResolver,
                                     RandomSource random) {
    public GachaRewardPoolRequest {
        if (rounds <= 0) {
            throw new IllegalArgumentException("Rounds must be positive");
        }
        Objects.requireNonNull(lootParams, "lootParams");
        Objects.requireNonNull(parameters, "parameters");
        Objects.requireNonNull(inputFingerprint, "inputFingerprint");
        Objects.requireNonNull(lootTableResolver, "lootTableResolver");
        Objects.requireNonNull(random, "random");
    }

    public LootContext createLootContext() {
        return new LootContext.Builder(lootParams).withOptionalRandomSource(random).create(Optional.empty());
    }
}

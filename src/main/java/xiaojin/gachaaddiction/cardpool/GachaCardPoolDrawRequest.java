package xiaojin.gachaaddiction.cardpool;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.storage.loot.LootParams;
import xiaojin.gachaaddiction.cardpool.parameter.GachaRequestAuthority;
import xiaojin.gachaaddiction.cardpool.transaction.GachaDurability;
import xiaojin.gachaaddiction.reward.GachaLootTableResolver;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterSet;

import java.util.Objects;
import java.util.OptionalInt;
import java.util.UUID;

public record GachaCardPoolDrawRequest(UUID playerId, ResourceLocation cardPoolId,
                                       ResourceLocation packageId, GachaParameterSet requestParameters,
                                       GachaRequestAuthority authority, GachaDurability durability,
                                       LootParams lootParams, GachaLootTableResolver lootTableResolver,
                                       RandomSource random, OptionalInt roundsOverride) {
    public GachaCardPoolDrawRequest(UUID playerId, ResourceLocation cardPoolId,
                                    ResourceLocation packageId, GachaParameterSet requestParameters,
                                    GachaRequestAuthority authority, GachaDurability durability,
                                    LootParams lootParams, GachaLootTableResolver lootTableResolver,
                                    RandomSource random) {
        this(playerId, cardPoolId, packageId, requestParameters, authority, durability, lootParams,
                lootTableResolver, random, OptionalInt.empty());
    }

    public GachaCardPoolDrawRequest(UUID playerId, ResourceLocation cardPoolId,
                                    ResourceLocation packageId, GachaParameterSet requestParameters,
                                    GachaRequestAuthority authority, GachaDurability durability,
                                    LootParams lootParams, GachaLootTableResolver lootTableResolver,
                                    RandomSource random, int rounds) {
        this(playerId, cardPoolId, packageId, requestParameters, authority, durability, lootParams,
                lootTableResolver, random, OptionalInt.of(rounds));
    }

    public GachaCardPoolDrawRequest {
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(cardPoolId, "cardPoolId");
        Objects.requireNonNull(packageId, "packageId");
        Objects.requireNonNull(requestParameters, "requestParameters");
        Objects.requireNonNull(authority, "authority");
        Objects.requireNonNull(durability, "durability");
        Objects.requireNonNull(lootParams, "lootParams");
        Objects.requireNonNull(lootTableResolver, "lootTableResolver");
        Objects.requireNonNull(random, "random");
        Objects.requireNonNull(roundsOverride, "roundsOverride");
        if (roundsOverride.isPresent() && roundsOverride.getAsInt() <= 0) {
            throw new IllegalArgumentException("Rounds override must be positive");
        }
    }
}

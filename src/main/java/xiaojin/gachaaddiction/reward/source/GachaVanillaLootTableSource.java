package xiaojin.gachaaddiction.reward.source;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;
import xiaojin.gachaaddiction.reward.model.GachaLogicalReward;
import xiaojin.gachaaddiction.reward.provenance.GachaRewardOrigin;
import xiaojin.gachaaddiction.reward.provenance.GachaRewardOriginTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record GachaVanillaLootTableSource(ResourceLocation id, ResourceKey<LootTable> lootTable)
        implements IGachaRewardSource {
    public GachaVanillaLootTableSource {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(lootTable, "lootTable");
    }

    @Override
    public List<GachaLogicalReward> generate(GachaSourceGenerationContext context) {
        Objects.requireNonNull(context, "context");
        LootTable resolvedTable = context.request().lootTableResolver().resolve(lootTable)
                .orElseThrow(() -> new IllegalArgumentException("Unknown loot table: " + lootTable.location()));
        List<GachaRewardOrigin> provenance = List.of(
                new GachaRewardOrigin(GachaRewardOriginTypes.SOURCE, id),
                new GachaRewardOrigin(GachaRewardOriginTypes.VANILLA_LOOT_TABLE, lootTable.location()));
        List<GachaLogicalReward> rewards = new ArrayList<>();
        resolvedTable.getRandomItems(context.request().lootParams(), context.request().random()).forEach(stack ->
                context.rewardFactory().create(stack, provenance).ifPresent(rewards::add));
        return List.copyOf(rewards);
    }
}

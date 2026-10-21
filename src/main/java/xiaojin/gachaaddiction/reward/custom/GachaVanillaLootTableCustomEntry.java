package xiaojin.gachaaddiction.reward.custom;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;
import xiaojin.gachaaddiction.reward.model.GachaLogicalReward;
import xiaojin.gachaaddiction.reward.provenance.GachaRewardOrigin;
import xiaojin.gachaaddiction.reward.provenance.GachaRewardOriginTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record GachaVanillaLootTableCustomEntry(ResourceLocation id, ResourceKey<LootTable> lootTable)
        implements GachaCustomEntry {
    public GachaVanillaLootTableCustomEntry {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(lootTable, "lootTable");
    }

    @Override
    public List<GachaLogicalReward> generate(GachaCustomEntryContext context) {
        LootTable resolvedTable = context.sourceContext().request().lootTableResolver().resolve(lootTable)
                .orElseThrow(() -> new IllegalArgumentException("Unknown loot table: " + lootTable.location()));
        List<GachaRewardOrigin> provenance = new ArrayList<>(context.provenance());
        provenance.add(new GachaRewardOrigin(GachaRewardOriginTypes.VANILLA_LOOT_TABLE, lootTable.location()));
        List<GachaLogicalReward> rewards = new ArrayList<>();
        resolvedTable.getRandomItems(context.sourceContext().request().lootParams(),
                context.sourceContext().request().random()).forEach(stack ->
                context.sourceContext().rewardFactory().create(stack, provenance).ifPresent(rewards::add));
        return List.copyOf(rewards);
    }
}

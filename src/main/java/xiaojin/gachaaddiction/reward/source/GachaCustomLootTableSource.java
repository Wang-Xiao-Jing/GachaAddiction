package xiaojin.gachaaddiction.reward.source;

import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.reward.custom.GachaCustomEntryContext;
import xiaojin.gachaaddiction.reward.custom.GachaCustomLootTable;
import xiaojin.gachaaddiction.reward.custom.GachaCustomTableResolver;
import xiaojin.gachaaddiction.reward.model.GachaLogicalReward;
import xiaojin.gachaaddiction.reward.provenance.GachaRewardOrigin;
import xiaojin.gachaaddiction.reward.provenance.GachaRewardOriginTypes;

import java.util.List;
import java.util.Objects;

public record GachaCustomLootTableSource(ResourceLocation id, ResourceLocation table,
                                         GachaCustomTableResolver resolver) implements GachaRewardSource {
    public GachaCustomLootTableSource {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(table, "table");
        Objects.requireNonNull(resolver, "resolver");
    }

    @Override
    public List<GachaLogicalReward> generate(GachaSourceGenerationContext context) {
        GachaCustomLootTable resolvedTable = resolver.resolveTable(table).orElseThrow(() ->
                new IllegalArgumentException("Unknown custom loot table: " + table));
        GachaCustomEntryContext entryContext = new GachaCustomEntryContext(context,
                List.of(new GachaRewardOrigin(GachaRewardOriginTypes.SOURCE, id)), List.of());
        return resolvedTable.generate(entryContext);
    }
}

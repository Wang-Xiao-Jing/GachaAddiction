package xiaojin.gachaaddiction.reward.custom;

import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.reward.model.GachaLogicalReward;

import java.util.List;
import java.util.Objects;

public record GachaCustomTableReferenceEntry(ResourceLocation id, ResourceLocation table,
                                             IGachaCustomTableResolver resolver) implements IGachaCustomEntry {
    public GachaCustomTableReferenceEntry {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(table, "table");
        Objects.requireNonNull(resolver, "resolver");
    }

    @Override
    public List<GachaLogicalReward> generate(GachaCustomEntryContext context) {
        return resolver.resolveTable(table).orElseThrow(() ->
                new IllegalArgumentException("Unknown custom loot table: " + table)).generate(context);
    }
}

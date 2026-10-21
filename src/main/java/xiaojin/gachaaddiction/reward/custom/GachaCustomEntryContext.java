package xiaojin.gachaaddiction.reward.custom;

import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.reward.number.GachaNumberContext;
import xiaojin.gachaaddiction.reward.provenance.GachaRewardOrigin;
import xiaojin.gachaaddiction.reward.provenance.GachaRewardOriginTypes;
import xiaojin.gachaaddiction.reward.source.GachaSourceGenerationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record GachaCustomEntryContext(GachaSourceGenerationContext sourceContext,
                                      List<GachaRewardOrigin> provenance,
                                      List<ResourceLocation> tableStack) {
    public GachaCustomEntryContext {
        Objects.requireNonNull(sourceContext, "sourceContext");
        provenance = List.copyOf(provenance);
        tableStack = List.copyOf(tableStack);
    }

    public GachaNumberContext numberContext() {
        return new GachaNumberContext(sourceContext.request().parameters(), sourceContext.currentParameters(),
                sourceContext.request().random());
    }

    public GachaCustomEntryContext enterTable(ResourceLocation tableId) {
        Objects.requireNonNull(tableId, "tableId");
        if (tableStack.contains(tableId)) {
            throw new IllegalArgumentException("Recursive custom loot table cycle: " + tableId);
        }
        List<ResourceLocation> tables = new ArrayList<>(tableStack);
        tables.add(tableId);
        sourceContext.budget().requireDepth(tables.size());
        List<GachaRewardOrigin> origins = new ArrayList<>(provenance);
        origins.add(new GachaRewardOrigin(GachaRewardOriginTypes.CUSTOM_TABLE, tableId));
        return new GachaCustomEntryContext(sourceContext, origins, tables);
    }

    public GachaCustomEntryContext enterEntry(ResourceLocation entryId) {
        sourceContext.budget().visitEntry();
        List<GachaRewardOrigin> origins = new ArrayList<>(provenance);
        origins.add(new GachaRewardOrigin(GachaRewardOriginTypes.CUSTOM_ENTRY,
                Objects.requireNonNull(entryId, "entryId")));
        return new GachaCustomEntryContext(sourceContext, origins, tableStack);
    }
}

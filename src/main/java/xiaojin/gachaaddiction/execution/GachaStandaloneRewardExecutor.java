package xiaojin.gachaaddiction.execution;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;
import xiaojin.gachaaddiction.reward.GachaLogicalRewardPoolResult;
import xiaojin.gachaaddiction.reward.GachaRewardPoolRequest;
import xiaojin.gachaaddiction.reward.GachaRewardPoolResult;
import xiaojin.gachaaddiction.reward.model.GachaLogicalReward;
import xiaojin.gachaaddiction.reward.model.GachaRewardFactory;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterEditor;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterSet;
import xiaojin.gachaaddiction.reward.quantity.GachaExecutionLimits;
import xiaojin.gachaaddiction.reward.quantity.GachaExecutionUsage;
import xiaojin.gachaaddiction.reward.quantity.GachaRewardFinalizer;
import xiaojin.gachaaddiction.reward.source.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class GachaStandaloneRewardExecutor {
    private final GachaExecutionComponentResolver components;

    public GachaStandaloneRewardExecutor(GachaExecutionComponentResolver components) {
        this.components = Objects.requireNonNull(components, "components");
    }

    public GachaRewardPoolResult drawCustomTable(ResourceLocation tableId, GachaRewardPoolRequest request,
                                                 GachaExecutionLimits limits) {
        Objects.requireNonNull(tableId, "tableId");
        return draw(new GachaCustomLootTableSource(tableId, tableId, components), request, limits);
    }

    public GachaRewardPoolResult drawVanillaLootTable(ResourceKey<LootTable> lootTable,
                                                      GachaRewardPoolRequest request,
                                                      GachaExecutionLimits limits) {
        Objects.requireNonNull(lootTable, "lootTable");
        return draw(new GachaVanillaLootTableSource(lootTable.location(), lootTable), request, limits);
    }

    public GachaRewardPoolResult draw(GachaRewardSource source, GachaRewardPoolRequest request,
                                      GachaExecutionLimits limits) {
        GachaLogicalRewardPoolResult result = drawLogical(source, request, limits);
        return new GachaRewardPoolResult(GachaRewardFinalizer.finalizeRewards(result.rewards(), limits),
                result.parameterChanges(), result.problems());
    }

    public GachaLogicalRewardPoolResult drawLogical(GachaRewardSource source, GachaRewardPoolRequest request,
                                                    GachaExecutionLimits limits) {
        return drawLogical(source, request, request.parameters(), limits);
    }

    GachaLogicalRewardPoolResult drawLogical(GachaRewardSource source, GachaRewardPoolRequest request,
                                             GachaParameterSet currentParameters, GachaExecutionLimits limits) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(currentParameters, "currentParameters");
        Objects.requireNonNull(limits, "limits");
        if (request.rounds() > limits.maxRounds()) {
            throw new IllegalArgumentException("Direct reward source round count exceeds execution limit");
        }
        if (request.rounds() > limits.maxSourceSelections()) {
            throw new IllegalArgumentException("Direct reward source selection count exceeds execution limit");
        }
        GachaGenerationBudget generationBudget = new GachaGenerationBudget(limits.maxCustomTableDepth(),
                limits.maxCustomEntryVisits());
        GachaRewardFactory rewardFactory = new GachaRewardFactory();
        List<GachaLogicalReward> logicalRewards = new ArrayList<>();
        for (int round = 0; round < request.rounds(); round++) {
            GachaSourceGenerationContext context = new GachaSourceGenerationContext(request, round, source.id(),
                    currentParameters, rewardFactory, generationBudget);
            for (var reward : source.generate(context)) {
                logicalRewards.add(reward.withPresentationRound(round));
            }
            if (logicalRewards.size() > limits.maxLogicalRewards()) {
                throw new IllegalArgumentException("Direct reward source logical reward count exceeds execution limit");
            }
        }
        GachaParameterEditor parameters = new GachaParameterEditor(request.inputFingerprint(), currentParameters);
        return new GachaLogicalRewardPoolResult(logicalRewards, parameters.buildChangeSet(), List.of(),
                java.util.Optional.of(limits), new GachaExecutionUsage(request.rounds(), 0,
                generationBudget.entryVisits(), generationBudget.maximumDepthObserved()), request.rounds());
    }
}

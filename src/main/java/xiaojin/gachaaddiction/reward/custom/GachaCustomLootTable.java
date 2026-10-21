package xiaojin.gachaaddiction.reward.custom;

import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.reward.model.GachaLogicalReward;
import xiaojin.gachaaddiction.reward.number.IGachaLongProvider;
import xiaojin.gachaaddiction.reward.probability.IGachaProbabilityDescribable;
import xiaojin.gachaaddiction.reward.probability.GachaProbabilityDescription;
import xiaojin.gachaaddiction.reward.probability.GachaProbabilitySelectionMode;
import xiaojin.gachaaddiction.reward.source.GachaTableSelectionStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class GachaCustomLootTable implements IGachaProbabilityDescribable {
    private final ResourceLocation id;
    private final GachaTableSelectionStrategy selectionStrategy;
    private final IGachaLongProvider selectionCount;
    private final List<GachaWeightedCustomEntry> entries;

    public GachaCustomLootTable(ResourceLocation id, GachaTableSelectionStrategy selectionStrategy,
                                IGachaLongProvider selectionCount, List<GachaWeightedCustomEntry> entries) {
        this.id = Objects.requireNonNull(id, "id");
        this.selectionStrategy = Objects.requireNonNull(selectionStrategy, "selectionStrategy");
        this.selectionCount = Objects.requireNonNull(selectionCount, "selectionCount");
        this.entries = List.copyOf(entries);
        if (this.entries.isEmpty()) {
            throw new IllegalArgumentException("Custom loot table must contain at least one entry: " + id);
        }
    }

    private static EvaluatedEntry selectOne(List<EvaluatedEntry> eligible, GachaCustomEntryContext context) {
        if (eligible.isEmpty()) {
            throw new IllegalArgumentException("No eligible custom loot table entry");
        }
        double totalWeight = eligible.stream().mapToDouble(EvaluatedEntry::weight).sum();
        if (!Double.isFinite(totalWeight) || totalWeight <= 0) {
            throw new IllegalArgumentException("Custom entry total weight must be finite and positive");
        }
        double target = context.sourceContext().request().random().nextDouble() * totalWeight;
        for (EvaluatedEntry entry : eligible) {
            target -= entry.weight();
            if (target < 0) {
                return entry;
            }
        }
        return eligible.getLast();
    }

    private static List<EvaluatedEntry> selectMultiple(List<EvaluatedEntry> eligible, int count,
                                                       GachaCustomEntryContext context) {
        if (count <= 0) {
            throw new IllegalArgumentException("Custom entry selection count must be positive");
        }
        List<EvaluatedEntry> remaining = new ArrayList<>(eligible);
        List<EvaluatedEntry> selected = new ArrayList<>();
        int actualCount = Math.min(count, remaining.size());
        for (int index = 0; index < actualCount; index++) {
            EvaluatedEntry entry = selectOne(remaining, context);
            selected.add(entry);
            for (int candidateIndex = 0; candidateIndex < remaining.size(); candidateIndex++) {
                if (remaining.get(candidateIndex).entry() == entry.entry()) {
                    remaining.remove(candidateIndex);
                    break;
                }
            }
        }
        return List.copyOf(selected);
    }

    private static List<EvaluatedEntry> selectIndependent(List<EvaluatedEntry> eligible,
                                                          GachaCustomEntryContext context) {
        List<EvaluatedEntry> selected = new ArrayList<>();
        for (EvaluatedEntry entry : eligible) {
            if (entry.weight() > 1) {
                throw new IllegalArgumentException("Independent custom entry chance cannot exceed one: "
                        + entry.entry().id());
            }
            if (context.sourceContext().request().random().nextDouble() < entry.weight()) {
                selected.add(entry);
            }
        }
        return List.copyOf(selected);
    }

    public ResourceLocation id() {
        return id;
    }

    @Override
    public GachaProbabilityDescription describeProbability() {
        GachaProbabilitySelectionMode mode = switch (selectionStrategy) {
            case SELECT_ONE -> GachaProbabilitySelectionMode.WEIGHTED_SINGLE;
            case SELECT_MULTIPLE -> GachaProbabilitySelectionMode.WEIGHTED_WITHOUT_REPLACEMENT;
            case COLLECT_MATCHING -> GachaProbabilitySelectionMode.COLLECT_ALL_ELIGIBLE;
            case FIRST_MATCH -> GachaProbabilitySelectionMode.FIRST_ELIGIBLE;
            case SEQUENCE -> GachaProbabilitySelectionMode.SEQUENCE;
            case INDEPENDENT_CHANCE -> GachaProbabilitySelectionMode.INDEPENDENT_CHANCE;
        };
        List<GachaProbabilityDescription.Candidate> candidates = entries.stream()
                .map(entry -> new GachaProbabilityDescription.Candidate(entry.entry().id(),
                        entry.weight().expression()))
                .toList();
        return new GachaProbabilityDescription(id, mode, selectionCount.expression(), candidates);
    }

    public List<GachaLogicalReward> generate(GachaCustomEntryContext parentContext) {
        GachaCustomEntryContext context = parentContext.enterTable(id);
        List<EvaluatedEntry> eligible = evaluate(context);
        List<EvaluatedEntry> selected = select(eligible, context);
        List<GachaLogicalReward> rewards = new ArrayList<>();
        for (EvaluatedEntry entry : selected) {
            rewards.addAll(entry.entry().generateSelected(entry.context()));
        }
        return List.copyOf(rewards);
    }

    private List<EvaluatedEntry> evaluate(GachaCustomEntryContext context) {
        List<EvaluatedEntry> eligible = new ArrayList<>();
        for (GachaWeightedCustomEntry weightedEntry : entries) {
            GachaCustomEntryContext entryContext = context.enterEntry(weightedEntry.entry().id());
            if (!weightedEntry.entry().isEligible(entryContext)) {
                continue;
            }
            double weight = weightedEntry.weight().get(entryContext.numberContext());
            if (!Double.isFinite(weight) || weight < 0) {
                throw new IllegalArgumentException("Custom entry weight must be finite and non-negative: "
                        + weightedEntry.entry().id());
            }
            if (weight > 0) {
                eligible.add(new EvaluatedEntry(weightedEntry.entry(), weight, entryContext));
            }
        }
        return eligible;
    }

    private List<EvaluatedEntry> select(List<EvaluatedEntry> eligible, GachaCustomEntryContext context) {
        return switch (selectionStrategy) {
            case SELECT_ONE -> eligible.isEmpty() ? List.of() : List.of(selectOne(eligible, context));
            case SELECT_MULTIPLE -> selectMultiple(eligible,
                    Math.toIntExact(selectionCount.get(context.numberContext())), context);
            case COLLECT_MATCHING, SEQUENCE -> eligible;
            case FIRST_MATCH -> eligible.isEmpty() ? List.of() : List.of(eligible.getFirst());
            case INDEPENDENT_CHANCE -> selectIndependent(eligible, context);
        };
    }

    private record EvaluatedEntry(IGachaCustomEntry entry, double weight, GachaCustomEntryContext context) {
    }
}

package xiaojin.gachaaddiction.reward.source;

import xiaojin.gachaaddiction.reward.number.GachaNumberContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class GachaSourceSelector {
    private GachaSourceSelector() {
    }

    public static List<IGachaRewardSource> select(List<GachaWeightedSource> sources,
                                                  GachaSourceSelectionStrategy strategy, int selectionCount,
                                                  GachaNumberContext context) {
        Objects.requireNonNull(sources, "sources");
        Objects.requireNonNull(strategy, "strategy");
        Objects.requireNonNull(context, "context");
        if (selectionCount <= 0) {
            throw new IllegalArgumentException("Selection count must be positive");
        }

        List<EvaluatedSource> eligible = evaluate(sources, context);
        return switch (strategy) {
            case SELECT_ONE_SOURCE -> eligible.isEmpty() ? List.of() : List.of(selectOne(eligible, context));
            case COLLECT_ALL_SOURCES -> eligible.stream().map(EvaluatedSource::source).toList();
            case SELECT_MULTIPLE_SOURCES -> selectMultiple(eligible, selectionCount, context);
            case FIRST_MATCH -> eligible.isEmpty() ? List.of() : List.of(eligible.getFirst().source());
        };
    }

    private static List<EvaluatedSource> evaluate(List<GachaWeightedSource> sources, GachaNumberContext context) {
        List<EvaluatedSource> eligible = new ArrayList<>();
        for (GachaWeightedSource source : sources) {
            double weight = source.weight().get(context);
            if (!Double.isFinite(weight) || weight < 0) {
                throw new IllegalArgumentException("Source weight must be finite and non-negative: "
                        + source.source().id());
            }
            if (weight > 0) {
                eligible.add(new EvaluatedSource(source.source(), weight));
            }
        }
        return eligible;
    }

    private static IGachaRewardSource selectOne(List<EvaluatedSource> sources, GachaNumberContext context) {
        if (sources.isEmpty()) {
            throw new IllegalArgumentException("No eligible reward source");
        }
        double totalWeight = sources.stream().mapToDouble(EvaluatedSource::weight).sum();
        if (!Double.isFinite(totalWeight) || totalWeight <= 0) {
            throw new IllegalArgumentException("Total source weight must be finite and positive");
        }
        double target = context.random().nextDouble() * totalWeight;
        for (EvaluatedSource source : sources) {
            target -= source.weight();
            if (target < 0) {
                return source.source();
            }
        }
        return sources.getLast().source();
    }

    private static List<IGachaRewardSource> selectMultiple(List<EvaluatedSource> sources, int selectionCount,
                                                           GachaNumberContext context) {
        List<EvaluatedSource> remaining = new ArrayList<>(sources);
        List<IGachaRewardSource> selected = new ArrayList<>();
        int count = Math.min(selectionCount, remaining.size());
        for (int index = 0; index < count; index++) {
            IGachaRewardSource source = selectOne(remaining, context);
            selected.add(source);
            for (int candidateIndex = 0; candidateIndex < remaining.size(); candidateIndex++) {
                if (remaining.get(candidateIndex).source() == source) {
                    remaining.remove(candidateIndex);
                    break;
                }
            }
        }
        return List.copyOf(selected);
    }

    private record EvaluatedSource(IGachaRewardSource source, double weight) {
    }
}

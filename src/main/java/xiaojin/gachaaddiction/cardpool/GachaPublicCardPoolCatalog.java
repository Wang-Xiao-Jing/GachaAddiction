package xiaojin.gachaaddiction.cardpool;

import net.minecraft.server.level.ServerPlayer;
import xiaojin.gachaaddiction.cardpool.parameter.GachaBooleanChoiceConstraint;
import xiaojin.gachaaddiction.cardpool.parameter.GachaEnumeratedChoiceConstraint;
import xiaojin.gachaaddiction.cardpool.parameter.GachaNumberRangeChoiceConstraint;
import xiaojin.gachaaddiction.cardpool.parameter.GachaParameterBinding;
import xiaojin.gachaaddiction.data.GachaDefinitionRepository;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class GachaPublicCardPoolCatalog {
    private GachaPublicCardPoolCatalog() {
    }

    public static List<GachaCardPoolCatalogEntry> visibleTo(ServerPlayer player) {
        Objects.requireNonNull(player, "player");
        Instant now = Instant.now();
        return GachaDefinitionRepository.INSTANCE.compiled().cardPools().values().stream()
                .filter(cardPool -> cardPool.availability().allows(player, now))
                .sorted(Comparator.comparing(cardPool -> cardPool.id().toString()))
                .map(GachaPublicCardPoolCatalog::summarize)
                .toList();
    }

    private static GachaCardPoolCatalogEntry summarize(GachaCardPoolDefinition cardPool) {
        List<GachaDrawPackageSummary> packages = cardPool.packages().values().stream()
                .sorted(Comparator.comparing(drawPackage -> drawPackage.id().toString()))
                .map(drawPackage -> new GachaDrawPackageSummary(drawPackage.id()))
                .toList();
        GachaCardPoolPresentation presentation = cardPool.presentation();
        List<GachaPlayerChoiceSummary> playerChoices = cardPool.parameterBindings().stream()
                .filter(binding -> binding.playerChoice().isPresent())
                .map(GachaPublicCardPoolCatalog::summarizeChoice)
                .toList();
        return new GachaCardPoolCatalogEntry(cardPool.id(), presentation.titleTranslationKey(),
                presentation.descriptionTranslationKey(), presentation.icon(), packages,
                playerChoices);
    }

    private static GachaPlayerChoiceSummary summarizeChoice(GachaParameterBinding<?> binding) {
        var choice = binding.playerChoice().orElseThrow();
        GachaPlayerChoiceKind kind;
        Optional<String> minimum = Optional.empty();
        Optional<String> maximum = Optional.empty();
        Optional<String> step = Optional.empty();
        List<String> values = List.of();
        if (choice.constraint() instanceof GachaNumberRangeChoiceConstraint<?>(
                java.math.BigDecimal minimum1, java.math.BigDecimal maximum1, java.math.BigDecimal step1
        )) {
            kind = GachaPlayerChoiceKind.NUMBER_RANGE;
            minimum = Optional.of(minimum1.toPlainString());
            maximum = Optional.of(maximum1.toPlainString());
            step = Optional.of(step1.toPlainString());
        } else if (choice.constraint() instanceof GachaEnumeratedChoiceConstraint<?> enumeration) {
            kind = GachaPlayerChoiceKind.ENUMERATED;
            values = enumeration.values().stream().map(Object::toString).toList();
        } else if (choice.constraint() instanceof GachaBooleanChoiceConstraint) {
            kind = GachaPlayerChoiceKind.BOOLEAN;
        } else {
            throw new IllegalArgumentException("Unsupported public player choice constraint: "
                    + choice.constraint().getClass().getName());
        }
        return new GachaPlayerChoiceSummary(binding.parameter().key().owner(), binding.parameter().key().name(),
                binding.parameter().type().id(), choice.labelTranslationKey(), kind, minimum, maximum, step, values);
    }
}

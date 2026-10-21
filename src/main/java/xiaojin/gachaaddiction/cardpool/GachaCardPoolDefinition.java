package xiaojin.gachaaddiction.cardpool;

import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.cardpool.parameter.GachaParameterBinding;
import xiaojin.gachaaddiction.cardpool.parameter.GachaPlayerChoiceLimits;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterKey;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterSchema;

import java.util.*;

public record GachaCardPoolDefinition(ResourceLocation id,
                                      Map<ResourceLocation, GachaDrawPackage> packages,
                                      List<GachaParameterBinding<?>> parameterBindings,
                                      GachaParameterSchema parameterSchema,
                                      GachaCardPoolPresentation presentation,
                                      GachaCardPoolAvailability availability,
                                      GachaRewardDeliveryMode deliveryMode,
                                      Optional<ResourceLocation> deliveryHandler) {
    public GachaCardPoolDefinition {
        Objects.requireNonNull(id, "id");
        packages = Collections.unmodifiableMap(new LinkedHashMap<>(packages));
        parameterBindings = List.copyOf(parameterBindings);
        long playerChoiceCount = parameterBindings.stream().filter(binding -> binding.playerChoice().isPresent())
                .count();
        if (playerChoiceCount > GachaPlayerChoiceLimits.MAXIMUM_CHOICES_PER_CARD_POOL) {
            throw new IllegalArgumentException("Card pool contains too many player choices: " + id);
        }
        Objects.requireNonNull(parameterSchema, "parameterSchema");
        Objects.requireNonNull(presentation, "presentation");
        Objects.requireNonNull(availability, "availability");
        Objects.requireNonNull(deliveryMode, "deliveryMode");
        Objects.requireNonNull(deliveryHandler, "deliveryHandler");
        if ((deliveryMode == GachaRewardDeliveryMode.EXTERNAL) != deliveryHandler.isPresent()) {
            throw new IllegalArgumentException("External card pool delivery must declare exactly one handler: " + id);
        }
        if (packages.isEmpty()) {
            throw new IllegalArgumentException("Card pool must contain at least one draw package: " + id);
        }
        packages.forEach((packageId, drawPackage) -> {
            if (!packageId.equals(drawPackage.id())) {
                throw new IllegalArgumentException("Draw package map key does not match package ID: " + packageId);
            }
        });
        Set<GachaParameterKey> parameterKeys = new HashSet<>();
        for (GachaParameterBinding<?> binding : parameterBindings) {
            parameterSchema.require(binding.parameter().key());
            if (!parameterKeys.add(binding.parameter().key())) {
                throw new IllegalArgumentException("Duplicate card pool parameter binding: "
                        + binding.parameter().key());
            }
        }
    }
}

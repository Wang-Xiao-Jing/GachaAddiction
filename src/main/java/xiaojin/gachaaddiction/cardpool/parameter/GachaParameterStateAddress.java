package xiaojin.gachaaddiction.cardpool.parameter;

import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterKey;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record GachaParameterStateAddress(GachaParameterBindingSource source,
                                         Optional<ResourceLocation> cardPoolId,
                                         Optional<UUID> playerId, GachaParameterKey parameterKey) {
    public GachaParameterStateAddress {
        Objects.requireNonNull(source, "source");
        cardPoolId = Objects.requireNonNull(cardPoolId, "cardPoolId");
        playerId = Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(parameterKey, "parameterKey");
        validateScope(source, cardPoolId, playerId);
    }

    public static GachaParameterStateAddress of(GachaParameterBinding<?> binding,
                                                GachaParameterBindingContext context) {
        return switch (binding.source()) {
            case PLAYER_BANNER -> new GachaParameterStateAddress(binding.source(), Optional.of(context.cardPoolId()),
                    Optional.of(context.playerId()), binding.parameter().key());
            case PLAYER_SHARED -> new GachaParameterStateAddress(binding.source(), Optional.empty(),
                    Optional.of(context.playerId()), binding.parameter().key());
            case SERVER_BANNER -> new GachaParameterStateAddress(binding.source(), Optional.of(context.cardPoolId()),
                    Optional.empty(), binding.parameter().key());
            case SERVER_SHARED -> new GachaParameterStateAddress(binding.source(), Optional.empty(), Optional.empty(),
                    binding.parameter().key());
            case CONSTANT, REQUEST -> throw new IllegalArgumentException("Binding source is not persistent: "
                    + binding.source());
        };
    }

    private static void validateScope(GachaParameterBindingSource source,
                                      Optional<ResourceLocation> cardPoolId, Optional<UUID> playerId) {
        boolean valid = switch (source) {
            case PLAYER_BANNER -> cardPoolId.isPresent() && playerId.isPresent();
            case PLAYER_SHARED -> cardPoolId.isEmpty() && playerId.isPresent();
            case SERVER_BANNER -> cardPoolId.isPresent() && playerId.isEmpty();
            case SERVER_SHARED -> cardPoolId.isEmpty() && playerId.isEmpty();
            case CONSTANT, REQUEST -> false;
        };
        if (!valid) {
            throw new IllegalArgumentException("Invalid parameter state address for source: " + source);
        }
    }
}

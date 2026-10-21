package xiaojin.gachaaddiction.cardpool.parameter;

import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.UUID;

public record GachaParameterBindingContext(ResourceLocation cardPoolId, UUID playerId) {
    public GachaParameterBindingContext {
        Objects.requireNonNull(cardPoolId, "cardPoolId");
        Objects.requireNonNull(playerId, "playerId");
    }
}

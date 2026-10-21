package xiaojin.gachaaddiction.cardpool;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.time.Instant;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.UUID;

public record GachaPreparedCardPoolDraw(UUID token, UUID playerId, ResourceLocation cardPoolId,
                                        ResourceLocation packageId, CompoundTag playerChoices,
                                        String cardPoolSnapshotHash,
                                        String titleTranslationKey, GachaCardPoolDrawPreview preview,
                                        Instant expiresAt, OptionalInt roundsOverride) {
    public GachaPreparedCardPoolDraw(UUID token, UUID playerId, ResourceLocation cardPoolId,
                                     ResourceLocation packageId, CompoundTag playerChoices,
                                     String cardPoolSnapshotHash, String titleTranslationKey,
                                     GachaCardPoolDrawPreview preview, Instant expiresAt) {
        this(token, playerId, cardPoolId, packageId, playerChoices, cardPoolSnapshotHash,
                titleTranslationKey, preview, expiresAt, OptionalInt.empty());
    }

    public GachaPreparedCardPoolDraw {
        Objects.requireNonNull(token, "token");
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(cardPoolId, "cardPoolId");
        Objects.requireNonNull(packageId, "packageId");
        playerChoices = Objects.requireNonNull(playerChoices, "playerChoices").copy();
        if (Objects.requireNonNull(cardPoolSnapshotHash, "cardPoolSnapshotHash").isBlank()) {
            throw new IllegalArgumentException("Prepared card pool snapshot hash cannot be blank");
        }
        if (Objects.requireNonNull(titleTranslationKey, "titleTranslationKey").isBlank()) {
            throw new IllegalArgumentException("Prepared card pool title cannot be blank");
        }
        Objects.requireNonNull(preview, "preview");
        Objects.requireNonNull(expiresAt, "expiresAt");
        Objects.requireNonNull(roundsOverride, "roundsOverride");
        if (roundsOverride.isPresent() && roundsOverride.getAsInt() <= 0) {
            throw new IllegalArgumentException("Rounds override must be positive");
        }
    }

    @Override
    public CompoundTag playerChoices() {
        return playerChoices.copy();
    }
}

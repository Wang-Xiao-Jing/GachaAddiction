package xiaojin.gachaaddiction.cardpool;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public final class GachaPendingDrawService {
    private final Duration lifetime;
    private final Clock clock;
    private final Map<UUID, GachaPreparedCardPoolDraw> pending = new HashMap<>();

    public GachaPendingDrawService(Duration lifetime) {
        this(lifetime, Clock.systemUTC());
    }

    GachaPendingDrawService(Duration lifetime, Clock clock) {
        Objects.requireNonNull(lifetime, "lifetime");
        if (lifetime.isZero() || lifetime.isNegative()) {
            throw new IllegalArgumentException("Pending draw lifetime must be positive");
        }
        this.lifetime = lifetime;
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public synchronized GachaPreparedCardPoolDraw prepare(UUID playerId, ResourceLocation cardPoolId,
                                                          ResourceLocation packageId, CompoundTag playerChoices,
                                                          String cardPoolSnapshotHash,
                                                          String titleTranslationKey,
                                                          GachaCardPoolDrawPreview preview) {
        return prepare(playerId, cardPoolId, packageId, playerChoices, cardPoolSnapshotHash,
                titleTranslationKey, preview, OptionalInt.empty());
    }

    public synchronized GachaPreparedCardPoolDraw prepare(UUID playerId, ResourceLocation cardPoolId,
                                                          ResourceLocation packageId, CompoundTag playerChoices,
                                                          String cardPoolSnapshotHash,
                                                          String titleTranslationKey,
                                                          GachaCardPoolDrawPreview preview,
                                                          OptionalInt roundsOverride) {
        discardExpired();
        pending.values().removeIf(draw -> draw.playerId().equals(playerId));
        Instant now = clock.instant();
        GachaPreparedCardPoolDraw prepared = new GachaPreparedCardPoolDraw(UUID.randomUUID(), playerId,
                cardPoolId, packageId, playerChoices, cardPoolSnapshotHash, titleTranslationKey, preview,
                now.plus(lifetime), roundsOverride);
        pending.put(prepared.token(), prepared);
        return prepared;
    }

    public synchronized GachaPreparedCardPoolDraw consume(UUID token, UUID playerId) {
        Objects.requireNonNull(token, "token");
        Objects.requireNonNull(playerId, "playerId");
        discardExpired();
        GachaPreparedCardPoolDraw prepared = pending.remove(token);
        if (prepared == null || !prepared.playerId().equals(playerId)) {
            throw new IllegalArgumentException("Pending draw is missing, expired, consumed, or belongs to another player");
        }
        return prepared;
    }

    public synchronized void restoreAfterFailure(GachaPreparedCardPoolDraw prepared) {
        Objects.requireNonNull(prepared, "prepared");
        discardExpired();
        if (!Instant.now(clock).isBefore(prepared.expiresAt())
                || pending.values().stream().anyMatch(draw -> draw.playerId().equals(prepared.playerId()))) {
            return;
        }
        pending.putIfAbsent(prepared.token(), prepared);
    }

    public synchronized Optional<GachaPreparedCardPoolDraw> peek(UUID token, UUID playerId) {
        Objects.requireNonNull(token, "token");
        Objects.requireNonNull(playerId, "playerId");
        discardExpired();
        GachaPreparedCardPoolDraw prepared = pending.get(token);
        if (prepared == null || !prepared.playerId().equals(playerId)) {
            return Optional.empty();
        }
        return Optional.of(prepared);
    }

    private void discardExpired() {
        Instant now = clock.instant();
        Iterator<GachaPreparedCardPoolDraw> iterator = pending.values().iterator();
        while (iterator.hasNext()) {
            if (!now.isBefore(iterator.next().expiresAt())) {
                iterator.remove();
            }
        }
    }
}

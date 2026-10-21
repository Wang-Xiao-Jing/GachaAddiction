package xiaojin.gachaaddiction.cardpool;

import xiaojin.gachaaddiction.cardpool.transaction.GachaDurability;

import java.time.Duration;
import java.util.Objects;

public record GachaRuntimeSettings(int maximumWalPayloadBytes, int maximumSnapshotBytes,
                                   int maximumInboxEntryBytes, int maximumParameterStateBytes,
                                   Duration groupCommitDelay, GachaDurability recoveryDurability,
                                   Duration publicDrawConfirmationLifetime) {
    public GachaRuntimeSettings {
        if (maximumWalPayloadBytes <= 0) {
            throw new IllegalArgumentException("Maximum WAL payload size must be positive");
        }
        if (maximumSnapshotBytes <= 0) {
            throw new IllegalArgumentException("Maximum snapshot size must be positive");
        }
        if (maximumInboxEntryBytes <= 0) {
            throw new IllegalArgumentException("Maximum inbox entry size must be positive");
        }
        if (maximumParameterStateBytes <= 0) {
            throw new IllegalArgumentException("Maximum parameter state size must be positive");
        }
        Objects.requireNonNull(groupCommitDelay, "groupCommitDelay");
        if (groupCommitDelay.isNegative()) {
            throw new IllegalArgumentException("Group commit delay cannot be negative");
        }
        Objects.requireNonNull(recoveryDurability, "recoveryDurability");
        requirePositive(publicDrawConfirmationLifetime, "publicDrawConfirmationLifetime");
    }

    private static void requirePositive(Duration duration, String name) {
        Objects.requireNonNull(duration, name);
        if (duration.isZero() || duration.isNegative()) {
            throw new IllegalArgumentException(name + " must be positive");
        }
    }
}
